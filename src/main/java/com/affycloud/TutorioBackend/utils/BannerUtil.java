package com.affycloud.TutorioBackend.utils;

import com.affycloud.TutorioBackend.models.Banner;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class BannerUtil {

    /**
     * A banner is visible to students only if it is active AND the days elapsed
     * since its last update are within the admin-configured showDays window.
     */
    public static boolean isBannerVisible(Banner banner) {
        if (banner == null) {
            return false;
        }
        if (banner.getIsActive() == null || !banner.getIsActive()) {
            return false;
        }
        if (banner.getUpdatedAt() == null || banner.getShowDays() == null) {
            return false;
        }
        long daysElapsed = ChronoUnit.DAYS.between(banner.getUpdatedAt().toInstant(), Instant.now());
        return daysElapsed <= banner.getShowDays();
    }

}
