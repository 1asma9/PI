package org.example.PI_Gestion_des_utilisateurs.services;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;


public class EmailService {

    private static final String SMTP_USERNAME = "elarbiahmed0@gmail.com";

    private static final String SMTP_PASSWORD = "uqoi vieu mlox ubfs"; // ⚠️ Vérifier que c'est bien un mot de passe d'application Gmail valide
    
    // Configuration Gmail SMTP
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final int SMTP_PORT = 587;
    
    // ===== FIN CONFIGURATION =====
    
    /**
     * Envoie un email via SMTP
     * @param to Adresse email du destinataire
     * @param subject Sujet de l'email
     * @param messageText Contenu du message
     * @throws MessagingException En cas d'erreur lors de l'envoi
     */
    public void sendEmail(String to, String subject, String messageText) throws MessagingException {
        // 🔍 DIAGNOSTIC - Paramètres reçus
        System.out.println("🔍 DEBUG EmailService - to: [" + to + "]");
        System.out.println("🔍 DEBUG EmailService - subject: [" + subject + "]");
        System.out.println("🔍 DEBUG EmailService - messageText length: " + (messageText != null ? messageText.length() : 0));
        
        // Validation des entrées
        if (to == null || to.trim().isEmpty()) {
            throw new MessagingException("L'adresse email du destinataire ne peut pas être vide");
        }
        if (subject == null || subject.trim().isEmpty()) {
            throw new MessagingException("Le sujet de l'email ne peut pas être vide");
        }
        if (messageText == null || messageText.trim().isEmpty()) {
            throw new MessagingException("Le contenu de l'email ne peut pas être vide");
        }
        
        // 🔍 DIAGNOSTIC - Après validation
        String toTrimmed = to.trim();
        System.out.println("🔍 DEBUG EmailService - to final: [" + toTrimmed + "]");
        System.out.println("🔍 DEBUG EmailService - to final length: " + toTrimmed.length());
        
        // Configuration des propriétés SMTP
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", String.valueOf(SMTP_PORT));
        props.put("mail.smtp.ssl.trust", SMTP_HOST);
        props.put("mail.debug", "true"); // ✅ Debug activé pour diagnostiquer
        
        // Création de la session avec authentification
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SMTP_USERNAME, SMTP_PASSWORD);
            }
        });
        
        try {
            // Création du message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(SMTP_USERNAME));
            
            // 🔍 DIAGNOSTIC - Configuration du destinataire
            System.out.println("🔍 DEBUG EmailService - Configuration destinataire avec: [" + toTrimmed + "]");
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toTrimmed));
            
            // 🔍 DIAGNOSTIC - Vérification des destinataires
            Address[] recipients = message.getRecipients(Message.RecipientType.TO);
            if (recipients != null) {
                for (Address addr : recipients) {
                    System.out.println("🔍 DEBUG EmailService - Destinataire configuré: [" + addr.toString() + "]");
                }
            } else {
                System.out.println("🔍 DEBUG EmailService - AUCUN destinataire configuré !");
            }
            
            message.setSubject(subject);
            message.setText(messageText);  // ✅ FORMAT TEXTE SIMPLE (text/plain)
            
            System.out.println("📧 Tentative d'envoi d'email à: " + toTrimmed);
            System.out.println("📧 Sujet: " + subject);
            System.out.println("📧 Serveur SMTP: " + SMTP_HOST + ":" + SMTP_PORT);
            
            // Envoi du message
            Transport.send(message);
            
            System.out.println("✅ Email envoyé avec succès à: " + toTrimmed);
            
        } catch (MessagingException e) {
            System.err.println("❌ Erreur détaillée lors de l'envoi de l'email: " + e.getMessage());
            System.err.println("❌ Code d'erreur: " + e.getClass().getSimpleName());
            
            // Afficher les causes possibles
            if (e.getMessage().contains("535-5.7.8")) {
                System.err.println("❌ Cause probable: Nom d'utilisateur ou mot de passe incorrect");
                System.err.println("❌ Solution: Vérifier que le mot de passe d'application Gmail est correct");
            } else if (e.getMessage().contains("530-5.7.0")) {
                System.err.println("❌ Cause probable: Authentification requise ou TLS désactivé");
                System.err.println("❌ Solution: Activer l'authentification 2 facteurs et générer un mot de passe d'application");
            } else if (e.getMessage().contains("timeout") || e.getMessage().contains("connection")) {
                System.err.println("❌ Cause probable: Problème de connexion réseau ou firewall");
                System.err.println("❌ Solution: Vérifier la connexion internet et les paramètres firewall");
            }
            
            e.printStackTrace();
            throw e;
        }
    }
    
    /**
     * Vérifie si la configuration SMTP est valide
     * @return true si les identifiants semblent configurés, false sinon
     */
    public static boolean isConfigurationValide() {
        return !SMTP_USERNAME.equals("votre.email.personnel@gmail.com") 
               && !SMTP_PASSWORD.equals("VOTRE_APP_PASSWORD_16_CARACTERES");
    }
    
    /**
     * Affiche les instructions de configuration
     */
    public static void afficherInstructionsConfiguration() {
        System.out.println("=== CONFIGURATION EMAIL SERVICE ===");
        System.out.println("Pour configurer l'envoi d'emails :");
        System.out.println("1. Activez l'authentification 2 facteurs sur votre compte Gmail");
        System.out.println("2. Allez sur : https://myaccount.google.com/apppasswords");
        System.out.println("3. Générez un mot de passe d'application pour 'JavaMail'");
        System.out.println("4. Copiez ce mot de passe (16 caractères)");
        System.out.println("5. Modifiez les constantes SMTP_USERNAME et SMTP_PASSWORD dans EmailService.java");
        System.out.println("=====================================");
    }
}
