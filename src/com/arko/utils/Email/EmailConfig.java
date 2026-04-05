package com.arko.utils.Email;

public class EmailConfig {
    // Sender identity
    public static final String SENDER_EMAIL = "arkosystem2025@gmail.com";  // From address
    public static final String APP_PASSWORD = "edbs wook ieoo zzlu";    // Gmail App Password
    public static final String SENDER_NAME  = "ARKO System";            // Display name

    // SMTP settings (Gmail STARTTLS)
    public static final String SMTP_HOST     = "smtp.gmail.com";
    public static final String SMTP_PORT     = "587";     // STARTTLS port
    public static final String SMTP_AUTH     = "true";
    public static final String SMTP_STARTTLS = "true";

    // Email subject templates
    // In EmailConfig.java
    public static final String SUBJECT_NEW_ACCOUNT = "[A.R.K.O] Your Account Has Been Created!";
    public static final String SUBJECT_PASSWORD_RESET = "[A.R.K.O] Password Reset Request";

    // Utility class — prevent instantiation ─
    private EmailConfig() {}
}
