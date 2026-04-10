package com.arko.controller.Login;

/**
 * Central lockout policy for failed login attempts (see {@link AuthController}).
 */
public final class AuthLockoutConfig {

    private AuthLockoutConfig() {}

    public static final int MAX_FAILED_ATTEMPTS = 3;
    public static final int LOCK_SECONDS = 30;
}
