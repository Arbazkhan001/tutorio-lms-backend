package com.affycloud.TutorioBackend.utils;

/**
 * Single source of truth for phone-number normalization.
 *
 * <p>Phone numbers take part in the {@code uq_student_tenant_phone} unique
 * constraint, so every code path that stores or compares a phone number must
 * normalize it the same way. Otherwise "+91 8269411131" and "+918269411131"
 * are treated as two different numbers by the uniqueness checks but collide
 * once one of them is sanitized.
 */
public final class PhoneNumberUtil {

    private PhoneNumberUtil() {
    }

    /**
     * Canonical form of a phone number, or {@code null} when the input carries
     * no number at all.
     *
     * <p>Separators a phone keypad or a paste can introduce (spaces, dashes,
     * brackets, dots) are removed; a leading {@code +} is kept because some
     * historical rows store the country code inside the number itself.
     *
     * <p>Blank input deliberately collapses to {@code null} rather than
     * {@code ""}. MySQL's unique index treats {@code NULL} as "no value" and
     * allows any number of them, but {@code ''} is an ordinary value — so a
     * single tenant can only ever hold one student with an empty string, and
     * the second one to be saved fails with a raw "Duplicate entry" error.
     * An empty string also makes the {@code phoneNumber != null} profile-setup
     * check believe the profile is complete when it is not.
     */
    public static String normalize(String phone) {
        if (phone == null) {
            return null;
        }
        String trimmed = phone.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        String digits = trimmed.replaceAll("\\D", "");
        if (digits.isEmpty()) {
            return null;
        }
        return trimmed.startsWith("+") ? "+" + digits : digits;
    }

    /**
     * @deprecated retained for existing call sites; delegates to
     *             {@link #normalize(String)}.
     */
    @Deprecated
    public static String sanitize(String phone) {
        return normalize(phone);
    }

    /** {@code true} when the value carries no usable phone number. */
    public static boolean isBlank(String phone) {
        return normalize(phone) == null;
    }
}
