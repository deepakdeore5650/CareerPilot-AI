package com.ai.Resume.analyser;

import com.ai.Resume.analyser.service.securityService;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseCookie;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CookieConfigTest {

    @Test
    void localhostCookiesShouldBeNonSecureAndLax() {
        ResponseCookie cookie = securityService.buildAuthCookie("token123", false, "Lax", false);

        assertFalse(cookie.isSecure());
        assertTrue(cookie.toString().contains("SameSite=Lax"));
    }

    @Test
    void productionHttpsCookiesShouldBeSecureAndCrossSite() {
        ResponseCookie cookie = securityService.buildAuthCookie("token123", true, "None", false);

        assertTrue(cookie.isSecure());
        assertTrue(cookie.toString().contains("SameSite=None"));
    }
}
