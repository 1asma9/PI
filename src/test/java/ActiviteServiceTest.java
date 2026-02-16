import edu.pidev.entities.Activite;
import edu.pidev.services.ActiviteService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ActiviteServiceTest {

    static ActiviteService service;
    static int createdId = -1;
    static int idActiviteToCleanup = -1; // ✅ ID à nettoyer
    static String uniqueName;

    @BeforeAll
    public static void setup() {
        service = new ActiviteService();
        System.out.println("[DEBUG_LOG] ActiviteService initialized");
    }

    @AfterEach
    void cleanUp() {
        try {
            if (idActiviteToCleanup != -1) {
                service.deleteActivite(idActiviteToCleanup);
                System.out.println("[DEBUG_LOG] Cleanup: Deleted Activite with ID: " + idActiviteToCleanup);
                idActiviteToCleanup = -1;
            }
        } catch (Exception e) {
            System.out.println("[DEBUG_LOG] Cleanup failed: " + e.getMessage());
        }
    }

    @Test
    public void testCreateAndReadActivite() {

        uniqueName = "JUnit Test " + System.currentTimeMillis();

        Activite a = new Activite(
                uniqueName,
                "Activite creee par test unitaire",
                "SPORT",
                30.0,
                90,
                "Tunis"
        );

        try {
            // ✅ CREATE (chez toi retourne void)
            service.addActivite(a);
            System.out.println("[DEBUG_LOG] addActivite() called");

            // ✅ READ
            List<Activite> list = service.getAllActivites();
            assertFalse(list.isEmpty(), "List should not be empty");

            Activite created = list.stream()
                    .filter(act -> act.getNom().equals(uniqueName))
                    .findFirst()
                    .orElse(null);

            assertNotNull(created, "Created activite should exist in DB");

            createdId = created.getIdActivite();
            idActiviteToCleanup = createdId; // ✅ pour cleanup
            System.out.println("[DEBUG_LOG] Found created activite ID: " + createdId);

            assertTrue(createdId > 0, "ID must be > 0");

        } catch (Exception e) {
            System.out.println("exception in test : " + e.getMessage());
            fail("Exception thrown: " + e.getMessage());
        }
    }

    @Test
    public void testDeleteActivite() {

        try {
            // si le test delete se lance avant create
            if (createdId <= 0) {
                uniqueName = "To Delete " + System.currentTimeMillis();

                Activite a = new Activite(
                        uniqueName,
                        "Delete test",
                        "TEST",
                        10.0,
                        30,
                        "Ariana"
                );

                service.addActivite(a);

                Activite created = service.getAllActivites().stream()
                        .filter(act -> act.getNom().equals(uniqueName))
                        .findFirst()
                        .orElse(null);

                assertNotNull(created, "Activite for delete test should exist");
                createdId = created.getIdActivite();
                System.out.println("[DEBUG_LOG] Created for delete test ID: " + createdId);
            }

            // ✅ DELETE (chez toi retourne void)
            service.deleteActivite(createdId);
            System.out.println("[DEBUG_LOG] deleteActivite() called for ID: " + createdId);

            // ✅ important : ne pas re-supprimer dans cleanup
            idActiviteToCleanup = -1;

            // ✅ VERIFY DELETE
            boolean stillExists = service.getAllActivites().stream()
                    .anyMatch(act -> act.getIdActivite() == createdId);

            assertFalse(stillExists, "Activite should be deleted");

        } catch (Exception e) {
            System.out.println("exception in test : " + e.getMessage());
            fail("Exception thrown: " + e.getMessage());
        }
    }

    @Test
    public void testUpdateActivite() {

        try {
            // 1️⃣ S'assurer qu'une activité existe
            if (createdId <= 0) {
                String name = "To Update " + System.currentTimeMillis();
                Activite a = new Activite(
                        name,
                        "Avant update",
                        "TEST",
                        15.0,
                        45,
                        "Tunis"
                );
                service.addActivite(a);

                Activite created = service.getAllActivites().stream()
                        .filter(act -> act.getNom().equals(name))
                        .findFirst()
                        .orElse(null);

                assertNotNull(created, "Activite for update test should exist");
                createdId = created.getIdActivite();
                System.out.println("[DEBUG_LOG] Created for update test ID: " + createdId);
            }

            // ✅ on marque cet ID pour cleanup (car on laisse une ligne en DB sinon)
            idActiviteToCleanup = createdId;

            // 2️⃣ UPDATE
            Activite updated = new Activite(
                    createdId,                 // ⚠️ ID OBLIGATOIRE
                    "Updated Name",
                    "Description mise a jour",
                    "UPDATED",
                    99.0,
                    120,
                    "Ariana"
            );

            service.updateActivite(updated);
            System.out.println("[DEBUG_LOG] updateActivite() called for ID: " + createdId);

            // 3️⃣ VERIFY UPDATE
            Activite fromDb = service.getAllActivites().stream()
                    .filter(act -> act.getIdActivite() == createdId)
                    .findFirst()
                    .orElse(null);

            assertNotNull(fromDb, "Updated activite should exist");

            assertEquals("Updated Name", fromDb.getNom());
            assertEquals("Description mise a jour", fromDb.getDescription());
            assertEquals("UPDATED", fromDb.getType());
            assertEquals(99.0, fromDb.getPrix());
            assertEquals(120, fromDb.getDuree());
            assertEquals("Ariana", fromDb.getLieu());

            System.out.println("[DEBUG_LOG] Update verified successfully");

        } catch (Exception e) {
            System.out.println("exception in test : " + e.getMessage());
            fail("Exception thrown: " + e.getMessage());
        }
    }
}
