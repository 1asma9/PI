package org.example.PI_Gestion_des_utilisateurs.interfaces;

import org.example.PI_Gestion_des_utilisateurs.entities.utilisateur;

import java.util.List;
import java.util.Optional;

public interface utilisateur_interface {
    boolean ajouterutilisateur(utilisateur Utilisateur);

    List<utilisateur> afficherutilisateurs();

    boolean modifierutilisateur(utilisateur Utilisateur);

    boolean supprimerutilisateur(int id);

    Optional<utilisateur> rechercherutilisateurParEmail(String email);

    boolean associerRoleAutilisateur(int utilisateurId, int roleId);

    boolean verifierEmailUnique(String email);

    boolean validerDonneesutilisateur(utilisateur Utilisateur);
}
