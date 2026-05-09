import hebergement.entities.Disponibilite;
import hebergement.entities.Hebergement;
import hebergement.entities.TypeHebergement;
import hebergement.services.DisponibiliteService;
import hebergement.services.HebergementService;
import hebergement.services.TypeHebergementService;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DisponibiliteServiceTest {

    static DisponibiliteService ds;
    static HebergementService hs;
    static TypeHebergementService ts;

    private int idType = -1;
    private int idHeb = -1;
    private int idDispo = -1;

    @BeforeAll
    static void setup() {
        ds = new DisponibiliteService();
        hs = new HebergementService();
        ts = new TypeHebergementService();
    }

    @AfterEach
    void cleanUp() throws SQLException {

        if (idDispo != -1) {
            Disponibilite d = new Disponibilite();
            d.setId(idDispo);
            ds.deleteEntity(d);
            System.out.println("[DEBUG_LOG] Cleanup: Deleted Disponibilite with ID: " + idDispo);
            idDispo = -1;
        }

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

    private void createFixture() throws SQLException {
        String lib = "TEST_TYPE_" + System.currentTimeMillis();
        ts.addEntity(new TypeHebergement(lib));
        idType = ts.getIdByLibelle(lib);

        Hebergement h = new Hebergement();
        h.setDescription("HEB_TEST");
        h.setAdresse("ADDR_TEST");
        h.setPrix(99.9);
        h.setTypeId(idType);
        h.setImagePath("img/x.png");
        idHeb = hs.addEntityReturnId(h);
    }

    @Test
    @Order(1)
    void testCreateDisponibilite() {
        try {
            createFixture();

            Disponibilite d = new Disponibilite();
            d.setHebergementId(idHeb);
            d.setDateDebut(LocalDate.now().plusDays(1));
            d.setDateFin(LocalDate.now().plusDays(10));
            d.setDisponible(true);

            ds.addEntity(d);

            List<Disponibilite> list = ds.getByHebergement(idHeb);
            assertFalse(list.isEmpty());

            idDispo = list.get(0).getId();
            System.out.println("[DEBUG_LOG] Created Disponibilite with ID: " + idDispo);

            boolean found = list.stream().anyMatch(x -> x.getId() == idDispo);
            assertTrue(found);

        } catch (SQLException e) {
            System.out.println("Exception in test: " + e.getMessage());
        }
    }

    @Test
    @Order(2)
    void testUpdateDisponibilite() throws SQLException {
        createFixture();

        Disponibilite d = new Disponibilite();
        d.setHebergementId(idHeb);
        d.setDateDebut(LocalDate.now().plusDays(1));
        d.setDateFin(LocalDate.now().plusDays(10));
        d.setDisponible(true);
        ds.addEntity(d);

        idDispo = ds.getByHebergement(idHeb).get(0).getId();

        Disponibilite updateInfo = new Disponibilite();
        updateInfo.setHebergementId(idHeb);
        updateInfo.setDateDebut(LocalDate.now().plusDays(2));
        updateInfo.setDateFin(LocalDate.now().plusDays(12));
        updateInfo.setDisponible(false);

        ds.update(idDispo, updateInfo);

        System.out.println("[DEBUG_LOG] Updated Disponibilite ID: " + idDispo);

        List<Disponibilite> list = ds.getByHebergement(idHeb);
        boolean found = list.stream().anyMatch(x -> x.getId() == idDispo && !x.isDisponible());
        assertTrue(found);
    }

    @Test
    @Order(3)
    void testDeleteDisponibilite() throws SQLException {
        createFixture();

        Disponibilite d = new Disponibilite();
        d.setHebergementId(idHeb);
        d.setDateDebut(LocalDate.now().plusDays(1));
        d.setDateFin(LocalDate.now().plusDays(10));
        d.setDisponible(true);
        ds.addEntity(d);

        idDispo = ds.getByHebergement(idHeb).get(0).getId();

        Disponibilite del = new Disponibilite();
        del.setId(idDispo);
        ds.deleteEntity(del);

        System.out.println("[DEBUG_LOG] Deleted Disponibilite ID: " + idDispo);

        List<Disponibilite> list = ds.getByHebergement(idHeb);
        boolean exists = list.stream().anyMatch(x -> x.getId() == idDispo);
        assertFalse(exists);

        idDispo = -1;
    }
}
