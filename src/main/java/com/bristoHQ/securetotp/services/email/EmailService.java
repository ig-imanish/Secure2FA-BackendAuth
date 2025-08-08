package com.bristoHQ.securetotp.services.email;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${frontend.url}")
    private String frontendUrl;

    public void sendOtpEmail(String to, String otp) {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        String htmlContent = "<!DOCTYPE html>" +
                "<html lang='en'>" +
                "<head>" +
                "<meta charset='UTF-8'>" +
                "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                "<title>OTP Verification</title>" +
                "<style>" +
                "body { font-family: 'Helvetica Neue', Arial, sans-serif; background-color: #f5f7fa; margin: 0; padding: 0; }"
                +
                ".container { max-width: 600px; margin: 40px auto; background: #ffffff; border-radius: 8px; overflow: hidden; box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1); }"
                +
                ".header { background: #1a73e8; padding: 20px; text-align: center; }" +
                ".header img { max-width: 120px; height: auto; }" +
                ".content { padding: 30px; text-align: center; }" +
                ".otp-box { font-size: 28px; font-weight: 600; color: #1a73e8; padding: 15px 20px; border: 2px solid #1a73e8; border-radius: 6px; display: inline-block; margin: 20px 0; letter-spacing: 4px; background: #f0f4ff; }"
                +
                "h1 { font-size: 24px; color: #202124; margin: 0 0 20px; }" +
                "p { font-size: 16px; color: #4a4a4a; line-height: 1.6; margin: 0 0 15px; }" +
                ".footer { font-size: 13px; color: #6b7280; padding: 20px; text-align: center; background: #f8fafc; }" +
                ".footer a { color: #1a73e8; text-decoration: none; }" +
                "@media screen and (max-width: 600px) { .container { margin: 20px; padding: 15px; } .header img { max-width: 100px; } }"
                +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='container'>" +
                "<div class='header'>" +
                "<img src='https://avatars.githubusercontent.com/u/215977659?s=400&u=d302d5d2bb6de369131ec257dbbec654eafda160&v=4' alt='BristoHQ Logo'>"
                +
                "</div>" +
                "<div class='content'>" +
                "<h1>OTP Verification</h1>" +
                "<p>Your One-Time Password (OTP) for secure verification is:</p>" +
                "<div class='otp-box'>" + otp + "</div>" +
                "<p>This OTP is valid for 10 minutes. For your security, please do not share it with anyone.</p>" +
                "<p>If you did not request this OTP, please contact our support team at <a href='mailto:bristohq@gmail.com'>bristohq@gmail.com</a>.</p>"
                +
                "</div>" +
                "<div class='footer'>" +
                "&copy; " + java.time.Year.now() + " BristoHQ. All rights reserved. | <a href='" + frontendUrl
                + "/privacy'>Privacy Policy</a>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";

        try {
            helper.setTo(to);
            helper.setSubject("Your OTP Code - BristoHQ");
            helper.setText(htmlContent, true);
        } catch (Exception e) {
            System.out.println("Error in sendOtpEmail: ");
            e.printStackTrace();
        }
        mailSender.send(message);
    }

    public void sendResetPasswordEmail(String email, String token) {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        String resetLink = frontendUrl + "/forgot-password?token=" + token;

        String htmlContent = "<!DOCTYPE html>" +
                "<html lang='en'>" +
                "<head>" +
                "<meta charset='UTF-8'>" +
                "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                "<title>Reset Your Password</title>" +
                "<style>" +
                "body { font-family: 'Helvetica Neue', Arial, sans-serif; background-color: #f5f7fa; margin: 0; padding: 0; }"
                +
                ".container { max-width: 600px; margin: 40px auto; background: #ffffff; border-radius: 8px; overflow: hidden; box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1); }"
                +
                ".header { background: #1a73e8; padding: 20px; text-align: center; }" +
                ".header img { max-width: 120px; height: auto; }" +
                ".content { padding: 30px; text-align: center; }" +
                "h1 { font-size: 24px; color: #202124; margin: 0 0 20px; }" +
                "p { font-size: 16px; color: #4a4a4a; line-height: 1.6; margin: 0 0 15px; }" +
                ".button { display: inline-block; background: #1a73e8; color: #ffffff; text-decoration: none; padding: 12px 24px; border-radius: 6px; font-size: 16px; font-weight: 500; margin: 20px 0; }"
                +
                ".button:hover { background: #1557b0; }" +
                ".footer { font-size: 13px; color: #6b7280; padding: 20px; text-align: center; background: #f8fafc; }" +
                ".footer a { color: #1a73e8; text-decoration: none; }" +
                "@media screen and (max-width: 600px) { .container { margin: 20px; padding: 15px; } .header img { max-width: 100px; } }"
                +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='container'>" +
                "<div class='header'>" +
                "<img src='https://avatars.githubusercontent.com/u/215977659?s=400&u=d302d5d2bb6de369131ec257dbbec654eafda160&v=4' alt='BristoHQ Logo'>"
                +
                "</div>" +
                "<div class='content'>" +
                "<h1>Reset Your Password</h1>" +
                "<p>We received a request to reset your password. Click the button below to set a new password:</p>" +
                "<a class='button' href='" + resetLink + "'>Reset Password</a>" +
                "<p>This link will expire in 30 minutes for your security. If you did not request a password reset, please contact our support team at <a href='mailto:bristohq@gmail.com'>bristohq@gmail.com</a>.</p>"
                +
                "</div>" +
                "<div class='footer'>" +
                "&copy; " + java.time.Year.now() + " BristoHQ. All rights reserved. | <a href='" + frontendUrl
                + "/privacy'>Privacy Policy</a>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";

        try {
            helper.setTo(email);
            helper.setSubject("Reset Your Password - BristoHQ");
            helper.setText(htmlContent, true);
        } catch (Exception e) {
            System.out.println("Error in sendResetPasswordEmail: ");
            e.printStackTrace();
        }
        mailSender.send(message);
    }

    public void welcomeEmailToNewUser(String email) {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        String htmlContent = "<!DOCTYPE html>" +
                "<html lang='en'>" +
                "<head>" +
                "<meta charset='UTF-8'>" +
                "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                "<title>Welcome to BristoHQ</title>" +
                "<style>" +
                "body { font-family: 'Helvetica Neue', Arial, sans-serif; background-color: #f5f7fa; margin: 0; padding: 0; }"
                +
                ".container { max-width: 600px; margin: 40px auto; background: #ffffff; border-radius: 8px; overflow: hidden; box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1); }"
                +
                ".header { background: #1a73e8; padding: 20px; text-align: center; }" +
                ".header img { max-width: 120px; height: auto; }" +
                ".content { padding: 30px; text-align: center; }" +
                "h1 { font-size: 24px; color: #202124; margin: 0 0 20px; }" +
                "p { font-size: 16px; color: #4a4a4a; line-height: 1.6; margin: 0 0 15px; }" +
                ".button { display: inline-block; background: #1a73e8; color: #ffffff; text-decoration: none; padding: 12px 24px; border-radius: 6px; font-size: 16px; font-weight: 500; margin: 20px 0; }"
                +
                ".button:hover { background: #1557b0; }" +
                ".footer { font-size: 13px; color: #6b7280; padding: 20px; text-align: center; background: #f8fafc; }" +
                ".footer a { color: #1a73e8; text-decoration: none; }" +
                "@media screen and (max-width: 600px) { .container { margin: 20px; padding: 15px; } .header img { max-width: 100px; } }"
                +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='container'>" +
                "<div class='header'>" +
                "<img src='https://avatars.githubusercontent.com/u/215977659?s=400&u=d302d5d2bb6de369131ec257dbbec654eafda160&v=4' alt='BristoHQ Logo'>"
                +
                "</div>" +
                "<div class='content'>" +
                "<h1>Welcome to BristoHQ!</h1>" +
                "<p>Hello,<br>Thank you for joining BristoHQ! We're excited to have you as part of our community. Get started by exploring our platform and discovering all the amazing features we offer.</p>"
                +
                "<a class='button' href='" + frontendUrl + "'>Get Started</a>" +
                "<p>Need assistance? Reach out to our support team at <a href='mailto:bristohq@gmail.com'>bristohq@gmail.com</a> anytime.<br><strong>Happy exploring!</strong></p>"
                +
                "</div>" +
                "<div class='footer'>" +
                "&copy; " + java.time.Year.now() + " BristoHQ. All rights reserved. | <a href='" + frontendUrl
                + "/privacy'>Privacy Policy</a>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";

        try {
            helper.setTo(email);
            helper.setSubject("Welcome to the BristoHQ Family!");
            helper.setText(htmlContent, true);
        } catch (Exception e) {
            System.out.println("Error in welcomeEmailToNewUser: ");
            e.printStackTrace();
        }
        mailSender.send(message);
    }
}