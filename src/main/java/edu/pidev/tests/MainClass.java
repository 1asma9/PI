package edu.pidev.tests;

import java.time.LocalDate;
import edu.pidev.entities.ReservationActivite;
import edu.pidev.services.ReservationActiviteService;


public class MainClass {
    public static void main(String[] args) {

        ReservationActiviteService service = new ReservationActiviteService();

        // ✅ TEST 1: date passée (doit être REFUSÉ)
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

        // ✅ TEST 2: nb personnes invalide (doit être REFUSÉ)
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

        // ✅ TEST 3: statut invalide (doit être REFUSÉ)
        System.out.println("----- TEST 3: statut invalide -----");
        ReservationActivite r3 = new ReservationActivite(
                LocalDate.now().plusDays(2),
                2,
                "OK", // ❌ pas dans EN_ATTENTE/CONFIRMEE/ANNULEE
                1
        );

        try {
            service.addReservation(r3);
        } catch (Exception e) {
            System.out.println("Erreur attrapée: " + e.getMessage());
        }

        // ✅ TEST 4: réservation correcte (doit être ACCEPTÉ)
        System.out.println("----- TEST 4: réservation correcte -----");
        ReservationActivite r4 = new ReservationActivite(
                LocalDate.now().plusDays(3),
                2,
                "en_attente", // ✅ même si minuscule, validator le normalise si tu as ajouté r.setStatut(st)
                1
        );

        try {
            service.addReservation(r4);
        } catch (Exception e) {
            System.out.println("Erreur attrapée: " + e.getMessage());
        }
    }
}
