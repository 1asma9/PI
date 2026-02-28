
import edu.destination.entities.Destination;
import edu.destination.services.DestinationService;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DestinationTest {

    private static DestinationService DS;
    private int idDestination = -1;

    @BeforeAll
    public static void setUp() {
        DS = new DestinationService();
        System.out.println("[DEBUG_LOG] DestinationService initialized.");
    }

    @AfterEach
    void cleanUp() throws SQLException {
        if (idDestination != -1) {
            Destination d = new Destination();
            d.setIdDestination(idDestination);
            DS.deleteEntity(d);
            System.out.println("[DEBUG_LOG] Cleanup: Deleted destination with ID " + idDestination);
            idDestination = -1;
        }
    }

    @Test
    @Order(1)
    public void testCreateDestination() throws SQLException {
        Destination d = new Destination(
                "Africa",
                "Tunisia",
                "Découverte du Sahara",
                true,
                "Octobre",
                33.8869,
                9.5375,
                5000,
                1200.0,
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 6, 10)
        );

        DS.addEntity(d);

        List<Destination> destinations = DS.getData();
        assertFalse(destinations.isEmpty(), "Destination list should not be empty");

        Destination created = destinations.get(destinations.size() - 1);
        idDestination = created.getIdDestination();

        System.out.println("[DEBUG_LOG] Created destination with ID: " + idDestination);

        assertTrue(idDestination > 0, "Destination ID should be greater than 0");

        boolean found = destinations.stream()
                .anyMatch(dest -> dest.getIdDestination() == idDestination &&
                        dest.getNom().equals("Africa"));
        assertTrue(found, "Created destination should exist in the list");
    }

    @Test
    @Order(2)
    public void testCreateDestinationWithAddEntity2() throws SQLException {
        Destination d = new Destination(
                "Africa",
                "Tunisia",
                "Découverte du Sahara",
                true,
                "Octobre",
                33.8869,
                9.5375,
                5000,
                1200.0,
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 6, 10)
        );

        DS.addEntity2(d);

        List<Destination> destinations = DS.getData();
        assertFalse(destinations.isEmpty(), "Destination list should not be empty");

        Destination created = destinations.get(destinations.size() - 1);
        idDestination = created.getIdDestination();

        System.out.println("[DEBUG_LOG] Created destination with ID: " + idDestination);

        assertTrue(idDestination > 0, "Destination ID should be greater than 0");

        boolean found = destinations.stream()
                .anyMatch(dest -> dest.getIdDestination() == idDestination &&
                        dest.getNom().equals("Africa"));
        assertTrue(found, "Created destination should exist in the list");
    }

    @Test
    @Order(3)
    public void testUpdateDestination() throws SQLException {
        Destination d = new Destination(
                "Africa",
                "Tunisia",
                "Découverte du Sahara",
                true,
                "Octobre",
                33.8869,
                9.5375,
                5000,
                1200.0,
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 6, 10)
        );

        DS.addEntity(d);

        List<Destination> destinations = DS.getData();
        Destination created = destinations.get(destinations.size() - 1);
        idDestination = created.getIdDestination();

        Destination updateInfo = new Destination(
                "Africa",
                "TUNISIA",
                "Découverte du Sahara",
                true,
                "Octobre",
                33.8869,
                9.5375,
                5000,
                1200.0,
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 6, 10)
        );

        DS.update(idDestination, updateInfo);

        List<Destination> updatedList = DS.getData();
        boolean found = updatedList.stream()
                .anyMatch(dest -> dest.getIdDestination() == idDestination &&
                        dest.getPays().equals("TUNISIA"));
        assertTrue(found, "Destination should have updated country 'TUNISIA'");
    }

    @Test
    @Order(4)
    public void testDeleteDestination() throws SQLException {
        Destination d = new Destination(
                "Africa",
                "Tunisia",
                "Découverte du Sahara",
                true,
                "Octobre",
                33.8869,
                9.5375,
                5000,
                1200.0,
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 6, 10)
        );

        DS.addEntity(d);

        List<Destination> list = DS.getData();
        Destination created = list.get(list.size() - 1);
        int idToDelete = created.getIdDestination();

        Destination toDelete = new Destination();
        toDelete.setIdDestination(idToDelete);
        DS.deleteEntity(toDelete);

        List<Destination> updatedList = DS.getData();
        boolean exists = updatedList.stream()
                .anyMatch(dest -> dest.getIdDestination() == idToDelete);
        assertFalse(exists, "Destination should be deleted");
    }

    @Test
    @Order(5)
    public void testGetData() {
        List<Destination> destinations = DS.getData();
        assertNotNull(destinations, "Destination list should not be null");
    }
}