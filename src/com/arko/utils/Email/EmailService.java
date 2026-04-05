package com.arko.utils.Email;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EmailService {

    // Thread pool: 2 daemon threads for background email sends.
    // Daemon threads exit automatically when the JVM shuts down,
    // so they won't prevent application exit.
    private static final ExecutorService EXECUTOR =
            Executors.newFixedThreadPool(2, r -> {
                Thread t = new Thread(r, "arko-email-worker");
                t.setDaemon(true);
                return t;
            });

    // Lazily created SMTP session (thread-safe singleton)
    private static Session mailSession;

    // Utility class - prevent instantiation
    private EmailService() {}


    public static void sendCredentialsEmail(
            String recipientEmail,
            String recipientName,
            String username,
            String temporaryPassword,
            String createdByName,
            boolean isNewAccount,
            EmailCallback callback) {

        EXECUTOR.submit(() -> {
            try {
                Session session = getSession();
                Message  message = buildMessage(
                        session,
                        recipientEmail,
                        recipientName,
                        username,
                        temporaryPassword,
                        createdByName,
                        isNewAccount
                );
                Transport.send(message);

                if (callback != null) {
                    javax.swing.SwingUtilities.invokeLater(() ->
                            callback.onSuccess(recipientEmail));
                }

            } catch (MessagingException e) {
                if (callback != null) {
                    javax.swing.SwingUtilities.invokeLater(() ->
                            callback.onFailure(recipientEmail, e.getMessage()));
                }
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private static synchronized Session getSession() {
        if (mailSession == null) {
            Properties props = new Properties();
            props.put("mail.smtp.host",              EmailConfig.SMTP_HOST);
            props.put("mail.smtp.port",              "587");
            props.put("mail.smtp.auth",              "true");
            props.put("mail.smtp.starttls.enable",   "true");
            props.put("mail.smtp.starttls.required", "true");
            props.put("mail.smtp.ssl.protocols",     "TLSv1.2 TLSv1.3");
            props.put("mail.smtp.ssl.trust",         "smtp.gmail.com");
            props.put("mail.smtp.connectiontimeout", "15000");
            props.put("mail.smtp.timeout",           "15000");
            props.put("mail.smtp.writetimeout",      "15000");

            mailSession = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(
                            EmailConfig.SENDER_EMAIL,
                            EmailConfig.APP_PASSWORD
                    );
                }
            });
        }
        return mailSession;
    }

    private static Properties getProperties() {
        Properties props = new Properties();
        props.put("mail.smtp.host",              EmailConfig.SMTP_HOST);
        props.put("mail.smtp.port",              EmailConfig.SMTP_PORT);
        props.put("mail.smtp.auth", EmailConfig.SMTP_AUTH);
        props.put("mail.smtp.starttls.enable", EmailConfig.SMTP_STARTTLS);
        props.put("mail.smtp.ssl.protocols",     "TLSv1.2");
        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout",           "10000");
        props.put("mail.smtp.writetimeout",      "10000");
        return props;
    }

    // ═════════════════════════════════════════════════════════
    // Message builder
    // ═════════════════════════════════════════════════════════

    private static Message buildMessage(
            Session session,
            String recipientEmail,
            String recipientName,
            String username,
            String temporaryPassword,
            String createdByName,
            boolean isNewAccount) throws MessagingException, UnsupportedEncodingException {

        MimeMessage msg = new MimeMessage(session);

        // From
        msg.setFrom(new InternetAddress(
                EmailConfig.SENDER_EMAIL,
                EmailConfig.SENDER_NAME,
                "UTF-8"
        ));

        // To
        msg.setRecipient(
                Message.RecipientType.TO,
                new InternetAddress(recipientEmail, recipientName, "UTF-8")
        );

        // Subject + date
        String subject = isNewAccount ? EmailConfig.SUBJECT_NEW_ACCOUNT : EmailConfig.SUBJECT_PASSWORD_RESET;
        msg.setSubject(subject, "UTF-8");
        msg.setSentDate(new Date());

        // Body: multipart/alternative — plain text first, HTML preferred
        MimeMultipart multipart = new MimeMultipart("alternative");

        MimeBodyPart textPart = new MimeBodyPart();
        textPart.setText(
                buildPlainText(recipientName, username, temporaryPassword, isNewAccount),
                "UTF-8"
        );
        multipart.addBodyPart(textPart);

        MimeBodyPart htmlPart = new MimeBodyPart();
        htmlPart.setContent(
                buildHtmlBody(recipientName, username, temporaryPassword, createdByName, isNewAccount),
                "text/html; charset=UTF-8"
        );
        multipart.addBodyPart(htmlPart);

        msg.setContent(multipart);
        return msg;
    }

    private static String buildPlainText(
            String name, String username, String tempPassword, boolean isNewAccount) {

        String intro = isNewAccount
                ? "Your account on the A.R.K.O System has been created."
                : "A password reset has been requested for your A.R.K.O account.";

        return (isNewAccount ? "Welcome, " : "Hello, ") + name + "!\n\n"
                + intro + "\n\n"
                + "Your Login Credentials:\n"
                + "Username: " + username + "\n"
                + "Temporary Password: " + tempPassword + "\n\n"
                + "You are required to change this password on your next login.\n"
                + "-- A.R.K.O System";
    }

    private static String buildHtmlBody(
            String name,
            String username,
            String tempPassword,
            String createdByName, boolean isNewAccount) {

        return "<!DOCTYPE html>"
                + "<html lang='en'>"
                + "<head>"
                + "<meta charset='UTF-8'>"
                + "<meta name='viewport' content='width=device-width,initial-scale=1'>"
                + "<title>" + (isNewAccount ? "A.R.K.O Account Created" : "A.R.K.O Password Reset") + "</title>"
                + "</head>"
                + "<body style='"
                +   "margin:0;"
                +   "padding:32px 24px;"
                +   "background:#ffffff;"
                +   "font-family:Arial,\"Segoe UI\",sans-serif;"
                + "'>"

                // ── Greeting ──
                + "<h2 style='"
                +   "margin:0 0 20px;"
                +   "font-size:26px;"
                +   "font-weight:700;"
                +   "color:#111111;"
                + "'>"
                + (isNewAccount ? "Welcome, " : "Hello, ") + escapeHtml(name) + "!"
                + "</h2>"

                // ── Status line ──
                + "<p style='"
                +   "margin:0 0 14px;"
                +   "font-size:15px;"
                +   "line-height:1.6;"
                +   "color:#333333;"
                + "'>"
                + (isNewAccount
                ? "Your account on the <strong>A.R.K.O System</strong> has been created by <strong>" + escapeHtml(createdByName) + "</strong> and is now <strong>active</strong>."
                : "A password reset has been initiated for your account on the <strong>A.R.K.O System</strong>.")
                + "</p>"

                // ── Password change notice ──
                + "<p style='"
                +   "margin:0 0 28px;"
                +   "font-size:15px;"
                +   "line-height:1.6;"
                +   "color:#333333;"
                + "'>"
                + "You will be required to <strong>change your password</strong> "
                + (isNewAccount ? "immediately after your first login." : "on your next login.")
                + "</p>"

                // ── Credentials heading ──
                + "<p style='"
                +   "margin:0 0 8px;"
                +   "font-size:15px;"
                +   "font-weight:700;"
                +   "color:#111111;"
                + "'>"
                + "Your Login Credentials:"
                + "</p>"

                // ── Credentials values ──
                + "<p style='"
                +   "margin:0;"
                +   "font-size:15px;"
                +   "line-height:2.2;"
                +   "color:#333333;"
                + "'>"
                + "Username:&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
                + "<span style='font-family:monospace;color:#111111;font-weight:600;'>"
                + escapeHtml(username)
                + "</span>"
                + "<br>"
                + "Temporary Password:&nbsp;"
                + "<span style='font-family:monospace;color:#111111;font-weight:600;'>"
                + escapeHtml(tempPassword)
                + "</span>"
                + "</p>"

                + "<br><br>"

                // ── Security note ──
                + "<p style='"
                +   "margin:0;"
                +   "font-size:13px;"
                +   "color:#888888;"
                +   "line-height:1.5;"
                + "'>"
                + "Do not share your credentials with anyone. "
                + "If you did not expect this email, please contact your system administrator."
                + "</p>"

                + "<br>"

                // ── Footer ──
                + "<hr style='border:none;border-top:1px solid #eeeeee;margin:0 0 14px;'>"
                + "<p style='"
                +   "margin:0;"
                +   "font-size:12px;"
                +   "color:#aaaaaa;"
                + "'>"
                + "A.R.K.O System"
                + "&nbsp;&bull;&nbsp;"
                + "Automated notification"
                + "&nbsp;&bull;&nbsp;"
                + "Do not reply"
                + "</p>"

                + "</body>"
                + "</html>";
    }

    private static String escapeHtml(String input) {
        if (input == null) return "";
        return input
                .replace("&",  "&amp;")
                .replace("<",  "&lt;")
                .replace(">",  "&gt;")
                .replace("\"", "&quot;")
                .replace("'",  "&#39;");
    }

    public interface EmailCallback {
        void onSuccess(String recipientEmail);

        void onFailure(String recipientEmail, String errorMessage);
    }
}
