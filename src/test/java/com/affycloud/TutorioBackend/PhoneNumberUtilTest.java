package com.affycloud.TutorioBackend;

import com.affycloud.TutorioBackend.utils.PhoneNumberUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Normalization is what makes the application-level uniqueness check and the
 * {@code uq_student_tenant_phone} index agree on what "the same number" means.
 */
class PhoneNumberUtilTest {

    @Test
    @DisplayName("formatting variants of one number collapse to a single stored value")
    void formattingVariantsCollapse() {
        String canonical = PhoneNumberUtil.normalize("8269411131");

        assertThat(PhoneNumberUtil.normalize(" 8269411131 ")).isEqualTo(canonical);
        assertThat(PhoneNumberUtil.normalize("826 941 1131")).isEqualTo(canonical);
        assertThat(PhoneNumberUtil.normalize("826-941-1131")).isEqualTo(canonical);
        assertThat(PhoneNumberUtil.normalize("(826) 941.1131")).isEqualTo(canonical);
    }

    @Test
    @DisplayName("a leading + is kept, so +91... and 91... stay distinct values")
    void leadingPlusIsPreserved() {
        assertThat(PhoneNumberUtil.normalize("+91 8269411131")).isEqualTo("+918269411131");
        assertThat(PhoneNumberUtil.normalize("91 8269411131")).isEqualTo("918269411131");
    }

    @Test
    @DisplayName("blank input collapses to null, never to an empty string")
    void blankCollapsesToNull() {
        /* "" is a real value under a MySQL unique index: only one student per
           tenant could ever hold it, and the second one failed with a raw
           duplicate-key error. It also made `phoneNumber != null` report an
           incomplete profile as complete. */
        assertThat(PhoneNumberUtil.normalize(null)).isNull();
        assertThat(PhoneNumberUtil.normalize("")).isNull();
        assertThat(PhoneNumberUtil.normalize("   ")).isNull();
        assertThat(PhoneNumberUtil.normalize("--")).isNull();
        assertThat(PhoneNumberUtil.isBlank(" ")).isTrue();
        assertThat(PhoneNumberUtil.isBlank("8269411131")).isFalse();
    }

    @Test
    @DisplayName("leading zeros are part of the number and are not stripped")
    void leadingZerosSurvive() {
        assertThat(PhoneNumberUtil.normalize("0 826 941 1131")).isEqualTo("08269411131");
    }
}
