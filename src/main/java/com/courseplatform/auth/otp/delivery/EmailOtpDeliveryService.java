package com.courseplatform.auth.otp.delivery;

import com.courseplatform.auth.otp.OtpChannel;
import com.courseplatform.auth.otp.OtpPurpose;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Sends OTP verification emails via the Resend HTTP API (https://api.resend.com/emails).
 *
 * Why HTTP API instead of SMTP?
 * Railway (and many cloud platforms) block outbound SMTP ports (25, 465, 587) to prevent spam.
 * The Resend HTTP API uses HTTPS (port 443), which is always allowed.
 *
 * Required environment variable on Railway:
 *   RESEND_API_KEY = re_XXXX...  (your Resend API key)
 *
 * Optional:
 *   RESEND_FROM_EMAIL = noreply@adfixstudio.com  (defaults to noreply@adfixstudio.com)
 */
@Service
public class EmailOtpDeliveryService implements OtpDeliveryService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(EmailOtpDeliveryService.class);
    private static final String RESEND_API_URL = "https://api.resend.com/emails";

    private final Environment environment;
    private final HttpClient httpClient;

    public EmailOtpDeliveryService(Environment environment) {
        this.environment = environment;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    @Override
    public OtpChannel getChannel() {
        return OtpChannel.EMAIL;
    }

    @Override
    public void deliver(String recipient, String rawOtp, OtpPurpose purpose) {
        // Resolve Resend API key — try RESEND_API_KEY first, then EMAIL_PROVIDER_API_KEY, fall back to SPRING_MAIL_PASSWORD
        String apiKey = environment.getProperty("RESEND_API_KEY");
        if (!StringUtils.hasText(apiKey)) {
            apiKey = environment.getProperty("EMAIL_PROVIDER_API_KEY");
        }
        if (!StringUtils.hasText(apiKey)) {
            apiKey = environment.getProperty("spring.mail.password");
        }

        // Resolve from address
        String fromEmail = environment.getProperty("RESEND_FROM_EMAIL");
        if (!StringUtils.hasText(fromEmail)) {
            fromEmail = environment.getProperty("spring.mail.from");
        }
        if (!StringUtils.hasText(fromEmail) || !fromEmail.contains("@")) {
            fromEmail = "noreply@adfixstudio.com";
        }

        boolean isTest = java.util.Arrays.asList(environment.getActiveProfiles()).contains("test");

        // Always log OTP for observability & emergency fallback
        log.info("[OTP DELIVERY - EMAIL] Dispatched {} code to recipient: {} [Code: {}] (From: {})",
                purpose, maskEmail(recipient), rawOtp, fromEmail);

        if (isTest) {
            log.debug("Skipping live email delivery (test profile active).");
            return;
        }

        if (!StringUtils.hasText(apiKey)) {
            log.warn("No Resend API key configured (RESEND_API_KEY or spring.mail.password). Skipping email delivery.");
            return;
        }

        String htmlContent = buildEmailTemplate(rawOtp, purpose);
        String subjectLine = "Your AdFix Studio Verification Code: " + rawOtp;

        // Build JSON payload for Resend API
        String jsonPayload = """
            {
              "from": "%s",
              "to": ["%s"],
              "subject": "%s",
              "html": %s
            }
            """.formatted(
                "AdFix Studio <" + fromEmail + ">",
                escapeJson(recipient),
                escapeJson(subjectLine),
                toJsonString(htmlContent)
        );

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(RESEND_API_URL))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .timeout(Duration.ofSeconds(15))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                log.info("[OTP DELIVERY - EMAIL] Email sent successfully via Resend API to {} (HTTP {})",
                        maskEmail(recipient), response.statusCode());
            } else {
                log.error("[OTP DELIVERY - EMAIL] Resend API returned HTTP {}: {} | Recipient: {}",
                        response.statusCode(), response.body(), maskEmail(recipient));
            }
        } catch (Exception ex) {
            log.error("[OTP DELIVERY - EMAIL] Failed to send email via Resend API to {}: {}",
                    maskEmail(recipient), ex.getMessage());
        }
    }

    private String buildEmailTemplate(String otp, OtpPurpose purpose) {
        String purposeTitle = purpose == OtpPurpose.REGISTRATION ? "Verify Your Account" : "Security Verification";
        return """
            <!DOCTYPE html>
            <html>
            <head>
              <meta charset="UTF-8">
              <style>
                body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; background-color: #0c0d0e; color: #f3f4f6; margin: 0; padding: 24px; }
                .container { max-width: 520px; margin: 0 auto; background-color: #16181a; border: 1px solid #27272a; border-radius: 16px; padding: 32px; }
                .logo { font-size: 20px; font-weight: 800; color: #f97316; letter-spacing: 1px; text-transform: uppercase; margin-bottom: 24px; }
                h1 { font-size: 22px; color: #ffffff; margin-top: 0; }
                p { font-size: 14px; line-height: 1.6; color: #a1a1aa; }
                .otp-box { background-color: #0c0d0e; border: 2px dashed #f97316; border-radius: 12px; text-align: center; padding: 20px; margin: 28px 0; }
                .otp-code { font-size: 36px; font-weight: 900; letter-spacing: 8px; color: #f97316; font-family: monospace; }
                .footer { font-size: 12px; color: #71717a; text-align: center; margin-top: 32px; border-top: 1px solid #27272a; padding-top: 16px; }
              </style>
            </head>
            <body>
              <div class="container">
                <div class="logo">AdFix Studio</div>
                <h1>%s</h1>
                <p>Hello,</p>
                <p>Please use the following single-use verification code to complete your verification request. This code is valid for <strong>5 minutes</strong>.</p>
                <div class="otp-box">
                  <div class="otp-code">%s</div>
                </div>
                <p>If you did not request this verification code, please ignore this email or contact support if you suspect unauthorized activity.</p>
                <div class="footer">
                  &copy; %d AdFix Studio. All rights reserved. Secure Learning Platform.
                </div>
              </div>
            </body>
            </html>
            """.formatted(purposeTitle, otp, java.time.Year.now().getValue());
    }

    /** Escape special JSON characters in a string value. */
    private String escapeJson(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    /** Convert a string to a JSON-encoded string literal (with surrounding quotes). */
    private String toJsonString(String value) {
        if (value == null) return "null";
        return "\"" + value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t")
                + "\"";
    }

    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "***";
        }
        int atIndex = email.indexOf('@');
        String name = email.substring(0, atIndex);
        String domain = email.substring(atIndex);
        String maskedName = name.length() <= 2 ? name.charAt(0) + "*" : name.substring(0, 2) + "***";
        return maskedName + domain;
    }
}

