package edu.destination.tests;

import edu.destination.entities.Destination;
import edu.destination.entities.Image;
import edu.destination.entities.Transport;
import edu.destination.services.DestinationService;
import edu.destination.services.ImageService;
import edu.destination.services.TransportService;
import edu.destination.entities.Voyage;
import edu.destination.services.VoyageService;


import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class MainClass {
    public static void main(String[] args) {

        // 1️⃣ Create the Destination correctly
        Destination d1 = new Destination(
                "Africa",                   // nom
                "Tunisia",                  // pays
                "Découverte du Sahara",     // description
                true,                       // statut
                "Octobre",                  // meilleureSaison
                33.8869,                    // latitude
                9.5375,                     // longitude
                0,                          // nbVisites
                "videos/sahara.mp4",        // videoPath  ✅ String
                0                           // nbLikes    ✅ int
        );

        DestinationService ds = new DestinationService();
        ImageService is = new ImageService();
        TransportService ts = new TransportService();
// Add VoyageService if you have one
// VoyageService vs = new VoyageService();

        try {
            ds.addEntity2(d1);

            List<Destination> destinations = ds.getData();
            int lastId = destinations.get(destinations.size() - 1).getId(); // use getId() not getIdDestination()

            // 2️⃣ Create a Voyage linked to this destination
            Voyage v1 = new Voyage(
                    LocalDate.of(2026, 4, 20), // dateDepart
                    LocalDate.of(2026, 4, 27), // dateArrivee
                    "Tunis",                   // pointDepart
                    "Sahara",                  // pointArrivee
                    1200.0,                    // prix
                    lastId,                    // destinationId
                    0                          // paid
            );
            // vs.addEntity2(v1);

            // 3️⃣ Add image
            Image img1 = new Image("sahara.jpg", lastId);
            is.addEntity2(img1);

            // 4️⃣ Add transport
            Transport t1 = new Transport("Avion", lastId);
            ts.addEntity2(t1);

            System.out.println("✅ Destination, voyage, image et transport ajoutés avec succès !");

        } catch (SQLException e) {
            System.out.println("Erreur : " + e.getMessage());
        }
    }
}