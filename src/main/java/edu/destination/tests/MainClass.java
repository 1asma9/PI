package edu.destination.tests;

import edu.destination.entities.Destination;
import edu.destination.entities.DestinationImage;
import edu.destination.entities.Transport;
import edu.destination.services.DestinationService;
import edu.destination.services.ImageService;
import edu.destination.services.TransportService;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class MainClass {
    public static void main(String[] args) {

        // 1️⃣ Création de la destination avec prix, dateDepart et dateArrivee
        Destination d1 = new Destination(
                "Africa",           // nom
                "Tunisia",          // pays
                "Découverte du Sahara", // description
                true,               // statut
                "Octobre",          // meilleureSaison
                33.8869,            // latitude
                9.5375,             // longitude
                0,                  // nbVisites
                1200.0,             // prix
                LocalDate.of(2026, 4, 20), // dateDepart
                LocalDate.of(2026, 4, 27)  // dateArrivee
        );

        DestinationService ds = new DestinationService();
        ImageService is = new ImageService();
        TransportService ts = new TransportService();

        try {
            // Ajout de la destination
            ds.addEntity2(d1);

            // On récupère l'id de la dernière destination ajoutée
            List<Destination> destinations = ds.getData();
            int lastId = destinations.get(destinations.size() - 1).getIdDestination();

            // 2️⃣ Ajout d'une image pour cette destination
            DestinationImage img1 = new DestinationImage(
                    "sahara.jpg",   // url_image
                    lastId          // id_destination
            );
            is.addEntity2(img1);

            // 3️⃣ Ajout d'un transport pour cette destination
            Transport t1 = new Transport(
                    "Avion",       // type_transport
                    lastId         // id_destination
            );
            ts.addEntity2(t1);

            System.out.println("✅ Destination, image et transport ajoutés avec succès !");

        } catch (SQLException e) {
            System.out.println("Erreur : " + e.getMessage());
        }
    }
}