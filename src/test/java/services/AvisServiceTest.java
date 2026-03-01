package services;

import entities.Avis;
import org.junit.jupiter.api.*;
import java.sql.SQLException;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AvisServiceTest {
    static AvisService service;

    @BeforeAll
    static void setup() {
        service = new AvisService();
    }

    @Test
    @Order(1)
    void testAddAvis() throws SQLException {
        Avis a = new Avis(1, 5, "Test comment");
        service.addEntity(a);
        List<Avis> avisList = service.getAllEntities();
        assertFalse(avisList.isEmpty());
        assertTrue(avisList.stream()
                .anyMatch(avis -> avis.getCommentaire().equals("Test comment")));
    }

    @Test
    @Order(2)
    void testUpdateAvis() throws SQLException {
        List<Avis> avisList = service.getAllEntities();
        if (!avisList.isEmpty()) {
            Avis a = avisList.get(avisList.size() - 1);
            a.setNote(4);
            a.setCommentaire("Updated comment");
            service.updateEntity(a);

            avisList = service.getAllEntities();
            assertTrue(avisList.stream()
                    .anyMatch(avis -> avis.getCommentaire().equals("Updated comment")));
        }
    }

    @Test
    @Order(3)
    void testDeleteAvis() throws SQLException {
        List<Avis> avisList = service.getAllEntities();
        if (!avisList.isEmpty()) {
            Avis last = avisList.get(avisList.size() - 1);
            service.deleteEntity(last.getId());

            avisList = service.getAllEntities();
            assertFalse(avisList.stream()
                    .anyMatch(a -> a.getId() == last.getId()));
        }
    }
}
