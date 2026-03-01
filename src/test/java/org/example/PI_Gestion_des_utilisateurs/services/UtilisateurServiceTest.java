package org.example.PI_Gestion_des_utilisateurs.services;

import org.example.PI_Gestion_des_utilisateurs.entities.utilisateur;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires complets pour le service utilisateur
 * Respecte les bonnes pratiques JUnit 5 et la séparation métier/test
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UtilisateurServiceTest {

    private static utilisateur_service service;
    private static Connection connection;

    /**
     * Initialisation avant tous les tests
     */
    @BeforeAll
    static void initAll() {
        service = new utilisateur_service();
        connection = org.example.PI_Gestion_des_utilisateurs.tools.MaConnection.getInstance().getConnection();
        System.out.println("=== DÉBUT DES TESTS ===");
    }

    /**
     * Nettoyage après chaque test pour garantir l'isolation
     */
    @AfterEach
    void cleanUp() {
        try {
            String sql = "DELETE FROM users WHERE email LIKE 'test%'";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("Erreur nettoyage: " + e.getMessage());
        }
    }

    /**
     * Test 1: Ajout d'un utilisateur valide
     * Vérifie la création complète avec toutes les informations requises
     */
    @Test
    @Order(1)
    void testAjoutUtilisateurValide() {
        utilisateur u = creerUtilisateurValide("test1@example.com", "flen", "benflen");
        boolean resultat = service.ajouterutilisateur(u);
        assertTrue(resultat, "L'ajout d'un utilisateur valide doit réussir");
        
        Optional<utilisateur> trouve = service.rechercherutilisateurParEmail("test1@example.com");
        assertTrue(trouve.isPresent(), "L'utilisateur ajouté doit être retrouvable");
        assertEquals("flen", trouve.get().getNom(), "Le nom doit correspondre");
        System.out.println("✓ Test 1: Ajout utilisateur réussi");
    }


    /**
     * Test 2: Recherche d'un utilisateur par email
     * Vérifie que la recherche retourne le bon utilisateur
     */
    @Test
    @Order(2)
    void testRechercheUtilisateurParEmail() {
        utilisateur u = creerUtilisateurValide("test4@example.com", "flen", "benflen");
        service.ajouterutilisateur(u);
        
        Optional<utilisateur> resultat = service.rechercherutilisateurParEmail("test4@example.com");
        assertTrue(resultat.isPresent(), "L'utilisateur doit être trouvé par email");
        assertEquals("flen", resultat.get().getNom(), "Le nom doit correspondre");
        assertEquals("benflen", resultat.get().getPrenom(), "Le prénom doit correspondre");
        assertEquals("test4@example.com", resultat.get().getEmail(), "L'email doit correspondre");
        System.out.println("✓ Test 2: Recherche utilisateur réussie");
    }

    /**
     * Test 3: Modification des informations d'un utilisateur
     * Vérifie que les modifications sont correctement appliquées
     */
    @Test
    @Order(3)
    void testModificationUtilisateur() {
        utilisateur u = creerUtilisateurValide("test5@example.com", "flen", "benflen");
        service.ajouterutilisateur(u);
        
        Optional<utilisateur> ajouteRecup = service.rechercherutilisateurParEmail("test5@example.com");
        assertTrue(ajouteRecup.isPresent(), "L'utilisateur doit exister pour la modification");
        int id = ajouteRecup.get().getId();
        
        u.setId(id);
        u.setNom("Martin-Modifié");
        u.setPrenom("Sophie-Modifiée");
        
        boolean resultat = service.modifierutilisateur(u);
        assertTrue(resultat, "La modification doit réussir");
        
        Optional<utilisateur> modifie = service.rechercherutilisateurParEmail("test5@example.com");
        assertTrue(modifie.isPresent(), "L'utilisateur modifié doit être retrouvable");
        assertEquals("Martin-Modifié", modifie.get().getNom(), "Le nom modifié doit être sauvegardé");
        System.out.println("✓ Test 3: Modification utilisateur réussie");
    }

   
    @Test
    @Order(4)
    void testSuppressionUtilisateur() {
        utilisateur u = creerUtilisateurValide("test6@example.com", "flen", "benflen");
        service.ajouterutilisateur(u);
        
        Optional<utilisateur> ajouteRecup = service.rechercherutilisateurParEmail("test6@example.com");
        assertTrue(ajouteRecup.isPresent(), "L'utilisateur doit exister pour la suppression");
        int id = ajouteRecup.get().getId();
        
        boolean resultat = service.supprimerutilisateur(id);
        assertTrue(resultat, "La suppression doit réussir");
        
        Optional<utilisateur> supprime = service.rechercherutilisateurParEmail("test6@example.com");
        assertFalse(supprime.isPresent(), "L'utilisateur supprimé ne doit plus être retrouvable");
        System.out.println("✓ Test 4: Suppression utilisateur réussie");
    }

    
    /**
     * Méthode utilitaire pour créer un utilisateur valide pour les tests
     */
    private utilisateur creerUtilisateurValide(String email, String nom, String prenom) {
        utilisateur u = new utilisateur();
        u.setNom(nom);
        u.setPrenom(prenom);
        u.setEmail(email);
        u.setPassword("123456");
        return u;
    }
    
    /**
     * Nettoyage final après tous les tests (méthode statique)
     */
    static void finalCleanUp() {
        try {
            String sql = "DELETE FROM users WHERE email LIKE 'test%'";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("Erreur nettoyage final: " + e.getMessage());
        }
    }


    @AfterAll
    static void finAll() {
        finalCleanUp();
    }
}
