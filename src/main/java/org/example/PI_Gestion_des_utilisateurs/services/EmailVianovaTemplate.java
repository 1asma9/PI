
package org.example.PI_Gestion_des_utilisateurs.services;

/**
 * Template d'email professionnel pour l'application de voyage Vianova
 * Génère des emails de bienvenue personnalisés et adaptés au secteur du tourisme
 */
public class EmailVianovaTemplate {
    
    /**
     * Génère l'email de bienvenue professionnel pour Vianova
     * @param nom Nom de l'utilisateur
     * @param prenom Prénom de l'utilisateur  
     * @param email Email de l'utilisateur
     * @param motDePasse Mot de passe en clair
     * @return Email complet formaté prêt à l'envoi
     */
    public static String genererEmailBienvenueVianova(String nom, String prenom, String email, String motDePasse) {
        StringBuilder message = new StringBuilder();
        
        // En-tête personnalisé et professionnel
        message.append("Cher/Chère ").append(prenom).append(" ").append(nom).append(",\n\n");
        
        // Message de bienvenue principal
        message.append("Bienvenue dans la famille Vianova ! 🌍\n\n");
        message.append("Nous sommes ravis de vous accueillir parmi nos voyageurs passionnés. ");
        message.append("Votre compte a été créé avec succès et vous êtes maintenant prêt(e) à découvrir ");
        message.append("des expériences de voyage inoubliables.\n\n");
        
        // Informations de connexion structurées
        message.append("📋 VOS INFORMATIONS DE CONNEXION :\n");
        message.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        message.append("👤 Nom complet : ").append(prenom).append(" ").append(nom).append("\n");
        message.append("📧 Adresse email : ").append(email).append("\n");
        message.append("🔑 Mot de passe : ").append(motDePasse).append("\n");
        message.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");
        
        // Phrase de sécurité importante
        message.append("🔒 SÉCURITÉ DE VOTRE COMPTE :\n");
        message.append("Pour garantir la sécurité de vos données personnelles, nous vous recommandons vivement ");
        message.append("de changer votre mot de passe lors de votre première connexion. ");
        message.append("Rendez-vous dans les paramètres de votre profil pour le personnaliser.\n\n");
        
        // Découverte des fonctionnalités Vianova
        message.append("🌟 DÉCOUVREZ VIANOVA :\n");
        message.append("• Explorez des destinations exceptionnelles à travers le monde\n");
        message.append("• Bénéficiez d'offres exclusives et de tarifs préférentiels\n");
        message.append("• Partagez vos expériences avec notre communauté de voyageurs\n");
        message.append("• Accédez à votre carnet de voyage numérique\n\n");
        
        // Support et assistance voyage
        message.append("🤝 BESOIN D'AIDE ?\n");
        message.append("Notre équipe d'assistance voyage est à votre disposition :\n");
        message.append("• Email : support@vianova.com\n");
        message.append("• Téléphone : +33 1 234 567 890\n");
        message.append("• Chat disponible 7j/7 sur notre site\n\n");
        
        // Phrase finale adaptée au secteur du voyage
        message.append("✈️ Que chaque voyage avec Vianova soit une nouvelle aventure !\n\n");
        
        // Signature professionnelle Vianova
        message.append("Cordialement,\n");
        message.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        message.append("L'équipe Vianova\n");
        message.append("🌍 Votre partenaire de confiance pour des voyages inoubliables\n");
        message.append("📧 contact@vianova.com | 🌐 www.vianova.com\n");
        message.append("📱 +33 1 234 567 890\n");
        
        return message.toString();
    }
    
    /**
     * Version simplifiée pour intégration rapide
     */
    public static String genererEmailBienvenueSimple(String nom, String prenom, String email, String motDePasse) {
        return String.format(
            "Bonjour %s %s,\n\n" +
            "Bienvenue dans l'application Vianova !\n\n" +
            "Voici vos informations de connexion :\n" +
            "Email : %s\n" +
            "Mot de passe : %s\n\n" +
            "Nous vous recommandons de changer votre mot de passe lors de votre première connexion.\n\n" +
            "✈️ Que chaque voyage avec Vianova soit une nouvelle aventure !\n\n" +
            "Cordialement,\n" +
            "L'équipe Vianova\n" +
            "🌍 Votre partenaire de confiance pour des voyages inoubliables",
            prenom, nom, email, motDePasse
        );
    }
}
