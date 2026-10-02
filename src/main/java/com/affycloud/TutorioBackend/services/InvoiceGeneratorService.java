package com.affycloud.TutorioBackend.services;

import com.affycloud.TutorioBackend.models.Order;

/**
 * Service for generating professional invoice content.
 * Produces both an HTML email body and a high-resolution PNG invoice.
 */
public interface InvoiceGeneratorService {

    /**
     * Generates a fully responsive, email-client-safe HTML invoice.
     *
     * @param order the completed order entity
     * @return HTML string ready to be sent as an email body
     */
    String generateEmailHtml(Order order);

    /**
     * Converts the invoice HTML into a high-resolution PNG image.
     *
     * @param order the completed order entity
     * @return PNG image bytes (can be attached to email or served as a download)
     */
    byte[] generateInvoicePng(Order order);
}
