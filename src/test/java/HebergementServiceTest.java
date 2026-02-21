import hebergement.entities.Hebergement;
import hebergement.entities.TypeHebergement;
import hebergement.services.HebergementService;
import hebergement.services.TypeHebergementService;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class HebergementServiceTest {

    static HebergementService hs;
    static TypeHebergementService ts;

    private int idType = -1;
    private int idHeb = -1;

    @BeforeAll
    static void setup() {
        hs = new HebergementService();
        ts = new TypeHebergementService();
    }

    @AfterEach
    void cleanUp() throws SQLException {

        if (idHeb != -1) {
            Hebergement h = new Hebergement();
            h.setId(idHeb);
            hs.deleteEntity(h);
            System.out.println("[DEBUG_LOG] Cleanup: Deleted Hebergement with ID: " + idHeb);
            idHeb = -1;
        }

        if (idType != -1) {
            TypeHebergement t = new TypeHebergement();
            t.setId(idType);
            ts.deleteEntity(t);
            System.out.println("[DEBUG_LOG] Cleanup: Deleted TypeHebergement with ID: " + idType);
            idType = -1;
        }
    }

    private void createType() throws SQLException {
        String lib = "TEST_TYPE_" + System.currentTimeMillis();
        ts.addEntity(new TypeHebergement(lib));
        Integer tmp = ts.getIdByLibelle(lib);
        idType = (tmp == null) ? -1 : tmp;
        assertTrue(idType > 0);
    }

    @Test
    @Order(1)
    void testCreateHebergement() {
        try {
            createType();

            Hebergement h = new Hebergement();
            h.setDescription("DESC_TEST");
            h.setAdresse("ADDR_TEST");
            h.setPrix(120);
            h.setTypeId(idType);
            h.setImagePath("img/test.png");

            idHeb = hs.addEntityReturnId(h);

            System.out.println("[DEBUG_LOG] Created Hebergement with ID: " + idHeb);
            assertTrue(idHeb > 0);

            List<Hebergement> list = hs.getData();
            assertFalse(list.isEmpty());

            boolean found = list.stream().anyMatch(x -> x.getId() == idHeb && "DESC_TEST".equals(x.getDescription()));
            assertTrue(found);

        } catch (SQLException e) {
            System.out.println("Exception in test: " + e.getMessage());
        }
    }

    @Test
    @Order(2)
    void testUpdateHebergement() throws SQLException {
        createType();

        Hebergement h = new Hebergement();
        h.setDescription("DESC_TEST");
        h.setAdresse("ADDR_TEST");
        h.setPrix(120);
        h.setTypeId(idType);
        h.setImagePath("img/test.png");
        idHeb = hs.addEntityReturnId(h);

        Hebergement updateInfo = new Hebergement();
        updateInfo.setDescription("DESC_AFTER");
        updateInfo.setAdresse("ADDR_AFTER");
        updateInfo.setPrix(300);
        updateInfo.setTypeId(idType);
        updateInfo.setImagePath("img/after.png");

        hs.update(idHeb, updateInfo);
        System.out.println("[DEBUG_LOG] Updated Hebergement ID " + idHeb);

        List<Hebergement> list = hs.getData();
        boolean found = list.stream().anyMatch(x -> x.getId() == idHeb && "DESC_AFTER".equals(x.getDescription()));
        assertTrue(found);
    }

    @Test
    @Order(3)
    void testDeleteHebergement() throws SQLException {
        createType();

        Hebergement h = new Hebergement();
        h.setDescription("DESC_TEST");
        h.setAdresse("ADDR_TEST");
        h.setPrix(120);
        h.setTypeId(idType);
        h.setImagePath("img/test.png");
        idHeb = hs.addEntityReturnId(h);

        Hebergement del = new Hebergement();
        del.setId(idHeb);
        hs.deleteEntity(del);

        System.out.println("[DEBUG_LOG] Deleted Hebergement with ID: " + idHeb);

        List<Hebergement> list = hs.getData();
        boolean exists = list.stream().anyMatch(x -> x.getId() == idHeb);
        assertFalse(exists);

        idHeb = -1; // déjà supprimé
    }
}
