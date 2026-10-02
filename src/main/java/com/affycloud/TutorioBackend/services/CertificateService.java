package com.affycloud.TutorioBackend.services;

import com.affycloud.TutorioBackend.dtos.CertificateRequestDto;
import com.affycloud.TutorioBackend.dtos.CertificateTemplateResponseDto;
import com.affycloud.TutorioBackend.dtos.IssuedCertificateResponseDto;
import com.affycloud.TutorioBackend.dtos.ManualCertificateRequestDto;
import com.affycloud.TutorioBackend.enums.ItemType;
import com.affycloud.TutorioBackend.enums.Role;
import com.affycloud.TutorioBackend.models.CertificateTemplate;
import com.affycloud.TutorioBackend.models.Enrollment;
import com.affycloud.TutorioBackend.models.IssuedCertificate;
import com.affycloud.TutorioBackend.models.Student;
import com.affycloud.TutorioBackend.models.User;
import com.affycloud.TutorioBackend.repositories.CertificateTemplateRepository;
import com.affycloud.TutorioBackend.repositories.EnrollmentRepository;
import com.affycloud.TutorioBackend.repositories.IssuedCertificateRepository;
import com.affycloud.TutorioBackend.repositories.StudentRepository;
import com.affycloud.TutorioBackend.repositories.UserRepository;
import fr.opensagres.xdocreport.document.IXDocReport;
import fr.opensagres.xdocreport.document.registry.XDocReportRegistry;
import fr.opensagres.xdocreport.template.IContext;
import fr.opensagres.xdocreport.template.TemplateEngineKind;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class CertificateService {

    private final CertificateTemplateRepository templateRepository;
    private final IssuedCertificateRepository issuedCertificateRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final S3Client s3Client;

    @Value("${r2.bucket-name}")
    private String bucketName;

    @Value("${r2.public-base-url}")
    private String publicBaseUrl;

    private static final String TEMPLATE_KEY_PREFIX = "certificates/";
    private static final String ISSUED_KEY_PREFIX = "certificates/issued/";
    private static final String TEMPLATE_FILENAME = "certificate_template.docx";
    private static final String DOCX_CONTENT_TYPE =
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
    private static final String PDF_CONTENT_TYPE = "application/pdf";

    private static final boolean IS_WINDOWS =
            System.getProperty("os.name").toLowerCase().contains("win");

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final DateTimeFormatter DISPLAY_DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /** Enrollments older than this are eligible for automatic certificate generation. */
    private static final int AUTO_GENERATE_BEFORE_EXPIRY_DAYS = 7;

    // ─── Upload ───────────────────────────────────────────────────────────────

    public CertificateTemplateResponseDto uploadTemplate(MultipartFile file, User admin) {
        String originalFilename = file.getOriginalFilename();

        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".docx")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only .docx files are allowed");
        }
        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File must not be empty");
        }

        String r2Key = TEMPLATE_KEY_PREFIX + admin.getId() + "/" + TEMPLATE_FILENAME;

        try {
            byte[] cleanedDocx = cleanDocxTemplate(file.getInputStream());

            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(r2Key)
                            .contentType(DOCX_CONTENT_TYPE)
                            .contentLength((long) cleanedDocx.length)
                            .build(),
                    RequestBody.fromBytes(cleanedDocx)
            );
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to upload template: " + e.getMessage());
        }

        CertificateTemplate template = templateRepository.findByAdmin(admin)
                .orElse(new CertificateTemplate());
        template.setAdmin(admin);
        template.setR2Key(r2Key);
        template.setOriginalFilename(originalFilename);
        template.setPublicUrl(publicBaseUrl + "/" + r2Key);
        templateRepository.save(template);

        return toDto(template);
    }

    // ─── Get template ─────────────────────────────────────────────────────────

    public Optional<CertificateTemplateResponseDto> getTemplate(User admin) {
        return templateRepository.findByAdmin(admin).map(this::toDto);
    }

    // ─── Generate ─────────────────────────────────────────────────────────────

    public byte[] generateCertificate(CertificateRequestDto request, User admin) {
        CertificateTemplate template = templateRepository.findByAdmin(admin)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No certificate template found. Please upload one first."));

        if (request.getEnrollmentId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "enrollmentId is required");
        }

        Enrollment enrollment = enrollmentRepository.findById(request.getEnrollmentId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Enrollment not found"));

        Student student = enrollment.getStudent();

        UUID tenantId = admin.getRole() == Role.ADMIN ? admin.getId() : admin.getParent().getId();
        if (student == null || !tenantId.equals(student.getTenantId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "This enrollment does not belong to your organisation");
        }

        if (enrollment.getItemType() != ItemType.COURSE || enrollment.getCourseAttempt() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Certificates can only be generated for course enrollments");
        }

        return generateAndStoreCertificate(enrollment, student, admin, template, request.getCompletionDate());
    }

    /**
     * Generates a one-off certificate from manually entered fields — not tied to any
     * student/enrollment record. Nothing is saved to the database or uploaded to R2;
     * the PDF is rendered and returned directly.
     */
    public byte[] generateManualCertificate(ManualCertificateRequestDto request, User admin) {
        CertificateTemplate template = templateRepository.findByAdmin(admin)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No certificate template found. Please upload one first."));

        if (request.getStudentName() == null || request.getStudentName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "studentName is required");
        }
        if (request.getCourseName() == null || request.getCourseName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "courseName is required");
        }

        String today = DISPLAY_DATE_FMT.format(LocalDate.now());
        String startDate = (request.getStartDate() == null || request.getStartDate().isBlank())
                ? ""
                : toDisplayDate(request.getStartDate());
        String completionDate = (request.getCompletionDate() == null || request.getCompletionDate().isBlank())
                ? today
                : toDisplayDate(request.getCompletionDate());

        try {
            byte[] docxBytes = downloadFromR2(template.getR2Key());
            return renderCertificatePdf(docxBytes, request.getStudentName(), request.getCourseName(),
                    startDate, completionDate, today, generateSerialNumber());
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "PDF generation failed: " + e.getMessage());
        }
    }

    /**
     * Renders the certificate for the given enrollment, uploads it to R2, and
     * upserts its {@link IssuedCertificate} record. Shared by the admin-triggered
     * {@link #generateCertificate} flow and the {@link #generateCertificatesForEligibleEnrollments}
     * scheduled job.
     */
    private byte[] generateAndStoreCertificate(Enrollment enrollment, Student student, User admin,
                                                CertificateTemplate template, String completionDateRaw) {
        UUID tenantId = student.getTenantId();
        String courseName = enrollment.getCourseAttempt().getCourseType().getCourse().getTitle();
        String startDate = formatDate(enrollment.getEnrolledAt());
        String today = DISPLAY_DATE_FMT.format(LocalDate.now());
        String completionDate = (completionDateRaw == null || completionDateRaw.isBlank())
                ? today
                : toDisplayDate(completionDateRaw);

        // Re-issuing for the same enrollment reuses its existing serial number / R2 key.
        Optional<IssuedCertificate> existing = issuedCertificateRepository.findByEnrollment(enrollment);
        String serialNumber = existing.map(IssuedCertificate::getSerialNumber).orElseGet(this::generateSerialNumber);

        try {
            byte[] docxBytes = downloadFromR2(template.getR2Key());
            byte[] pdfBytes = renderCertificatePdf(
                    docxBytes, student.getName(), courseName, startDate, completionDate, today, serialNumber);

            // Upload the issued certificate to R2 and record/refresh the issuance.
            String r2Key = ISSUED_KEY_PREFIX + tenantId + "/" + student.getId() + "/" + serialNumber + ".pdf";
            uploadToR2(r2Key, pdfBytes, PDF_CONTENT_TYPE);

            IssuedCertificate cert = existing.orElseGet(IssuedCertificate::new);
            cert.setStudent(student);
            cert.setEnrollment(enrollment);
            cert.setAdmin(admin);
            cert.setSerialNumber(serialNumber);
            cert.setR2Key(r2Key);
            cert.setPublicUrl(publicBaseUrl + "/" + r2Key);
            cert.setStudentName(student.getName());
            cert.setCourseName(courseName);
            cert.setStartDate(startDate);
            cert.setCompletionDate(completionDate);
            cert.setIssueDate(today);
            issuedCertificateRepository.save(cert);

            return pdfBytes;

        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "PDF generation failed: " + e.getMessage());
        }
    }

    /**
     * Fills the given template DOCX with the supplied field values and converts it to PDF.
     * Shared by the enrollment-backed flow and the manual/ad-hoc certificate flow.
     */
    private byte[] renderCertificatePdf(byte[] templateDocxBytes, String studentName, String courseName,
                                         String startDate, String completionDate, String today,
                                         String serialNumber) throws Exception {
        // 1. Load DOCX via XDocReport + Freemarker — merges split runs automatically.
        IXDocReport report = XDocReportRegistry.getRegistry()
                .loadReport(new ByteArrayInputStream(templateDocxBytes), TemplateEngineKind.Freemarker);

        // 2. Populate context from the resolved field values.
        IContext context = report.createContext();
        context.put("student_name", studentName);
        context.put("course_name", courseName);
        context.put("start_date", startDate);
        context.put("completion_date", completionDate);
        context.put("date", today);
        context.put("s_no", serialNumber);

        // 3. Process template → filled DOCX bytes in memory.
        ByteArrayOutputStream filledDocxOut = new ByteArrayOutputStream();
        report.process(context, filledDocxOut);

        // 4. Remove trailing empty paragraphs XDocReport appends after processing.
        byte[] trimmedDocx = trimTrailingEmptyParagraphs(filledDocxOut.toByteArray());

        // 5. Convert to PDF — Word COM on Windows, LibreOffice on Linux.
        return IS_WINDOWS ? convertViaWord(trimmedDocx) : convertViaLibreOffice(trimmedDocx);
    }

    // ─── Scheduled auto-generation ────────────────────────────────────────────

    /**
     * Auto-generates and stores certificates for every COURSE enrollment that:
     * <ul>
     *   <li>expires within {@link #AUTO_GENERATE_BEFORE_EXPIRY_DAYS} days (or has already expired), and</li>
     *   <li>does not already have an {@link IssuedCertificate}.</li>
     * </ul>
     * Each enrollment's tenant admin must have a certificate template uploaded;
     * enrollments belonging to tenants without one are skipped. Failures for an
     * individual enrollment are logged and do not stop the rest of the batch.
     * Invoked by the daily {@code CertificateGenerationScheduler}.
     */
    public void generateCertificatesForEligibleEnrollments() {
        Date expiryCutoff = Date.from(Instant.now().plus(AUTO_GENERATE_BEFORE_EXPIRY_DAYS, ChronoUnit.DAYS));
        List<Enrollment> candidates = enrollmentRepository.findCandidatesForCertificateGeneration();

        // Use stored enrollmentEndAt — never recompute from expirationDays so plan changes don't affect existing students
        List<Enrollment> eligible = candidates.stream()
                .filter(e -> {
                    Date endAt = e.getEnrollmentEndAt();
                    return endAt != null && !endAt.after(expiryCutoff);
                })
                .toList();

        log.info("Auto certificate generation: {} candidate(s) without a certificate, {} expiring within {} day(s)",
                candidates.size(), eligible.size(), AUTO_GENERATE_BEFORE_EXPIRY_DAYS);

        for (Enrollment enrollment : eligible) {
            try {
                Student student = enrollment.getStudent();
                if (student == null || student.getTenantId() == null) {
                    continue;
                }

                User admin = userRepository.findById(student.getTenantId()).orElse(null);
                if (admin == null) {
                    continue;
                }

                CertificateTemplate template = templateRepository.findByAdmin(admin).orElse(null);
                if (template == null) {
                    log.warn("Skipping enrollment {}: tenant {} has no certificate template",
                            enrollment.getId(), admin.getId());
                    continue;
                }

                generateAndStoreCertificate(enrollment, student, admin, template, null);
                log.info("Auto-generated certificate for enrollment {}", enrollment.getId());
            } catch (Exception e) {
                log.error("Failed to auto-generate certificate for enrollment {}: {}",
                        enrollment.getId(), e.getMessage(), e);
            }
        }
    }

    // ─── Issued certificate lookups ───────────────────────────────────────────

    /** Certificates issued to a student (student-facing "My Certificates"). */
    public List<IssuedCertificateResponseDto> getCertificatesForStudent(Student student) {
        return issuedCertificateRepository.findByStudentOrderByIssuedAtDesc(student).stream()
                .map(IssuedCertificateResponseDto::fromEntity)
                .toList();
    }

    /** In-memory certificate file plus a friendly download filename. */
    public record CertificateFile(byte[] data, String filename, String contentType) {}

    /**
     * Streams an issued certificate's bytes for the owning student. Fetching the file
     * through the API (rather than the raw R2 public URL) keeps the request same-origin
     * with proper auth + CORS + Content-Disposition, so viewing and downloading behave
     * identically on desktop and mobile.
     */
    public CertificateFile getCertificateFileForStudent(UUID certificateId, Student student) {
        IssuedCertificate cert = issuedCertificateRepository.findById(certificateId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Certificate not found"));

        if (cert.getStudent() == null || !cert.getStudent().getId().equals(student.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This certificate does not belong to you");
        }

        byte[] data = downloadFromR2(cert.getR2Key());
        String safeCourse = (cert.getCourseName() == null || cert.getCourseName().isBlank()
                ? "Certificate" : cert.getCourseName()).replaceAll("[^a-zA-Z0-9]", "_");
        return new CertificateFile(data, safeCourse + "_Certificate.pdf", PDF_CONTENT_TYPE);
    }

    /** Certificates issued by an admin (admin-facing "Issued Certificates"). */
    public List<IssuedCertificateResponseDto> getCertificatesForAdmin(User admin) {
        return issuedCertificateRepository.findByAdminOrderByIssuedAtDesc(admin).stream()
                .map(IssuedCertificateResponseDto::fromEntity)
                .toList();
    }

    /** Certificates issued to a specific student (admin-facing, viewed from the student's profile). */
    public List<IssuedCertificateResponseDto> getCertificatesForStudentId(UUID studentId, User admin) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found"));

        UUID tenantId = admin.getRole() == Role.ADMIN ? admin.getId() : admin.getParent().getId();
        if (!tenantId.equals(student.getTenantId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "This student does not belong to your organisation");
        }

        return getCertificatesForStudent(student);
    }

    /** Whether a certificate has already been issued for a given enrollment, and its details if so. */
    public Optional<IssuedCertificateResponseDto> getCertificateForEnrollment(UUID enrollmentId) {
        return enrollmentRepository.findById(enrollmentId)
                .flatMap(issuedCertificateRepository::findByEnrollment)
                .map(IssuedCertificateResponseDto::fromEntity);
    }

    /**
     * Deletes ALL issued certificates belonging to this admin's tenant — removes the
     * PDF files from R2 and the records from the database, so they can be regenerated.
     */
    public Map<String, Object> deleteAllIssuedCertificates(User admin) {
        List<IssuedCertificate> certificates = issuedCertificateRepository.findByAdminOrderByIssuedAtDesc(admin);

        int deletedFiles = 0;
        for (IssuedCertificate cert : certificates) {
            try {
                s3Client.deleteObject(DeleteObjectRequest.builder()
                        .bucket(bucketName)
                        .key(cert.getR2Key())
                        .build());
                deletedFiles++;
            } catch (Exception e) {
                log.error("Failed to delete certificate file {} from R2: {}", cert.getR2Key(), e.getMessage(), e);
            }
        }

        issuedCertificateRepository.deleteAll(certificates);
        log.info("Deleted {} issued certificate record(s), {} file(s) removed from R2",
                certificates.size(), deletedFiles);

        return Map.of(
                "deletedRecords", certificates.size(),
                "deletedFiles", deletedFiles
        );
    }

    // ─── Post-processing ──────────────────────────────────────────────────────

    /**
     * XDocReport appends trailing empty paragraphs after processing a template.
     * On a tight single-page certificate layout these push the signature block
     * onto a blank page 2.  This strips them directly from word/document.xml
     * inside the ZIP — no extra dependency needed.
     */
    private byte[] trimTrailingEmptyParagraphs(byte[] docxBytes) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(docxBytes));
                 ZipOutputStream zos = new ZipOutputStream(out)) {
                ZipEntry entry;
                while ((entry = zis.getNextEntry()) != null) {
                    zos.putNextEntry(new ZipEntry(entry.getName()));
                    if ("word/document.xml".equals(entry.getName())) {
                        String xml = new String(zis.readAllBytes(),
                                java.nio.charset.StandardCharsets.UTF_8);
                        zos.write(removeTrailingEmptyParas(xml)
                                .getBytes(java.nio.charset.StandardCharsets.UTF_8));
                    } else {
                        zis.transferTo(zos);
                    }
                    zos.closeEntry();
                    zis.closeEntry();
                }
            }
            return out.toByteArray();
        } catch (Exception e) {
            return docxBytes;
        }
    }

    /**
     * Removes consecutive trailing &lt;w:p&gt; elements that have no &lt;w:t&gt;
     * text content, immediately before the closing &lt;/w:body&gt; tag.
     */
    private String removeTrailingEmptyParas(String xml) {
        int bodyClose = xml.lastIndexOf("</w:body>");
        if (bodyClose < 0) return xml;

        String head = xml.substring(0, bodyClose);
        String tail = xml.substring(bodyClose);

        boolean removed = true;
        while (removed) {
            removed = false;
            String h = head.stripTrailing();
            if (!h.endsWith("</w:p>")) break;

            int openTag = h.lastIndexOf("<w:p");
            if (openTag < 0) break;

            // Only strip if this paragraph has no text runs
            String para = h.substring(openTag);
            if (!para.contains("<w:t")) {
                head = h.substring(0, openTag);
                removed = true;
            }
        }
        return head + tail;
    }

    // ─── LibreOffice conversion (Linux / production) ──────────────────────────

    /**
     * Converts DOCX bytes to PDF using LibreOffice headless.
     * Preserves all backgrounds, images, fonts, and shapes exactly as designed.
     * Requires: sudo apt-get install -y libreoffice
     *
     * Each call gets its own temp dir + LibreOffice user profile to avoid
     * concurrent-request conflicts.
     */
    private byte[] convertViaLibreOffice(byte[] docxBytes) throws Exception {
        Path tempDir = Files.createTempDirectory("cert_");
        Path inputDocx = tempDir.resolve("input.docx");
        Path outputPdf = tempDir.resolve("input.pdf");
        Path loProfile = tempDir.resolve("lo_profile");

        try {
            Files.write(inputDocx, docxBytes);
            Files.createDirectories(loProfile);

            // file:// URI for the isolated LibreOffice profile
            String profileUri = loProfile.toUri().toString();

            // EmbedStandardFonts=true  — embeds all fonts used in the DOCX into the PDF
            // so custom certificate fonts render identically on every device.
            ProcessBuilder pb = new ProcessBuilder(
                    "libreoffice",
                    "-env:UserInstallation=" + profileUri,
                    "--headless",
                    "--norestore",
                    "--convert-to", "pdf:writer_pdf_Export:EmbedStandardFonts=true,EmbedComplexScriptFonts=true",
                    "--outdir", tempDir.toString(),
                    inputDocx.toString()
            );
            pb.redirectErrorStream(true);

            Process proc = pb.start();
            String output = new String(proc.getInputStream().readAllBytes());
            boolean finished = proc.waitFor(120, TimeUnit.SECONDS);

            if (!finished) {
                proc.destroyForcibly();
                throw new RuntimeException("LibreOffice timed out after 120 s");
            }
            if (proc.exitValue() != 0) {
                throw new RuntimeException(
                        "LibreOffice exited " + proc.exitValue() + ": " + output);
            }
            if (!Files.exists(outputPdf)) {
                throw new RuntimeException(
                        "LibreOffice did not create PDF. Output: " + output);
            }

            return Files.readAllBytes(outputPdf);

        } finally {
            deleteDirectory(tempDir);
        }
    }

    // ─── Word COM conversion (Windows / local dev) ────────────────────────────

    /**
     * Converts DOCX bytes to PDF using Microsoft Word via PowerShell COM automation.
     * Used automatically on Windows (local development).
     * Requires: Microsoft Word installed.
     */
    private byte[] convertViaWord(byte[] docxBytes) throws Exception {
        Path tempDocx = Files.createTempFile("cert_", ".docx");
        String docxAbs = tempDocx.toAbsolutePath().toString();
        String pdfAbs = docxAbs.substring(0, docxAbs.length() - 5) + ".pdf";
        Path tempPdf = Paths.get(pdfAbs);

        try {
            Files.write(tempDocx, docxBytes);

            String ps = String.join("; ",
                    "$w = New-Object -ComObject Word.Application",
                    "$w.Visible = $false",
                    "$w.DisplayAlerts = 0",
                    "$d = $w.Documents.Open('" + docxAbs + "')",
                    "$d.SaveAs2('" + pdfAbs + "', 17)",
                    "$d.Close($false)",
                    "$w.Quit()",
                    "[System.Runtime.InteropServices.Marshal]::ReleaseComObject($w) | Out-Null"
            );

            Process proc = new ProcessBuilder(
                    "powershell", "-NonInteractive", "-NoProfile", "-Command", ps)
                    .redirectErrorStream(true)
                    .start();

            String output = new String(proc.getInputStream().readAllBytes());
            boolean finished = proc.waitFor(90, TimeUnit.SECONDS);

            if (!finished) {
                proc.destroyForcibly();
                throw new RuntimeException("Word conversion timed out after 90 s");
            }
            if (proc.exitValue() != 0) {
                throw new RuntimeException(
                        "Word conversion exited " + proc.exitValue() + ": " + output);
            }
            if (!Files.exists(tempPdf)) {
                throw new RuntimeException(
                        "PDF not produced by Word. Output: " + output);
            }

            return Files.readAllBytes(tempPdf);

        } finally {
            Files.deleteIfExists(tempDocx);
            Files.deleteIfExists(tempPdf);
        }
    }

    // ─── R2 helpers ───────────────────────────────────────────────────────────

    private byte[] downloadFromR2(String key) {
        try {
            ResponseInputStream<GetObjectResponse> response = s3Client.getObject(
                    GetObjectRequest.builder()
                            .bucket(bucketName)
                            .key(key)
                            .build()
            );
            return response.readAllBytes();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to download template from storage: " + e.getMessage());
        }
    }

    private void uploadToR2(String key, byte[] bytes, String contentType) {
        try {
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(key)
                            .contentType(contentType)
                            .contentLength((long) bytes.length)
                            .build(),
                    RequestBody.fromBytes(bytes)
            );
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to upload certificate to storage: " + e.getMessage());
        }
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    /**
     * Generates a unique certificate serial number in the form
     * {@code ddMMyy} + {@code AC} + a random 3-digit number, e.g. {@code 100626AC123}.
     * Retries on collision until a number not yet present in
     * {@link IssuedCertificateRepository} is found.
     */
    private String generateSerialNumber() {
        String datePart = DateTimeFormatter.ofPattern("ddMMyy").format(LocalDate.now());

        String serial;
        do {
            String randomPart = String.format("%03d", RANDOM.nextInt(1000));
            serial = datePart + "AC" + randomPart;
        } while (issuedCertificateRepository.existsBySerialNumber(serial));

        return serial;
    }

    /** Converts YYYY-MM-DD → dd/MM/yyyy; returns original string for non-dates. */
    private String toDisplayDate(String value) {
        try { return DISPLAY_DATE_FMT.format(LocalDate.parse(value)); }
        catch (Exception e) { return value; }
    }

    /** Formats a java.util.Date as dd/MM/yyyy. */
    private String formatDate(java.util.Date date) {
        if (date == null) return "";
        return DISPLAY_DATE_FMT.format(
                date.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate());
    }

    private CertificateTemplateResponseDto toDto(CertificateTemplate t) {
        return new CertificateTemplateResponseDto(
                t.getId(), t.getOriginalFilename(), t.getPublicUrl(), t.getUpdatedAt());
    }

    private void deleteDirectory(Path dir) {
        try {
            Files.walk(dir)
                    .sorted(Comparator.reverseOrder())
                    .forEach(p -> { try { Files.delete(p); } catch (Exception ignored) {} });
        } catch (Exception ignored) {}
    }

    // ─── Template cleaning ────────────────────────────────────────────────────

    /**
     * Reads the DOCX zip in memory, rewrites word/document.xml to merge any
     * XML-fragmented ${...} placeholders that Word split across multiple runs,
     * then returns clean DOCX bytes.
     */
    private byte[] cleanDocxTemplate(InputStream in) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (ZipInputStream zis = new ZipInputStream(in);
             ZipOutputStream zos = new ZipOutputStream(out)) {

            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
                    "\\$(?:<[^>]*>|\\s)*\\{(?:<[^>]*>|[^$}])*\\}");
            ZipEntry entry;

            while ((entry = zis.getNextEntry()) != null) {
                zos.putNextEntry(new ZipEntry(entry.getName()));

                if ("word/document.xml".equals(entry.getName())) {
                    String xml = new String(zis.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
                    java.util.regex.Matcher matcher = pattern.matcher(xml);
                    String cleanedXml = matcher.replaceAll(mr -> {
                        String cleanText = mr.group().replaceAll("<[^>]+>", "");
                        return java.util.regex.Matcher.quoteReplacement(cleanText);
                    });
                    zos.write(cleanedXml.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                } else {
                    zis.transferTo(zos);
                }

                zos.closeEntry();
                zis.closeEntry();
            }
        }
        return out.toByteArray();
    }
}
