package org.example.PI_Gestion_des_utilisateurs.services;

public class EmailVianovaTemplate {

    private static final String BASE_TEMPLATE = "<div style=\"background-color:#f4f4f4;padding:20px;font-family:Arial,sans-serif;\">" +
            "<div style=\"max-width:600px;margin:0 auto;background-color:#ffffff;border-radius:10px;overflow:hidden;box-shadow:0 4px 10px rgba(0,0,0,0.1);\">" +
            "<div style=\"background:linear-gradient(135deg, #e8a87c, #d4845a);padding:20px;text-align:center;color:#ffffff;\">" +
            "<h1 style=\"margin:0;font-size:24px;\">ViaNoVa</h1>" +
            "</div>" +
            "<div style=\"padding:30px;color:#333333;line-height:1.6;\">" +
            "%s" +
            "</div>" +
            "<div style=\"background-color:#f9f9f9;padding:15px;text-align:center;font-size:12px;color:#777777;\">" +
            "&copy; " + java.time.Year.now().getValue() + " ViaNoVa. Tous droits réservés." +
            "</div>" +
            "</div></div>";

    private static final String BUTTON_STYLE = "display:inline-block;padding:12px 25px;background:linear-gradient(135deg, #e8a87c, #d4845a);color:#ffffff;text-decoration:none;border-radius:5px;font-weight:bold;margin-top:20px;margin-bottom:20px;";

    public static String getVerificationEmailHtml(String toName, String verificationLink) {
        String content = "<h2 style=\"color:#d4845a;margin-top:0;\">Vérification de votre compte</h2>" +
                "<p>Bonjour " + toName + ",</p>" +
                "<p>Merci d'avoir rejoint ViaNoVa ! Veuillez vérifier votre adresse email en cliquant sur le bouton ci-dessous :</p>" +
                "<div style=\"text-align:center;\"><a href=\"" + verificationLink + "\" style=\"" + BUTTON_STYLE + "\">Vérifier mon email</a></div>" +
                "<p>Si le bouton ne fonctionne pas, copiez-collez ce lien dans votre navigateur :</p>" +
                "<p><a href=\"" + verificationLink + "\" style=\"color:#d4845a;word-break:break-all;\">" + verificationLink + "</a></p>";
        return String.format(BASE_TEMPLATE, content);
    }

    public static String getWelcomeEmailHtml(String toName) {
        String content = "<h2 style=\"color:#d4845a;margin-top:0;\">Bienvenue chez ViaNoVa !</h2>" +
                "<p>Bonjour " + toName + ",</p>" +
                "<p>Votre compte a été créé avec succès ! Nous sommes ravis de vous compter parmi nos membres.</p>" +
                "<div style=\"text-align:center;\"><a href=\"http://localhost:8000\" style=\"" + BUTTON_STYLE + "\">Découvrir ViaNoVa</a></div>" +
                "<p>Préparez-vous pour de nouvelles aventures !</p>";
        return String.format(BASE_TEMPLATE, content);
    }

    public static String getResetPasswordEmailHtml(String toName, String resetLink) {
        String content = "<h2 style=\"color:#d4845a;margin-top:0;\">Réinitialisation de mot de passe</h2>" +
                "<p>Bonjour " + toName + ",</p>" +
                "<p>Vous avez demandé à réinitialiser votre mot de passe. Cliquez sur le bouton ci-dessous pour en créer un nouveau (le lien expire dans 1 heure) :</p>" +
                "<div style=\"text-align:center;\"><a href=\"" + resetLink + "\" style=\"" + BUTTON_STYLE + "\">Réinitialiser mon mot de passe</a></div>" +
                "<p>Si vous n'êtes pas à l'origine de cette demande, vous pouvez ignorer cet email en toute sécurité.</p>";
        return String.format(BASE_TEMPLATE, content);
    }

    // Keep old methods if they are still used somewhere
    public static String genererEmailBienvenueVianova(String nom, String prenom, String email, String motDePasse) {
        return "OBSOLETE";
    }

    public static String genererEmailBienvenueSimple(String nom, String prenom, String email, String motDePasse) {
        return "OBSOLETE";
    }
}
