package services;

import entities.Reclamation;
import org.junit.jupiter.api.*;
import java.sql.SQLException;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ReclamationServiceTest {
    static ReclamationService service;

    @BeforeAll
    static void setup() {
        service = new ReclamationService();
    }

    @Test
    @Order(1)
    void testAddReclamation() throws SQLException {
        Reclamation r = new Reclamation(1, "Test Title", "Test Description");
        service.addEntity(r);
        List<Reclamation> reclamations = service.getAllEntities();
        assertFalse(reclamations.isEmpty());
        assertTrue(reclamations.stream()
                .anyMatch(rec -> rec.getTitre().equals("Test Title")));
    }

    @Test
    @Order(2)
    void testUpdateReclamation() throws SQLException {
        List<Reclamation> reclamations = service.getAllEntities();
        if (!reclamations.isEmpty()) {
            Reclamation r = reclamations.get(reclamations.size() - 1);
            r.setTitre("Updated Title");
            service.updateEntity(r);

            reclamations = service.getAllEntities();
            assertTrue(reclamations.stream()
                    .anyMatch(rec -> rec.getTitre().equals("Updated Title")));
        }
    }

    @Test
    @Order(3)
    void testRespondToReclamation() throws SQLException {
        Reclamation r = new Reclamation(1, "Test Respond", "Test Description");
        service.addEntity(r);

        List<Reclamation> reclamations = service.getAllEntities();
        Reclamation last = reclamations.get(reclamations.size() - 1);

        service.repondreReclamation(last.getId(), "Admin response test");

        reclamations = service.getAllEntities();
        Reclamation updated = reclamations.stream()
                .filter(rec -> rec.getId() == last.getId())
                .findFirst()
                .orElse(null);

        assertNotNull(updated);
        assertEquals("Traitée", updated.getStatut());
        assertEquals("Admin response test", updated.getReponseAdmin());
    }

    @Test
    @Order(4)
    void testDeleteReclamation() throws SQLException {
        List<Reclamation> reclamations = service.getAllEntities();
        if (!reclamations.isEmpty()) {
            Reclamation last = reclamations.get(reclamations.size() - 1);
            service.deleteEntity(last.getId());

            reclamations = service.getAllEntities();
            assertFalse(reclamations.stream()
                    .anyMatch(r -> r.getId() == last.getId()));
        }
    }
}
