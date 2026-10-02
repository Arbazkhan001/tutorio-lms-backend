package com.affycloud.TutorioBackend.controllers;

import com.affycloud.TutorioBackend.dtos.CertificateRequestDto;
import com.affycloud.TutorioBackend.dtos.CertificateTemplateResponseDto;
import com.affycloud.TutorioBackend.dtos.IssuedCertificateResponseDto;
import com.affycloud.TutorioBackend.dtos.ManualCertificateRequestDto;
import com.affycloud.TutorioBackend.models.Student;
import com.affycloud.TutorioBackend.models.User;
import com.affycloud.TutorioBackend.services.CertificateService;
import com.affycloud.TutorioBackend.utils.AuthUtilAdmin;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CertificateController {

    private final CertificateService certificateService;
    private final AuthUtilAdmin authUtilAdmin;

    @PostMapping("/api/templates/upload")
    public ResponseEntity<Map<String, Object>> uploadTemplate(
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request) {
        User admin = authUtilAdmin.getAuthenticatedUser(request);
        CertificateTemplateResponseDto dto = certificateService.uploadTemplate(file, admin);
        return ResponseEntity.ok(Map.of(
                "message", "Template uploaded successfully",
                "template", dto
        ));
    }

    @GetMapping("/api/templates/my")
    public ResponseEntity<?> getMyTemplate(HttpServletRequest request) {
        User admin = authUtilAdmin.getAuthenticatedUser(request);
        return certificateService.getTemplate(admin)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/api/certificates/generate")
    public ResponseEntity<byte[]> generateCertificate(
            @RequestBody CertificateRequestDto request,
            HttpServletRequest httpRequest) {
        User admin = authUtilAdmin.getAuthenticatedUser(httpRequest);
        byte[] pdf = certificateService.generateCertificate(request, admin);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "certificate.pdf");
        headers.setContentLength(pdf.length);

        return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
    }

    @PostMapping("/api/certificates/generate-manual")
    public ResponseEntity<byte[]> generateManualCertificate(
            @RequestBody ManualCertificateRequestDto request,
            HttpServletRequest httpRequest) {
        User admin = authUtilAdmin.getAuthenticatedUser(httpRequest);
        byte[] pdf = certificateService.generateManualCertificate(request, admin);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "certificate.pdf");
        headers.setContentLength(pdf.length);

        return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
    }

    @GetMapping("/api/certificates/issued/enrollment/{enrollmentId}")
    public ResponseEntity<?> getIssuedCertificateForEnrollment(
            @PathVariable UUID enrollmentId,
            HttpServletRequest httpRequest) {
        authUtilAdmin.getAuthenticatedUser(httpRequest);
        return certificateService.getCertificateForEnrollment(enrollmentId)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/api/certificates/issued")
    public ResponseEntity<List<IssuedCertificateResponseDto>> getIssuedCertificates(
            HttpServletRequest httpRequest) {
        User admin = authUtilAdmin.getAuthenticatedUser(httpRequest);
        return ResponseEntity.ok(certificateService.getCertificatesForAdmin(admin));
    }

    @GetMapping("/api/certificates/issued/student/{studentId}")
    public ResponseEntity<List<IssuedCertificateResponseDto>> getIssuedCertificatesForStudent(
            @PathVariable UUID studentId,
            HttpServletRequest httpRequest) {
        User admin = authUtilAdmin.getAuthenticatedUser(httpRequest);
        return ResponseEntity.ok(certificateService.getCertificatesForStudentId(studentId, admin));
    }

    @GetMapping("/api/certificates/my")
    public ResponseEntity<List<IssuedCertificateResponseDto>> getMyCertificates(
            HttpServletRequest httpRequest) {
        Student student = authUtilAdmin.getAuthenticatedStudent(httpRequest);
        return ResponseEntity.ok(certificateService.getCertificatesForStudent(student));
    }

    /**
     * Streams the authenticated student's certificate PDF through the API.
     * {@code ?download=true} serves it as an attachment (Save to device); otherwise
     * it is served inline for the in-app viewer. Same-origin + auth + correct
     * Content-Disposition make viewing and downloading reliable on mobile and desktop.
     */
    @GetMapping("/api/certificates/{id}/file")
    public ResponseEntity<byte[]> getCertificateFile(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "false") boolean download,
            HttpServletRequest httpRequest) {
        Student student = authUtilAdmin.getAuthenticatedStudent(httpRequest);
        CertificateService.CertificateFile file = certificateService.getCertificateFileForStudent(id, student);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentLength(file.data().length);
        headers.setContentDisposition(ContentDisposition
                .builder(download ? "attachment" : "inline")
                .filename(file.filename())
                .build());

        return new ResponseEntity<>(file.data(), headers, HttpStatus.OK);
    }

    // @DeleteMapping("/api/certificates/issued/all")
    // public ResponseEntity<Map<String, Object>> deleteAllIssuedCertificates(HttpServletRequest httpRequest) {
    //     User admin = authUtilAdmin.getAuthenticatedUser(httpRequest);
    //     return ResponseEntity.ok(certificateService.deleteAllIssuedCertificates(admin));
    // }
}
