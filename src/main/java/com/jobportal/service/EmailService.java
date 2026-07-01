package com.jobportal.service;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    // ─────────────────────────────────────────
    // 1. New Message Notification
    // ─────────────────────────────────────────
    @Async
    public void sendNewMessageEmail(String toEmail, String recipientName, String senderName, String messagePreview) {
        try {
            String html = buildMessageEmailHtml(recipientName, senderName, messagePreview);
            sendHtmlEmail(toEmail, "💬 New Message from " + senderName + " – CareerConnect", html);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ─────────────────────────────────────────
    // 2. New Application Notification (to Employer)
    // ─────────────────────────────────────────
    @Async
    public void sendNewApplicationEmail(String toEmail, String employerName, String applicantName, String jobTitle) {
        try {
            String html = buildNewApplicationEmailHtml(employerName, applicantName, jobTitle);
            sendHtmlEmail(toEmail, "🎯 New Application for " + jobTitle + " – CareerConnect", html);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ─────────────────────────────────────────
    // 3. Application Status Update (to Applicant)
    // ─────────────────────────────────────────
    @Async
    public void sendStatusUpdateEmail(String toEmail, String applicantName, String jobTitle, String newStatus) {
        try {
            String html = buildStatusUpdateEmailHtml(applicantName, jobTitle, newStatus);
            sendHtmlEmail(toEmail, getStatusEmailSubject(newStatus) + " – CareerConnect", html);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ─────────────────────────────────────────
    // Core mail sender
    // ─────────────────────────────────────────
    private void sendHtmlEmail(String toEmail, String subject, String htmlBody) throws Exception {
        MimeMessage mm = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mm, true, "UTF-8");
        helper.setFrom(fromEmail);
        helper.setTo(toEmail);
        helper.setSubject(subject);
        helper.setText(htmlBody, true);
        mailSender.send(mm);
    }

    // ─────────────────────────────────────────
    // Helper: Subject line per status
    // ─────────────────────────────────────────
    private String getStatusEmailSubject(String status) {
        switch (status) {
            case "INTERVIEWING": return "🗓️ Interview Scheduled";
            case "OFFERED":      return "🎉 Congratulations! You Got an Offer";
            case "REJECTED":     return "📋 Application Update";
            default:             return "📋 Your Application Status Has Changed";
        }
    }

    // ─────────────────────────────────────────
    // HTML Builders
    // ─────────────────────────────────────────
    private String buildMessageEmailHtml(String recipientName, String senderName, String messagePreview) {
        String highlight = "<div style='background:#1e1e2e;border-left:4px solid #ffbd20;padding:16px 20px;"
                + "border-radius:8px;margin:20px 0;font-style:italic;color:#ccc;'>" + messagePreview + "</div>";
        return baseTemplate(
            "💬 New Message",
            "Hi " + recipientName + ",",
            "<b>" + senderName + "</b> sent you a message on CareerConnect:",
            highlight,
            "Reply Now",
            frontendUrl + "/messages"
        );
    }

    private String buildNewApplicationEmailHtml(String employerName, String applicantName, String jobTitle) {
        String highlight = "<div style='background:#1e1e2e;border-left:4px solid #ffbd20;padding:16px 20px;"
                + "border-radius:8px;margin:20px 0;font-size:18px;font-weight:bold;color:#ffbd20;'>" + jobTitle + "</div>";
        return baseTemplate(
            "🎯 New Application Received",
            "Hi " + employerName + ",",
            "Great news! <b>" + applicantName + "</b> has applied for your job posting:",
            highlight,
            "View Application",
            frontendUrl + "/posted-jobs/0"
        );
    }

    private String buildStatusUpdateEmailHtml(String applicantName, String jobTitle, String status) {
        String statusColor;
        String statusEmoji;
        String bodyText;

        switch (status) {
            case "INTERVIEWING":
                statusColor = "#3b82f6";
                statusEmoji = "🗓️ Interview Scheduled";
                bodyText = "Congratulations! The employer has reviewed your application and would like to schedule an interview. Check your Job History for the interview details.";
                break;
            case "OFFERED":
                statusColor = "#22c55e";
                statusEmoji = "🎉 Offer Extended";
                bodyText = "Incredible news! The employer has extended a formal offer to you. Log in to CareerConnect to view all the details.";
                break;
            case "REJECTED":
                statusColor = "#ef4444";
                statusEmoji = "📋 Application Reviewed";
                bodyText = "Thank you for your interest. After careful consideration, the employer has decided to move forward with other candidates. Don't give up — new opportunities await!";
                break;
            default:
                statusColor = "#ffbd20";
                statusEmoji = "📋 Status Updated";
                bodyText = "Your application status has been updated. Log in to see more details.";
        }

        String highlight = "<div style='background:#1e1e2e;border-left:4px solid " + statusColor
                + ";padding:16px 20px;border-radius:8px;margin:20px 0;font-size:18px;font-weight:bold;color:"
                + statusColor + ";'>" + jobTitle + "</div>";

        return baseTemplate(
            statusEmoji,
            "Hi " + applicantName + ",",
            bodyText,
            highlight,
            "View Job History",
            frontendUrl + "/job-history"
        );
    }

    private String baseTemplate(String title, String greeting, String bodyText,
                                String highlightBlock, String ctaText, String ctaUrl) {
        return "<!DOCTYPE html><html lang='en'><head><meta charset='UTF-8'>"
            + "<meta name='viewport' content='width=device-width,initial-scale=1.0'></head>"
            + "<body style='margin:0;padding:0;background-color:#0b0e14;font-family:Segoe UI,Arial,sans-serif;'>"
            + "<table width='100%' cellpadding='0' cellspacing='0' style='background:#0b0e14;padding:40px 20px;'>"
            + "<tr><td align='center'>"
            + "<table width='600' cellpadding='0' cellspacing='0' style='max-width:600px;width:100%;'>"

            // Header
            + "<tr><td style='background:linear-gradient(135deg,#1a1d2e 0%,#12151f 100%);"
            + "border-radius:16px 16px 0 0;padding:32px 40px;border-bottom:2px solid #ffbd20;text-align:center;'>"
            + "<div style='font-size:28px;font-weight:700;color:#ffbd20;letter-spacing:-0.5px;'>CareerConnect</div>"
            + "<div style='font-size:13px;color:#6b7280;margin-top:4px;'>Your Professional Journey</div>"
            + "</td></tr>"

            // Body
            + "<tr><td style='background:#12151f;padding:40px;border-left:1px solid #1f2937;border-right:1px solid #1f2937;'>"
            + "<h1 style='color:#f9fafb;font-size:22px;font-weight:700;margin:0 0 8px 0;'>" + title + "</h1>"
            + "<p style='color:#9ca3af;font-size:15px;margin:0 0 20px 0;'>" + greeting + "</p>"
            + "<p style='color:#d1d5db;font-size:15px;line-height:1.7;margin:0 0 8px 0;'>" + bodyText + "</p>"
            + highlightBlock
            + "<div style='text-align:center;margin-top:32px;'>"
            + "<a href='" + ctaUrl + "' style='display:inline-block;background:#ffbd20;color:#0b0e14;"
            + "font-weight:700;font-size:15px;padding:14px 32px;border-radius:8px;text-decoration:none;"
            + "letter-spacing:0.3px;'>" + ctaText + " &rarr;</a></div>"
            + "</td></tr>"

            // Footer
            + "<tr><td style='background:#0d1017;border-radius:0 0 16px 16px;padding:24px 40px;"
            + "border:1px solid #1f2937;border-top:none;text-align:center;'>"
            + "<p style='color:#4b5563;font-size:12px;margin:0;'>You received this email because you have an account on CareerConnect.<br>"
            + "Please do not reply to this email.</p>"
            + "</td></tr>"

            + "</table></td></tr></table></body></html>";
    }
}
