import hebergement.entities.TypeHebergement;
import hebergement.services.TypeHebergementService;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TypeHebergementServiceTest {

    static TypeHebergementService ts;
    private int idType = -1;

    @BeforeAll
    static void setup() {
        ts = new TypeHebergementService();
    }

    @AfterEach
    void cleanUp() throws SQLException {
        if (idType != -1) {
            TypeHebergement t = new TypeHebergement();
            t.setId(idType);
            ts.deleteEntity(t);
            System.out.println("[DEBUG_LOG] Cleanup: Deleted TypeHebergement with ID: " + idType);
            idType = -1;
        }
    }

    @Test
    @Order(1)
    void testCreateType() {
        String libelle = "TEST_TYPE_" + System.currentTimeMillis();
        try {
            ts.addEntity(new TypeHebergement(libelle));
            idType = ts.getIdByLibelle(libelle);

            System.out.println("[DEBUG_LOG] Created TypeHebergement with ID: " + idType);
            assertTrue(idType > 0);

            List<TypeHebergement> list = ts.getData();
            assertFalse(list.isEmpty());

            boolean found = list.stream().anyMatch(t -> t.getId() == idType && libelle.equals(t.getLibelle()));
            assertTrue(found);

        } catch (SQLException e) {
            System.out.println("Exception in test: " + e.getMessage());
        }
    }

    @Test
    @Order(2)
    void testUpdateType() throws SQLException {
        String libelle = "TEST_TYPE_" + System.currentTimeMillis();
        ts.addEntity(new TypeHebergement(libelle));
        idType = ts.getIdByLibelle(libelle);

        String newLib = "TEST_TYPE_MODIF_" + System.currentTimeMillis();
        ts.update(idType, new TypeHebergement(newLib));

        System.out.println("[DEBUG_LOG] Updated TypeHebergement ID " + idType + " to libelle: " + newLib);

        List<TypeHebergement> list = ts.getData();
        boolean found = list.stream().anyMatch(t -> t.getId() == idType && newLib.equals(t.getLibelle()));
        assertTrue(found);
    }

    @Test
    @Order(3)
    void testDeleteType() throws SQLException {
        String libelle = "TEST_TYPE_" + System.currentTimeMillis();
        ts.addEntity(new TypeHebergement(libelle));
        idType = ts.getIdByLibelle(libelle);

        TypeHebergement t = new TypeHebergement();
        t.setId(idType);
        ts.deleteEntity(t);

        System.out.println("[DEBUG_LOG] Deleted TypeHebergement with ID: " + idType);

        List<TypeHebergement> list = ts.getData();
        boolean exists = list.stream().anyMatch(x -> x.getId() == idType);
        assertFalse(exists);

        idType = -1; // déjà supprimé
    }
}
