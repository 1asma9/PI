import hebergement.entities.Disponibilite;
import hebergement.entities.Hebergement;
import hebergement.entities.Reservation;
import hebergement.entities.TypeHebergement;
import hebergement.services.DisponibiliteService;
import hebergement.services.HebergementService;
import hebergement.services.ReservationService;
import hebergement.services.TypeHebergementService;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ReservationServiceTest {

    static ReservationService rs;
    static DisponibiliteService ds;
    static HebergementService hs;
    static TypeHebergementService ts;

    private int idType = -1;
    private int idHeb = -1;
    private int idDispo = -1;
    private int idRes = -1;

    @BeforeAll
    static void setup() {
        rs = new ReservationService();
        ds = new DisponibiliteService();
        hs = new HebergementService();
        ts = new TypeHebergementService();
    }

    @AfterEach
    void cleanUp() throws SQLException {

        if (idRes != -1) {
            Reservation r = new Reservation();
            r.setId(idRes);
            rs.deleteEntity(r);
            System.out.println("[DEBUG_LOG] Cleanup: Deleted Reservation with ID: " + idRes);
            idRes = -1;
        }

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

    private void createFixture(LocalDate debut, LocalDate fin) throws SQLException {
        String lib = "TEST_TYPE_" + System.currentTimeMillis();
        ts.addEntity(new TypeHebergement(lib));
        idType = ts.getIdByLibelle(lib);

        Hebergement h = new Hebergement();
        h.setDescription("HEB_TEST");
        h.setAdresse("ADDR_TEST");
        h.setPrix(200);
        h.setTypeId(idType);
        h.setImagePath("img/r.png");
        idHeb = hs.addEntityReturnId(h);

        Disponibilite d = new Disponibilite();
        d.setHebergementId(idHeb);
        d.setDateDebut(debut.minusDays(2));
        d.setDateFin(fin.plusDays(2));
        d.setDisponible(true);
        ds.addEntity(d);

        idDispo = ds.getByHebergement(idHeb).get(0).getId();
    }

    @Test
    @Order(1)
    void testCreateReservation() {
        try {
            LocalDate debut = LocalDate.now().plusDays(5);
            LocalDate fin = LocalDate.now().plusDays(8);
            createFixture(debut, fin);

            Reservation r = new Reservation();
            r.setHebergementId(idHeb);
            r.setClientNom("Client Test");
            r.setClientTel("123");
            r.setClientEmail("test@mail.com");
            r.setDateDebut(debut);
            r.setDateFin(fin);

            int nb = (int) ChronoUnit.DAYS.between(debut, fin);
            r.setNbNuits(nb);
            r.setTotal(nb * 200.0);
            r.setStatut("EN_ATTENTE");

            rs.addEntity(r);

            List<Reservation> list = rs.getByHebergement(idHeb);
            assertFalse(list.isEmpty());

            idRes = list.get(0).getId();
            System.out.println("[DEBUG_LOG] Created Reservation with ID: " + idRes);

            boolean found = list.stream().anyMatch(x -> x.getId() == idRes && "Client Test".equals(x.getClientNom()));
            assertTrue(found);

        } catch (SQLException e) {
            System.out.println("Exception in test: " + e.getMessage());
        }
    }

    @Test
    @Order(2)
    void testUpdateReservation() throws SQLException {
        LocalDate debut = LocalDate.now().plusDays(5);
        LocalDate fin = LocalDate.now().plusDays(8);
        createFixture(debut, fin);

        Reservation r = new Reservation();
        r.setHebergementId(idHeb);
        r.setClientNom("C1");
        r.setClientTel("111");
        r.setClientEmail("c1@mail.com");
        r.setDateDebut(debut);
        r.setDateFin(fin);
        r.setNbNuits((int) ChronoUnit.DAYS.between(debut, fin));
        r.setTotal(500);
        r.setStatut("EN_ATTENTE");

        rs.addEntity(r);
        idRes = rs.getByHebergement(idHeb).get(0).getId();

        Reservation updateInfo = new Reservation();
        updateInfo.setHebergementId(idHeb);
        updateInfo.setClientNom("C1_AFTER");
        updateInfo.setClientTel("222");
        updateInfo.setClientEmail("c1_after@mail.com");
        updateInfo.setDateDebut(debut);
        updateInfo.setDateFin(fin);
        updateInfo.setNbNuits((int) ChronoUnit.DAYS.between(debut, fin));
        updateInfo.setTotal(700);
        updateInfo.setStatut("EN_ATTENTE");

        rs.update(idRes, updateInfo);

        System.out.println("[DEBUG_LOG] Updated Reservation ID: " + idRes);

        List<Reservation> list = rs.getByHebergement(idHeb);
        boolean found = list.stream().anyMatch(x -> x.getId() == idRes && "C1_AFTER".equals(x.getClientNom()));
        assertTrue(found);
    }

    @Test
    @Order(3)
    void testDeleteReservation() throws SQLException {
        LocalDate debut = LocalDate.now().plusDays(5);
        LocalDate fin = LocalDate.now().plusDays(8);
        createFixture(debut, fin);

        Reservation r = new Reservation();
        r.setHebergementId(idHeb);
        r.setClientNom("C_DELETE");
        r.setClientTel("999");
        r.setClientEmail("x@mail.com");
        r.setDateDebut(debut);
        r.setDateFin(fin);
        r.setNbNuits((int) ChronoUnit.DAYS.between(debut, fin));
        r.setTotal(100);
        r.setStatut("EN_ATTENTE");

        rs.addEntity(r);
        idRes = rs.getByHebergement(idHeb).get(0).getId();

        Reservation del = new Reservation();
        del.setId(idRes);
        rs.deleteEntity(del);

        System.out.println("[DEBUG_LOG] Deleted Reservation ID: " + idRes);

        List<Reservation> list = rs.getByHebergement(idHeb);
        boolean exists = list.stream().anyMatch(x -> x.getId() == idRes);
        assertFalse(exists);

        idRes = -1;
    }
}
