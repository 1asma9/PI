import edu.destination.entities.Destination;
import edu.destination.entities.Transport;
import edu.destination.services.DestinationService;
import edu.destination.services.TransportService;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TransportTest {

    static TransportService TS;
    static DestinationService DS;
    private int idTransport = -1;
    private int idDestination = -1;

    @BeforeAll
    public static void setUp() {
        DS = new DestinationService();
        TS = new TransportService();
    }

    @AfterEach
    void cleanUp() throws SQLException {
        // Supprimer le transport ajouté
        if (idTransport != -1) {
            Transport t = new Transport();
            t.setIdTransport(idTransport);
            TS.deleteEntity(t);
            System.out.println("[DEBUG_LOG] Cleanup: Deleted transport with id " + idTransport);
            idTransport = -1;
        }
        // Supprimer la destination ajoutée
        if (idDestination != -1) {
            Destination d = new Destination();
            d.setIdDestination(idDestination);
            DS.deleteEntity(d);
            System.out.println("[DEBUG_LOG] Cleanup: Deleted destination with id " + idDestination);
            idDestination = -1;
        }
    }

    @Test
    @Order(1)
    public void testCreateTransport() throws SQLException {
        Destination dest = new Destination(
                "Dest2",
                "Pays2",
                "Desc2",
                true,
                "Avril",
                0.0,
                0.0,
                0,
                200.0,
                LocalDate.of(2026, 4, 1),   // dateDepart
                LocalDate.of(2026, 4, 10)   // dateArrivee
        );        DS.addEntity(dest);
        List<Destination> destinations = DS.getData();
        Destination lastDest = destinations.get(destinations.size() - 1);
        idDestination = lastDest.getIdDestination();

        Transport t = new Transport("Bus", idDestination);
        TS.addEntity(t);

        List<Transport> transports = TS.getData();
        assertFalse(transports.isEmpty());
        Transport created = transports.get(transports.size() - 1);
        idTransport = created.getIdTransport();

        assertTrue(idTransport > 0);
        boolean found = transports.stream()
                .anyMatch(tr -> tr.getIdTransport() == idTransport && tr.getTypeTransport().equals("Bus"));
        assertTrue(found, "Transport with type 'Bus' should exist");
    }

    @Test
    @Order(2)
    public void testCreateTransportWithAddEntity2() throws SQLException {
        Destination dest = new Destination(
                "Dest2",
                "Pays2",
                "Desc2",
                true,
                "Avril",
                0.0,
                0.0,
                0,
                200.0,
                LocalDate.of(2026, 4, 1),   // dateDepart
                LocalDate.of(2026, 4, 10)   // dateArrivee
        );        DS.addEntity(dest);
        List<Destination> destinations = DS.getData();
        Destination lastDest = destinations.get(destinations.size() - 1);
        idDestination = lastDest.getIdDestination();

        Transport t = new Transport("Ferry", idDestination);
        TS.addEntity2(t);

        List<Transport> transports = TS.getData();
        assertFalse(transports.isEmpty());
        Transport created = transports.get(transports.size() - 1);
        int id = created.getIdTransport();
        idTransport = id;

        assertTrue(id > 0);
        boolean found = transports.stream()
                .anyMatch(tr -> tr.getIdTransport() == id && tr.getTypeTransport().equals("Ferry"));
        assertTrue(found, "Transport with type 'Ferry' should exist");
    }

    @Test
    @Order(3)
    public void testUpdateTransport() throws SQLException {
        Destination dest = new Destination(
                "Dest2",
                "Pays2",
                "Desc2",
                true,
                "Avril",
                0.0,
                0.0,
                0,
                200.0,
                LocalDate.of(2026, 4, 1),   // dateDepart
                LocalDate.of(2026, 4, 10)   // dateArrivee
        );        DS.addEntity(dest);
        List<Destination> destinations = DS.getData();
        Destination lastDest = destinations.get(destinations.size() - 1);
        idDestination = lastDest.getIdDestination();

        Transport t = new Transport("Train", idDestination);
        TS.addEntity(t);
        List<Transport> transports = TS.getData();
        Transport created = transports.get(transports.size() - 1);
        idTransport = created.getIdTransport();

        Transport updateInfo = new Transport("Train Express", idDestination);
        TS.update(idTransport, updateInfo);

        List<Transport> updatedList = TS.getData();
        boolean found = updatedList.stream()
                .anyMatch(tr -> tr.getIdTransport() == idTransport && tr.getTypeTransport().equals("Train Express"));
        assertTrue(found, "Transport should be updated with type 'Train Express'");
    }

    @Test
    @Order(4)
    public void testDeleteTransport() throws SQLException {
        Destination dest = new Destination(
                "Dest2",
                "Pays2",
                "Desc2",
                true,
                "Avril",
                0.0,
                0.0,
                0,
                200.0,
                LocalDate.of(2026, 4, 1),   // dateDepart
                LocalDate.of(2026, 4, 10)   // dateArrivee
        );        DS.addEntity(dest);
        List<Destination> destinations = DS.getData();
        Destination lastDest = destinations.get(destinations.size() - 1);
        idDestination = lastDest.getIdDestination();

        Transport t = new Transport("Avion", idDestination);
        TS.addEntity(t);
        List<Transport> transports = TS.getData();
        Transport created = transports.get(transports.size() - 1);
        int id = created.getIdTransport();

        Transport toDelete = new Transport();
        toDelete.setIdTransport(id);
        TS.deleteEntity(toDelete);

        List<Transport> updatedList = TS.getData();
        boolean exists = updatedList.stream().anyMatch(tr -> tr.getIdTransport() == id);
        assertFalse(exists, "Transport should be deleted");
    }

    @Test
    @Order(5)
    public void testGetData() {
        List<Transport> transports = TS.getData();
        assertNotNull(transports);
    }

    @Test
    public void testGetTransportsByDestination() throws SQLException {
        Destination dest = new Destination(
                "Dest2",
                "Pays2",
                "Desc2",
                true,
                "Avril",
                0.0,
                0.0,
                0,
                200.0,
                LocalDate.of(2026, 4, 1),   // dateDepart
                LocalDate.of(2026, 4, 10)   // dateArrivee
        );        DS.addEntity2(dest);
        Destination createdDest = DS.getData().get(DS.getData().size() - 1);
        int destId = createdDest.getIdDestination();

        Transport t1 = new Transport("Bus", destId);
        Transport t2 = new Transport("Train", destId);

        TS.addEntity2(t1);
        TS.addEntity2(t2);

        List<Transport> filtered = TS.getTransportsByDestination(destId);

        assertNotNull(filtered);
        assertTrue(filtered.size() >= 2);
        for (Transport t : filtered) {
            assertEquals(destId, t.getIdDestination());
        }
    }
}