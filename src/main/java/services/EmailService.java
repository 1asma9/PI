package services;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;
import java.util.Date;

public class EmailService {

    // Configuration Gmail
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";
    private static final String EMAIL_FROM = "rayenhafian72@gmail.com";

    // Mot de passe d'application fourni par l'utilisateur
    private static final String EMAIL_PASSWORD = "slmf dpdo pvgo mqrd";

    private Session session;

    public EmailService() {
        // Configuration des propriétés SMTP
        Properties props = new Properties();
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");
        props.put("mail.smtp.ssl.trust", SMTP_HOST);

        // Authentification
        this.session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(EMAIL_FROM, EMAIL_PASSWORD);
            }
        });
    }

    /**
     * Envoyer un email simple
     */
    public boolean envoyerEmail(String destinataire, String sujet, String contenu) {
        try {
            Message message = new MimeMessage(session);

            // Expéditeur
            message.setFrom(new InternetAddress(EMAIL_FROM, "Gestion Réclamations & Avis"));

            // Destinataire
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(destinataire));

            // Sujet
            message.setSubject(sujet);

            // Contenu (texte)
            message.setText(contenu);

            // Date
            message.setSentDate(new Date());

            // Envoi
            Transport.send(message);

            System.out.println("✅ Email envoyé avec succès à : " + destinataire);
            return true;

        } catch (Exception e) {
            System.err.println("❌ Erreur envoi email : " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Envoyer un email HTML (plus joli)
     */
    public boolean envoyerEmailHTML(String destinataire, String sujet, String contenuHTML) {
        try {
            Message message = new MimeMessage(session);

            message.setFrom(new InternetAddress(EMAIL_FROM, "Gestion Réclamations & Avis"));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(destinataire));
            message.setSubject(sujet);

            // Contenu HTML
            message.setContent(contenuHTML, "text/html; charset=utf-8");
            message.setSentDate(new Date());

            Transport.send(message);

            System.out.println("✅ Email HTML envoyé avec succès à : " + destinataire);
            return true;

        } catch (Exception e) {
            System.err.println("❌ Erreur envoi email HTML : " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * EMAIL : Nouvelle réclamation créée
     */
    public void envoyerEmailNouvelleReclamation(String titre, String description) {
        String destinataire = "rayenhafian72@gmail.com";
        String sujet = "✉️ Nouvelle Réclamation Créée";

        String contenu = String.format(
                "Bonjour,\n\n" +
                        "Une nouvelle réclamation vient d'être créée :\n\n" +
                        "📋 Titre : %s\n\n" +
                        "📝 Description :\n%s\n\n" +
                        "──────────────────────────────\n" +
                        "Date : %s\n" +
                        "Statut : En attente\n\n" +
                        "Veuillez vous connecter à la plateforme pour la traiter.\n\n" +
                        "Cordialement,\n" +
                        "Système de Gestion",
                titre,
                description,
                new java.text.SimpleDateFormat("dd/MM/yyyy à HH:mm").format(new Date()));

        envoyerEmail(destinataire, sujet, contenu);
    }

    /**
     * EMAIL : Nouvelle réclamation créée (VERSION HTML)
     */
    public void envoyerEmailNouvelleReclamationHTML(String titre, String description) {
        String destinataire = "rayenhafian72@gmail.com";
        String sujet = "✉️ Nouvelle Réclamation Créée";

        String contenuHTML = String.format(
                "<html>" +
                        "<body style='font-family: Arial, sans-serif; background-color: #f6f1e7; padding: 20px;'>" +
                        "  <div style='max-width: 600px; margin: 0 auto; background-color: white; padding: 30px; border-radius: 20px; box-shadow: 0 4px 12px rgba(0,0,0,0.1);'>"
                        +
                        "    <h2 style='color: #0f2a2a; border-bottom: 3px solid #c9a24a; padding-bottom: 10px;'>📋 Nouvelle Réclamation</h2>"
                        +
                        "    " +
                        "    <div style='background-color: #fff4df; padding: 15px; border-radius: 10px; margin: 20px 0;'>"
                        +
                        "      <p style='margin: 0; color: #9a6a11; font-weight: bold;'>⚠️ En attente de traitement</p>"
                        +
                        "    </div>" +
                        "    " +
                        "    <div style='margin: 20px 0;'>" +
                        "      <p style='font-weight: bold; color: #0f2a2a; margin-bottom: 5px;'>Titre :</p>" +
                        "      <p style='background-color: #faf6ef; padding: 10px; border-radius: 8px; margin: 0;'>%s</p>"
                        +
                        "    </div>" +
                        "    " +
                        "    <div style='margin: 20px 0;'>" +
                        "      <p style='font-weight: bold; color: #0f2a2a; margin-bottom: 5px;'>Description :</p>" +
                        "      <p style='background-color: #faf6ef; padding: 10px; border-radius: 8px; margin: 0;'>%s</p>"
                        +
                        "    </div>" +
                        "    " +
                        "    <div style='margin: 20px 0; padding: 15px; background-color: #f0f0f0; border-radius: 10px;'>"
                        +
                        "      <p style='margin: 0; font-size: 12px; color: #6a7a73;'>📅 Date : %s</p>" +
                        "    </div>" +
                        "    " +
                        "    <p style='color: #6a7a73; font-size: 14px; margin-top: 30px;'>Connectez-vous à la plateforme pour traiter cette réclamation.</p>"
                        +
                        "    " +
                        "    <div style='text-align: center; margin-top: 30px; padding-top: 20px; border-top: 1px solid #e0e0e0;'>"
                        +
                        "      <p style='color: #9a8a6f; font-size: 12px; margin: 0;'>© 2025 Gestion Réclamations & Avis</p>"
                        +
                        "    </div>" +
                        "  </div>" +
                        "</body>" +
                        "</html>",
                titre,
                description,
                new java.text.SimpleDateFormat("dd/MM/yyyy à HH:mm").format(new Date()));

        envoyerEmailHTML(destinataire, sujet, contenuHTML);
    }

    /**
     * EMAIL : Nouvel avis créé
     */
    public void envoyerEmailNouvelAvis(int note, String commentaire) {
        String destinataire = "rayenhafian72@gmail.com";
        String sujet = "⭐ Nouvel Avis Reçu";

        String etoiles = "⭐".repeat(Math.max(0, note));

        String contenu = String.format(
                "Bonjour,\n\n" +
                        "Un nouvel avis vient d'être publié :\n\n" +
                        "⭐ Note : %s (%d/5)\n\n" +
                        "💬 Commentaire :\n%s\n\n" +
                        "──────────────────────────────\n" +
                        "Date : %s\n\n" +
                        "Merci de consulter la plateforme pour plus de détails.\n\n" +
                        "Cordialement,\n" +
                        "Système de Gestion",
                etoiles,
                note,
                commentaire,
                new java.text.SimpleDateFormat("dd/MM/yyyy à HH:mm").format(new Date()));

        envoyerEmail(destinataire, sujet, contenu);
    }

    /**
     * EMAIL : Nouvel avis créé (VERSION HTML)
     */
    public void envoyerEmailNouvelAvisHTML(int note, String commentaire) {
        String destinataire = "rayenhafian72@gmail.com";
        String sujet = "⭐ Nouvel Avis Reçu";

        String etoiles = "⭐".repeat(Math.max(0, note));
        String couleurNote = note >= 4 ? "#1c7a44" : (note >= 3 ? "#f0d4a0" : "#d9534f");

        String contenuHTML = String.format(
                "<html>" +
                        "<body style='font-family: Arial, sans-serif; background-color: #f6f1e7; padding: 20px;'>" +
                        "  <div style='max-width: 600px; margin: 0 auto; background-color: white; padding: 30px; border-radius: 20px; box-shadow: 0 4px 12px rgba(0,0,0,0.1);'>"
                        +
                        "    <h2 style='color: #0f2a2a; border-bottom: 3px solid #c9a24a; padding-bottom: 10px;'>⭐ Nouvel Avis</h2>"
                        +
                        "    " +
                        "    <div style='text-align: center; margin: 20px 0;'>" +
                        "      <div style='background-color: %s; color: white; display: inline-block; padding: 15px 30px; border-radius: 50px; font-size: 24px; font-weight: bold;'>"
                        +
                        "        %s %d/5" +
                        "      </div>" +
                        "    </div>" +
                        "    " +
                        "    <div style='margin: 20px 0;'>" +
                        "      <p style='font-weight: bold; color: #0f2a2a; margin-bottom: 5px;'>Commentaire :</p>" +
                        "      <p style='background-color: #faf6ef; padding: 15px; border-radius: 8px; margin: 0; font-style: italic;'>%s</p>"
                        +
                        "    </div>" +
                        "    " +
                        "    <div style='margin: 20px 0; padding: 15px; background-color: #f0f0f0; border-radius: 10px;'>"
                        +
                        "      <p style='margin: 0; font-size: 12px; color: #6a7a73;'>📅 Date : %s</p>" +
                        "    </div>" +
                        "    " +
                        "    <p style='color: #6a7a73; font-size: 14px; margin-top: 30px;'>Consultez la plateforme pour voir tous les détails.</p>"
                        +
                        "    " +
                        "    <div style='text-align: center; margin-top: 30px; padding-top: 20px; border-top: 1px solid #e0e0e0;'>"
                        +
                        "      <p style='color: #9a8a6f; font-size: 12px; margin: 0;'>© 2025 Gestion Réclamations & Avis</p>"
                        +
                        "    </div>" +
                        "  </div>" +
                        "</body>" +
                        "</html>",
                couleurNote,
                etoiles,
                note,
                commentaire,
                new java.text.SimpleDateFormat("dd/MM/yyyy à HH:mm").format(new Date()));

        envoyerEmailHTML(destinataire, sujet, contenuHTML);
    }
}
