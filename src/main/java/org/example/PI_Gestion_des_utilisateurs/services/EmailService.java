package org.example.PI_Gestion_des_utilisateurs.services;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class EmailService {

    // Gmail SMTP
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final int SMTP_PORT = 587;

    // Variables d'environnement attendues
    // VIANOVA_SMTP_USER = ton email gmail
    // VIANOVA_SMTP_PASS = mot de passe d'application (16 caractères)
    private final String username;
    private final String appPassword;

    public EmailService() {
        this.username = System.getenv("VIANOVA_SMTP_USER");
        this.appPassword = System.getenv("VIANOVA_SMTP_PASS");
    }

    /** Vérifie si la config SMTP existe */
    public boolean isConfigured() {
        return username != null && !username.isBlank()
                && appPassword != null && !appPassword.isBlank();
    }

    /** Message d'aide si la config n'est pas faite */
    public static String configurationHelp() {
        return """
                SMTP non configuré.
                Définis les variables d'environnement :
                - VIANOVA_SMTP_USER = ton Gmail (ex: xxxx@gmail.com)
                - VIANOVA_SMTP_PASS = mot de passe d'application (16 caractères)
                
                Pour créer un mot de passe d'application :
                1) Active la double authentification (2FA) sur Gmail
                2) Va dans "Mots de passe des applications"
                3) Génère un mot de passe pour "Mail" / "Java"
                """;
    }

    /** Envoi email texte simple */
    public void sendEmail(String to, String subject, String messageText) throws MessagingException {
        validateInputs(to, subject, messageText);

        Session session = createSession();
        Message message = new MimeMessage(session);

        message.setFrom(new InternetAddress(username));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to.trim(), false));
        message.setSubject(subject.trim());
        message.setText(messageText);
        Transport.send(message);
    }

    /** Envoi email HTML (optionnel) */
    public void sendEmailHtml(String to, String subject, String htmlContent) throws MessagingException {
        validateInputs(to, subject, htmlContent);

        Session session = createSession();
        MimeMessage message = new MimeMessage(session);

        message.setFrom(new InternetAddress(username));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to.trim(), false));
        message.setSubject(subject.trim(), StandardCharsets.UTF_8.name());
        message.setContent(htmlContent, "text/html; charset=UTF-8");

        Transport.send(message);
    }

    // -------------------- Helpers --------------------

    private void validateInputs(String to, String subject, String body) throws MessagingException {
        if (!isConfigured()) {
            throw new MessagingException(configurationHelp());
        }
        if (to == null || to.trim().isEmpty()) {
            throw new MessagingException("Destinataire vide");
        }
        if (subject == null || subject.trim().isEmpty()) {
            throw new MessagingException("Sujet vide");
        }
        if (body == null || body.trim().isEmpty()) {
            throw new MessagingException("Message vide");
        }
    }

    private Session createSession() {
        Properties props = new Properties();

        // SMTP + TLS
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", String.valueOf(SMTP_PORT));

        // confiance SSL
        props.put("mail.smtp.ssl.trust", SMTP_HOST);

        // timeouts (important)
        props.put("mail.smtp.connectiontimeout", "8000");
        props.put("mail.smtp.timeout", "8000");
        props.put("mail.smtp.writetimeout", "8000");

        return Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, appPassword);
            }
        });
    }
}