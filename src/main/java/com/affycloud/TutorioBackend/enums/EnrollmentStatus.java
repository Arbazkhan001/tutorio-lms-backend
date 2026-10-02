package com.affycloud.TutorioBackend.enums;

public enum EnrollmentStatus {
    ACTIVE,      // valid, student can access content
    EXPIRED,     // past enrollmentEndAt — kept for history, no content access
    CANCELLED,   // admin cancelled
    REFUNDED     // money returned
}
