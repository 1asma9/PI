package edu.pidev.tests;

import java.time.LocalDate;
import edu.pidev.entities.ReservationActivite;
import edu.pidev.services.ReservationActiviteService;


public class MainClass {
    public static void main(String[] args) {

        ReservationActiviteService service = new ReservationActiviteService();

        System.out.println("----- TEST 1: date passée -----");
        ReservationActivite r1 = new ReservationActivite(
                LocalDate.now().minusDays(1), // ❌ passé
                2,
                "CONFIRMEE",
                1
        );

        try {
            service.addReservation(r1);
        } catch (Exception e) {
            System.out.println("Erreur attrapée: " + e.getMessage());
        }

        System.out.println("----- TEST 2: nb personnes = 0 -----");
        ReservationActivite r2 = new ReservationActivite(
                LocalDate.now().plusDays(2),
                0, // ❌
                "CONFIRMEE",
                1
        );

        try {
            service.addReservation(r2);
        } catch (Exception e) {
            System.out.println("Erreur attrapée: " + e.getMessage());
        }

        System.out.println("----- TEST 3: statut invalide -----");
        ReservationActivite r3 = new ReservationActivite(
                LocalDate.now().plusDays(2),
                2,
                "OK",
                1
        );

        try {
            service.addReservation(r3);
        } catch (Exception e) {
            System.out.println("Erreur attrapée: " + e.getMessage());
        }

        System.out.println("----- TEST 4: réservation correcte -----");
        ReservationActivite r4 = new ReservationActivite(
                LocalDate.now().plusDays(3),
                2,
                "en_attente",
                1
        );

        try {
            service.addReservation(r4);
        } catch (Exception e) {
            System.out.println("Erreur attrapée: " + e.getMessage());
        }
    }
}
