package hebergement.controllers;

import javafx.fxml.FXML;
import org.example.PI_Gestion_des_utilisateurs.entities.utilisateur;

public class MainLayoutController {

    private static utilisateur currentUser;

    public static void setCurrentUser(utilisateur user) {
        currentUser = user;
    }

    public static utilisateur getCurrentUser() {
        return currentUser;
    }

}