package com.affycloud.TutorioBackend.services.implementation;

import com.affycloud.TutorioBackend.models.Order;
import com.affycloud.TutorioBackend.models.OrderItem;
import com.affycloud.TutorioBackend.services.InvoiceGeneratorService;
import com.openhtmltopdf.java2d.api.BufferedImagePageProcessor;
import com.openhtmltopdf.java2d.api.Java2DRendererBuilder;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Production-ready invoice generator.
 *
 * <p>
 * generateEmailHtml → table-based HTML with inline CSS (Gmail/Outlook safe)
 * <p>
 * generateInvoicePng → renders the same HTML to a high-res PNG via
 * openhtmltopdf-java2d
 */
@Service
public class InvoiceGeneratorServiceImpl implements InvoiceGeneratorService {

    // ── Brand colours (MUST NOT be changed) ─────────────────────────────────
    private static final String COLOR_PRIMARY = "#d4af37"; // golden
    private static final String COLOR_BG = "#f5f6fa";
    private static final String COLOR_TEXT = "#333333";
    private static final String COLOR_MUTED = "#777777";
    private static final String COLOR_BORDER = "#e8e8e8";
    private static final String COLOR_TABLE_HDR = "#f9f7ee";

    // ── Logo URLs ────────────────────────────────────────────────────────────
    private static final String LOGO_ACCENTTALK = "https://accenttalk.in/assets/img/logo.png";
    private static final String LOGO_TUTORIO = "https://accent-talk.tutorio.in/assets/img/tutorio-logo-half.png";
    private static final String LOGO_AFFY = "https://affycloudsolution.com/static/media/affy_logo.7354960bda008279c013.png";

    // ── Brand URLs ───────────────────────────────────────────────────────────
    private static final String URL_ACCENTTALK = "https://accenttalk.in/";
    private static final String URL_AFFY = "https://www.affyclouditsolutions.com/";

    // ── Currency formatter ───────────────────────────────────────────────────
    private static final NumberFormat CURRENCY = NumberFormat.getInstance(Locale.of("en", "IN"));

    static {
        CURRENCY.setMinimumFractionDigits(2);
        CURRENCY.setMaximumFractionDigits(2);
    }

    // ────────────────────────────────────────────────────────────────────────
    // PUBLIC API
    // ────────────────────────────────────────────────────────────────────────

    @Override
    public String generateEmailHtml(Order order) {
        return buildHtml(order, false);
    }

    @Override
    public byte[] generateInvoicePng(Order order) {
        try {
            // openhtmltopdf requires valid XHTML; wrap our HTML
            String xhtml = buildHtml(order, true);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            // BufferedImagePageProcessor renders at the given scale factor
            BufferedImagePageProcessor pageProcessor = new BufferedImagePageProcessor(BufferedImage.TYPE_INT_ARGB, 1.5);

            Java2DRendererBuilder builder = new Java2DRendererBuilder();
            builder.withHtmlContent(xhtml, null);
            builder.toSinglePage(pageProcessor);
            builder.runFirstPage();

            List<BufferedImage> pages = pageProcessor.getPageImages();
            if (pages.isEmpty()) {
                throw new RuntimeException("openhtmltopdf rendered 0 pages");
            }

            // Use the first (and only) page
            ImageIO.write(pages.get(0), "PNG", baos);
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate invoice PNG: " + e.getMessage(), e);
        }
    }

    // ────────────────────────────────────────────────────────────────────────
    // PRIVATE – HTML BUILDER
    // ────────────────────────────────────────────────────────────────────────

    /**
     * Builds the complete invoice HTML.
     *
     * @param forPng when {@code true}, wraps output in &lt;?xml…&gt; + DOCTYPE
     *               so openhtmltopdf can parse it as XHTML.
     */
    private String buildHtml(Order order, boolean forPng) {
        String studentName = safe(order.getStudent().getName());
        String studentEmail = safe(order.getStudent().getEmail());
        String studentPhone = safe(order.getStudent().getPhoneNumber());
        String invoiceId = safe(order.getRandomId());
        String payStatus = safe(order.getPaymentStatus());
        String dateStr = formatDate(order.getCreatedAt());

        String itemsHtml = buildItemsRows(order.getOrderItems());
        String totalsHtml = buildTotalsBlock(order);

        String xhtmlPrefix = forPng
                ? "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                        + "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" "
                        + "\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n"
                        + "<html xmlns=\"http://www.w3.org/1999/xhtml\">\n"
                : "<html>\n";

        return xhtmlPrefix
                + "<head>\n"
                + "<meta charset=\"UTF-8\" />\n"
                + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\" />\n"
                + "<title>Invoice " + invoiceId + " - AccentTalk / Tutorio</title>\n"
                + "</head>\n"
                + "<body style=\"margin:0;padding:0;background-color:" + COLOR_BG + ";"
                + "font-family:Arial,Helvetica,sans-serif;\">\n"

                // ── Outer wrapper ────────────────────────────────────────────
                + "<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" "
                + "style=\"background-color:" + COLOR_BG + ";\">\n"
                + "  <tr><td align=\"center\" style=\"padding:30px 10px;\">\n"

                // ── Inner card (600px) ───────────────────────────────────────
                + "    <table width=\"600\" cellpadding=\"0\" cellspacing=\"0\" "
                + "style=\"background:#ffffff;border-radius:10px;overflow:hidden;"
                + "border:1px solid " + COLOR_BORDER + ";\">\n"

                // ══ HEADER ═══════════════════════════════════════════════════
                + buildHeader()

                // ══ GREETING ═════════════════════════════════════════════════
                + buildGreeting(studentName)

                // ══ INVOICE META ══════════════════════════════════════════════
                + buildInvoiceMeta(invoiceId, dateStr, payStatus)

                // ══ BILLING INFO ══════════════════════════════════════════════
                + buildBillingInfo(studentName, studentEmail, studentPhone)

                // ══ ITEMS TABLE ═══════════════════════════════════════════════
                + buildItemsTable(itemsHtml)

                // ══ TOTALS ════════════════════════════════════════════════════
                + totalsHtml

                // ══ HELP NOTE ═════════════════════════════════════════════════
                + buildHelpNote()

                // ══ FOOTER ════════════════════════════════════════════════════
                + buildFooter()

                + "    </table>\n"
                + "  </td></tr>\n"
                + "</table>\n"
                + "</body>\n"
                + "</html>\n";
    }

    // ── Section builders ─────────────────────────────────────────────────────

    private String buildHeader() {
        return "      <!-- HEADER -->\n"
                + "      <tr>\n"
                + "        <td style=\"background:" + COLOR_PRIMARY + ";padding:24px 30px;"
                + "text-align:center;\">\n"

                // AccentTalk logo (clickable)
                + "          <a href=\"" + URL_ACCENTTALK + "\" target=\"_blank\" "
                + "style=\"display:inline-block;text-decoration:none;\">\n"
                + "            <img src=\"" + LOGO_ACCENTTALK + "\" alt=\"AccentTalk\" "
                + "style=\"max-height:55px;width:auto;border:0;display:block;"
                + "margin:0 auto 10px auto;\" />\n"
                + "          </a>\n"

                // Title
                + "          <p style=\"margin:6px 0 0 0;font-size:13px;color:#ffffff;"
                + "letter-spacing:1px;text-transform:uppercase;\">"
                + "Order Confirmation &amp; Invoice</p>\n"
                + "        </td>\n"
                + "      </tr>\n";
    }

    private String buildGreeting(String name) {
        return "      <!-- GREETING -->\n"
                + "      <tr>\n"
                + "        <td style=\"padding:28px 30px 10px 30px;color:" + COLOR_TEXT + ";\">\n"
                + "          <p style=\"margin:0 0 8px 0;font-size:16px;\">Hi "
                + "<strong>" + name + "</strong>,</p>\n"
                + "          <p style=\"margin:0;font-size:14px;line-height:1.6;color:#555555;\">"
                + "Thank you for your purchase! Your order has been successfully completed "
                + "and is confirmed. Please find your invoice summary below.</p>\n"
                + "        </td>\n"
                + "      </tr>\n";
    }

    private String buildInvoiceMeta(String invoiceId, String date, String payStatus) {
        String badgeColor = "PAID".equalsIgnoreCase(payStatus) ? "#27ae60" : "#e74c3c";
        return "      <!-- INVOICE META -->\n"
                + "      <tr>\n"
                + "        <td style=\"padding:10px 30px 20px 30px;\">\n"
                + "          <table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" "
                + "style=\"border:1px solid " + COLOR_BORDER + ";border-radius:6px;"
                + "background:#fafafa;\">\n"
                + "            <tr>\n"
                + "              <td style=\"padding:14px 20px;font-size:13px;color:#555555;"
                + "width:50%;border-right:1px solid " + COLOR_BORDER + ";\">\n"
                + "                <strong style=\"color:" + COLOR_TEXT + ";\">Invoice No.</strong><br/>\n"
                + "                <span style=\"font-size:15px;font-weight:bold;"
                + "color:" + COLOR_PRIMARY + ";\">" + invoiceId + "</span>\n"
                + "              </td>\n"
                + "              <td style=\"padding:14px 20px;font-size:13px;color:#555555;"
                + "width:30%;border-right:1px solid " + COLOR_BORDER + ";\">\n"
                + "                <strong style=\"color:" + COLOR_TEXT + ";\">Date</strong><br/>\n"
                + "                <span>" + date + "</span>\n"
                + "              </td>\n"
                + "              <td style=\"padding:14px 20px;font-size:13px;color:#555555;"
                + "width:20%;text-align:center;\">\n"
                + "                <strong style=\"color:" + COLOR_TEXT + ";\">Status</strong><br/>\n"
                + "                <span style=\"display:inline-block;margin-top:4px;padding:3px 10px;"
                + "background:" + badgeColor + ";color:#ffffff;border-radius:12px;"
                + "font-size:11px;font-weight:bold;letter-spacing:0.5px;\">"
                + payStatus + "</span>\n"
                + "              </td>\n"
                + "            </tr>\n"
                + "          </table>\n"
                + "        </td>\n"
                + "      </tr>\n";
    }

    private String buildBillingInfo(String name, String email, String phone) {
        return "      <!-- BILLING INFO -->\n"
                + "      <tr>\n"
                + "        <td style=\"padding:0 30px 20px 30px;\">\n"
                + "          <p style=\"margin:0 0 8px 0;font-size:13px;font-weight:bold;"
                + "color:" + COLOR_TEXT + ";text-transform:uppercase;letter-spacing:0.5px;\">"
                + "Bill To</p>\n"
                + "          <table width=\"100%\" cellpadding=\"8\" cellspacing=\"0\" "
                + "style=\"background:#fafafa;border:1px solid " + COLOR_BORDER + ";"
                + "border-radius:6px;font-size:13px;color:#555555;\">\n"
                + "            <tr>\n"
                + "              <td style=\"width:50%;\">\n"
                + "                <strong style=\"color:" + COLOR_TEXT + ";\">" + name + "</strong><br/>\n"
                + "                <a href=\"mailto:" + email + "\" style=\"color:#555555;"
                + "text-decoration:none;\">" + email + "</a>\n"
                + "              </td>\n"
                + "              <td style=\"width:50%;text-align:right;\">\n"
                + "                <span>" + (isBlank(phone) ? "&nbsp;" : phone) + "</span>\n"
                + "              </td>\n"
                + "            </tr>\n"
                + "          </table>\n"
                + "        </td>\n"
                + "      </tr>\n";
    }

    private String buildItemsTable(String itemsHtml) {
        return "      <!-- ITEMS TABLE -->\n"
                + "      <tr>\n"
                + "        <td style=\"padding:0 30px 10px 30px;\">\n"
                + "          <p style=\"margin:0 0 8px 0;font-size:13px;font-weight:bold;"
                + "color:" + COLOR_TEXT + ";text-transform:uppercase;letter-spacing:0.5px;\">"
                + "Items Purchased</p>\n"
                + "          <table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" "
                + "style=\"border-collapse:collapse;font-size:13px;\">\n"

                // Table header row
                + "            <tr style=\"background:" + COLOR_TABLE_HDR + ";\">\n"
                + "              <th style=\"padding:10px 12px;text-align:left;color:" + COLOR_TEXT + ";"
                + "border-bottom:2px solid " + COLOR_PRIMARY + ";font-weight:bold;width:44%;\">"
                + "Item</th>\n"
                + "              <th style=\"padding:10px 12px;text-align:center;color:" + COLOR_TEXT + ";"
                + "border-bottom:2px solid " + COLOR_PRIMARY + ";font-weight:bold;width:18%;\">"
                + "Type</th>\n"
                + "              <th style=\"padding:10px 12px;text-align:center;color:" + COLOR_TEXT + ";"
                + "border-bottom:2px solid " + COLOR_PRIMARY + ";font-weight:bold;width:10%;\">"
                + "Qty</th>\n"
                + "              <th style=\"padding:10px 12px;text-align:right;color:" + COLOR_TEXT + ";"
                + "border-bottom:2px solid " + COLOR_PRIMARY + ";font-weight:bold;width:14%;\">"
                + "Unit Price</th>\n"
                + "              <th style=\"padding:10px 12px;text-align:right;color:" + COLOR_TEXT + ";"
                + "border-bottom:2px solid " + COLOR_PRIMARY + ";font-weight:bold;width:14%;\">"
                + "Total</th>\n"
                + "            </tr>\n"

                + itemsHtml // dynamic rows injected here

                + "          </table>\n"
                + "        </td>\n"
                + "      </tr>\n";
    }

    /**
     * Builds per-item
     * <tr>
     * rows
     */
    private String buildItemsRows(List<OrderItem> items) {
        if (items == null || items.isEmpty()) {
            return "<tr><td colspan=\"5\" style=\"padding:12px;text-align:center;"
                    + "color:#999;\">No items found.</td></tr>\n";
        }

        StringBuilder sb = new StringBuilder();
        boolean alternate = false;

        for (OrderItem item : items) {
            String rowBg = alternate ? "#f9f9f9" : "#ffffff";
            alternate = !alternate;

            double unitPrice = item.getPrice() != null ? item.getPrice() : 0.0;
            double finalAmt = item.getFinalPrice() != null ? item.getFinalPrice() : 0.0;
            int qty = item.getQuantity() != null ? item.getQuantity() : 1;

            sb.append("            <tr style=\"background:").append(rowBg).append(";\">\n")
                    .append("              <td style=\"padding:10px 12px;border-bottom:1px solid ")
                    .append(COLOR_BORDER).append(";color:").append(COLOR_TEXT).append(";\">\n")
                    .append("                ").append(safe(item.getTitle())).append("\n")
                    .append("              </td>\n")
                    .append("              <td style=\"padding:10px 12px;border-bottom:1px solid ")
                    .append(COLOR_BORDER).append(";text-align:center;color:#666;font-size:12px;\">\n")
                    .append("                <span style=\"background:#eef0f4;border-radius:10px;"
                            + "padding:2px 8px;\">")
                    .append(item.getItemType() != null ? item.getItemType().toString() : "-")
                    .append("</span>\n")
                    .append("              </td>\n")
                    .append("              <td style=\"padding:10px 12px;border-bottom:1px solid ")
                    .append(COLOR_BORDER).append(";text-align:center;\">")
                    .append(qty).append("</td>\n")
                    .append("              <td style=\"padding:10px 12px;border-bottom:1px solid ")
                    .append(COLOR_BORDER).append(";text-align:right;\">&#8377;")
                    .append(CURRENCY.format(unitPrice)).append("</td>\n")
                    .append("              <td style=\"padding:10px 12px;border-bottom:1px solid ")
                    .append(COLOR_BORDER).append(";text-align:right;font-weight:bold;\">"
                            + "&#8377;")
                    .append(CURRENCY.format(finalAmt)).append("</td>\n")
                    .append("            </tr>\n");
        }
        return sb.toString();
    }

    /** Builds subtotal / discount / total block */
    private String buildTotalsBlock(Order order) {
        double subtotal = order.getAmount() != null ? order.getAmount() : 0.0;
        double total = order.getTotalAmount() != null ? order.getTotalAmount() : 0.0;
        double discount = order.getDiscountAmount() != null ? order.getDiscountAmount() : 0.0;
        String couponCode = (order.getCoupon() != null && order.getCoupon().getCode() != null)
                ? order.getCoupon().getCode()
                : null;

        StringBuilder sb = new StringBuilder();
        sb.append("      <!-- TOTALS -->\n")
                .append("      <tr>\n")
                .append("        <td style=\"padding:10px 30px 20px 30px;\">\n")
                .append("          <table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\">\n")
                .append("            <tr>\n")
                .append("              <td></td>\n") // left spacer
                .append("              <td style=\"width:260px;\">\n")
                .append("                <table width=\"100%\" cellpadding=\"6\" cellspacing=\"0\" "
                        + "style=\"font-size:13px;\">\n");

        // Subtotal row
        sb.append("                  <tr>\n")
                .append("                    <td style=\"color:#555555;\">Subtotal</td>\n")
                .append("                    <td style=\"text-align:right;color:")
                .append(COLOR_TEXT).append(";\">&#8377;")
                .append(CURRENCY.format(subtotal)).append("</td>\n")
                .append("                  </tr>\n");

        // Discount row (only if coupon was applied)
        if (discount > 0) {
            String couponLabel = couponCode != null ? "Discount (" + couponCode + ")" : "Discount";
            sb.append("                  <tr>\n")
                    .append("                    <td style=\"color:#27ae60;\">").append(couponLabel)
                    .append("</td>\n")
                    .append("                    <td style=\"text-align:right;color:#27ae60;\">"
                            + "- &#8377;")
                    .append(CURRENCY.format(discount)).append("</td>\n")
                    .append("                  </tr>\n");
        }

        // Separator + Total
        sb.append("                  <tr>\n")
                .append("                    <td colspan=\"2\" style=\"padding:4px 0;\">\n")
                .append("                      <hr style=\"border:none;border-top:1px solid ")
                .append(COLOR_BORDER).append(";margin:2px 0;\" />\n")
                .append("                    </td>\n")
                .append("                  </tr>\n")
                .append("                  <tr>\n")
                .append("                    <td style=\"font-size:15px;font-weight:bold;"
                        + "color:")
                .append(COLOR_TEXT).append(";\">Total</td>\n")
                .append("                    <td style=\"text-align:right;font-size:18px;"
                        + "font-weight:bold;color:")
                .append(COLOR_PRIMARY).append(";\">"
                        + "&#8377;")
                .append(CURRENCY.format(total)).append("</td>\n")
                .append("                  </tr>\n")
                .append("                </table>\n")
                .append("              </td>\n")
                .append("            </tr>\n")
                .append("          </table>\n")
                .append("        </td>\n")
                .append("      </tr>\n");

        return sb.toString();
    }

    private String buildHelpNote() {
        return "      <!-- HELP NOTE -->\n"
                + "      <tr>\n"
                + "        <td style=\"padding:0 30px 24px 30px;\">\n"
                + "          <table width=\"100%\" cellpadding=\"14\" cellspacing=\"0\" "
                + "style=\"background:#fffbea;border:1px solid " + COLOR_PRIMARY + "33;"
                + "border-left:4px solid " + COLOR_PRIMARY + ";border-radius:4px;"
                + "font-size:12px;color:#666666;\">\n"
                + "            <tr>\n"
                + "              <td>&#128220; This is a system-generated invoice. "
                + "For any queries, contact us via "
                + "<a href=\"" + URL_ACCENTTALK + "\" target=\"_blank\" "
                + "style=\"color:" + COLOR_PRIMARY + ";text-decoration:none;\">"
                + "accenttalk.in</a>.</td>\n"
                + "            </tr>\n"
                + "          </table>\n"
                + "        </td>\n"
                + "      </tr>\n";
    }

    private String buildFooter() {
        return "      <!-- FOOTER -->\n"
                + "      <tr>\n"
                + "        <td style=\"background:#f2f2f2;padding:20px 30px;"
                + "border-top:1px solid " + COLOR_BORDER + ";\">\n"

                // Partner logos row
                + "          <table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\">\n"
                + "            <tr>\n"

                // Tutorio logo (non-clickable as per requirement)
                + "              <td style=\"text-align:center;width:50%;"
                + "padding-right:10px;border-right:1px solid " + COLOR_BORDER + ";\">\n"
                + "                <img src=\"" + LOGO_TUTORIO + "\" alt=\"Tutorio\" "
                + "style=\"max-height:40px;width:auto;border:0;\" />\n"
                + "                <p style=\"margin:4px 0 0 0;font-size:10px;"
                + "color:" + COLOR_MUTED + ";\">Tutorio LMS Platform</p>\n"
                + "              </td>\n"

                // Affy Cloud logo (clickable)
                + "              <td style=\"text-align:center;width:50%;padding-left:10px;\">\n"
                + "                <a href=\"" + URL_AFFY + "\" target=\"_blank\" "
                + "style=\"text-decoration:none;display:inline-block;\">\n"
                + "                  <img src=\"" + LOGO_AFFY + "\" alt=\"Affy Cloud IT Solutions\" "
                + "style=\"max-height:40px;width:auto;border:0;\" />\n"
                + "                </a>\n"
                + "                <p style=\"margin:4px 0 0 0;font-size:10px;"
                + "color:" + COLOR_MUTED + ";\">"
                + "Powered by "
                + "<a href=\"" + URL_AFFY + "\" target=\"_blank\" "
                + "style=\"color:" + COLOR_MUTED + ";text-decoration:none;\">"
                + "Affy Cloud Solution"
                + "</a>"
                + "</p>\n"
                + "              </td>\n"
                + "            </tr>\n"
                + "          </table>\n"

                // Copyright line
                + "          <p style=\"margin:16px 0 0 0;font-size:11px;"
                + "color:" + COLOR_MUTED + ";text-align:center;\">\n"
//                + "            &copy; 2026\n"
//                + "            <a href=\"" + URL_ACCENTTALK + "\" target=\"_blank\" "
//                + "style=\"color:" + COLOR_MUTED + ";text-decoration:none;\">AccentTalk</a>\n"
//                + "            &nbsp;|&nbsp;\n"
//                + "            <a href=\"" + URL_AFFY + "\" target=\"_blank\" "
//                + "style=\"color:" + COLOR_MUTED + ";text-decoration:none;\">"
//                + "Affy Cloud Solution Pvt. Ltd.</a>\n"
                + "          </p>\n"
                + "        </td>\n"
                + "      </tr>\n";
    }

    // ── Utility helpers ───────────────────────────────────────────────────────

    /** Returns the string or an empty string if null. */
    private static String safe(String s) {
        return s == null ? "" : s;
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private static String formatDate(Date date) {
        if (date == null)
            return "—";
        return new SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH).format(date);
    }
}
