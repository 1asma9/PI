package org.example.PI_Gestion_des_utilisateurs.templates;

/**
 * Template d'email professionnel pour Vianova - Application de voyage
 * Fournit des méthodes pour générer des emails de bienvenue formatés
 */
public class VianovaEmailTemplate {

    /**
     * Génère un email de bienvenue au format texte simple
     * @param nom Prénom de l'utilisateur
     * @param prenom Nom de l'utilisateur  
     * @param email Email de l'utilisateur
     * @param motDePasse Mot de passe de l'utilisateur
     * @return Email au format texte
     */
    public static String genererEmailBienvenueTexte(String nom, String prenom, String email, String motDePasse) {
        return "Bonjour " + prenom + " " + nom + ",\n\n"
                + "Bienvenue dans la famille Vianova ! Nous sommes ravis de vous accompagner dans vos futures aventures.\n\n"
                + "Votre compte a été créé avec succès et vous êtes maintenant prêt à explorer le monde avec nous.\n\n"
                + "----------------------------------------\n"
                + "Nom et prénom : " + prenom + " " + nom + "\n"
                + "Email : " + email + "\n"
                + "Mot de passe : " + motDePasse + "\n"
                + "----------------------------------------\n\n"
                + "Pour des raisons de sécurité, nous vous recommandons de modifier votre mot de passe lors de votre première connexion.\n\n"
                + "Que votre voyage soit rempli de découvertes, d'émerveillement et de souvenirs inoubliables !\n\n"
                + "L'équipe Vianova\n"
                + "Votre partenaire d'aventure";
    }

    /**
     * Génère un email de bienvenue au format HTML professionnel
     * @param nom Prénom de l'utilisateur
     * @param prenom Nom de l'utilisateur
     * @param email Email de l'utilisateur
     * @param motDePasse Mot de passe de l'utilisateur
     * @return Email au format HTML
     */
    public static String genererEmailBienvenueHTML(String nom, String prenom, String email, String motDePasse) {
        return "<!DOCTYPE html>"
                + "<html><head>"
                + "<meta charset='UTF-8'>"
                + "<title>Bienvenue chez Vianova</title>"
                + "<style>"
                + "body { font-family: 'Segoe UI', Arial, sans-serif; line-height: 1.6; color: #2c3e50; margin: 0; padding: 20px; background-color: #f8f9fa; }"
                + ".container { max-width: 600px; margin: 0 auto; background: white; border-radius: 10px; overflow: hidden; box-shadow: 0 4px 6px rgba(0,0,0,0.1); }"
                + ".header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 30px; text-align: center; }"
                + ".header h1 { margin: 0; font-size: 28px; font-weight: 300; }"
                + ".content { padding: 40px 30px; }"
                + ".welcome { font-size: 18px; color: #27ae60; font-weight: 500; margin-bottom: 20px; }"
                + ".info-box { background: #f8f9fa; border-left: 4px solid #667eea; padding: 20px; margin: 25px 0; border-radius: 5px; }"
                + ".info-item { margin: 10px 0; display: flex; align-items: center; }"
                + ".info-label { font-weight: 600; color: #495057; min-width: 120px; }"
                + ".info-value { color: #2c3e50; font-family: 'Courier New', monospace; background: #e9ecef; padding: 4px 8px; border-radius: 3px; }"
                + ".security-tip { background: #fff3cd; border: 1px solid #ffeaa7; border-radius: 5px; padding: 15px; margin: 20px 0; }"
                + ".security-tip h4 { color: #856404; margin: 0 0 10px 0; font-size: 16px; }"
                + ".adventure { background: linear-gradient(135deg, #74b9ff 0%, #0984e3 100%); color: white; padding: 25px; text-align: center; margin: 30px 0; border-radius: 5px; }"
                + ".adventure p { margin: 0; font-size: 18px; font-style: italic; }"
                + ".footer { background: #2c3e50; color: white; padding: 20px; text-align: center; }"
                + ".footer p { margin: 5px 0; font-size: 14px; }"
                + ".signature { font-weight: 500; font-size: 16px; }"
                + "</style>"
                + "</head><body>"
                + "<div class='container'>"
                + "<div class='header'>"
                + "<h1>✈️ Vianova</h1>"
                + "<p>Votre partenaire d'aventure</p>"
                + "</div>"
                + "<div class='content'>"
                + "<p class='welcome'>Bonjour " + prenom + " " + nom + ",</p>"
                + "<p>Bienvenue dans la famille Vianova ! Nous sommes ravis de vous accompagner dans vos futures aventures.</p>"
                + "<p>Votre compte a été créé avec succès et vous êtes maintenant prêt à explorer le monde avec nous.</p>"
                + "<div class='info-box'>"
                + "<h3>📋 Vos informations de connexion</h3>"
                + "<div class='info-item'>"
                + "<span class='info-label'>Nom et prénom :</span>"
                + "<span class='info-value'>" + prenom + " " + nom + "</span>"
                + "</div>"
                + "<div class='info-item'>"
                + "<span class='info-label'>Email :</span>"
                + "<span class='info-value'>" + email + "</span>"
                + "</div>"
                + "<div class='info-item'>"
                + "<span class='info-label'>Mot de passe :</span>"
                + "<span class='info-value'>" + motDePasse + "</span>"
                + "</div>"
                + "</div>"
                + "<div class='security-tip'>"
                + "<h4>🔒 Sécurité</h4>"
                + "<p>Pour des raisons de sécurité, nous vous recommandons de modifier votre mot de passe lors de votre première connexion.</p>"
                + "</div>"
                + "<div class='adventure'>"
                + "<p>🌍 Que votre voyage soit rempli de découvertes, d'émerveillement et de souvenirs inoubliables !</p>"
                + "</div>"
                + "</div>"
                + "<div class='footer'>"
                + "<p class='signature'>L'équipe Vianova</p>"
                + "<p>Votre partenaire d'aventure</p>"
                + "<p>📧 contact@vianova.travel | 🌐 www.vianova.travel</p>"
                + "</div>"
                + "</div>"
                + "</body></html>";
    }

    /**
     * Génère un sujet d'email professionnel
     * @return Sujet de l'email
     */
    public static String genererSujetBienvenue() {
        return "Bienvenue chez Vianova - Votre aventure commence ici !";
    }
}
