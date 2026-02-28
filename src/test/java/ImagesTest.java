import edu.destination.entities.Destination;
import edu.destination.entities.DestinationImage;
import edu.destination.services.DestinationService;
import edu.destination.services.ImageService;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ImagesTest {

    static ImageService IS;
    static DestinationService DS;
    private int idImage = -1;
    private int idDestination = -1;

    @BeforeAll
    public static void setUp() {
        DS = new DestinationService();
        IS = new ImageService();
    }

    @AfterEach
    void cleanUp() throws SQLException {
        if (idImage != -1) {
            DestinationImage img = new DestinationImage();
            img.setIdImage(idImage);
            IS.deleteEntity(img);
            System.out.println("[DEBUG_LOG] Cleanup: Deleted image with id " + idImage);
            idImage = -1;
        }
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
    public void testCreateImage() throws SQLException {
        // On crée d'abord une destination pour la clé étrangère
        Destination d = new Destination("Europe", "France", "Paris Tour", true, "Juin", 48.8566, 2.3522, 0);
        DS.addEntity2(d);

        List<Destination> destinations = DS.getData();
        Destination createdDest = destinations.get(destinations.size() - 1);
        idDestination = createdDest.getIdDestination();

        // Créer l'image
        DestinationImage img = new DestinationImage("photo1.jpg", idDestination);
        IS.addEntity(img);

        List<DestinationImage> images = IS.getData();
        DestinationImage created = images.get(images.size() - 1);
        idImage = created.getIdImage();

        System.out.println("[DEBUG_LOG] Created image with ID : " + idImage);

        assertTrue(idImage > 0, "Image ID should be greater than 0");

        boolean found = images.stream()
                .anyMatch(im -> im.getIdImage() == idImage && im.getUrlImage().equals("photo1.jpg"));
        assertTrue(found, "Image with generated ID and URL 'photo1.jpg' should exist");
    }

    @Test
    @Order(2)
    public void testCreateImageWithAddEntity2() throws SQLException {
        // Créer destination
        Destination d = new Destination("Europe", "Germany", "Berlin Tour", true, "Juillet", 52.52, 13.405, 0);
        DS.addEntity2(d);

        Destination createdDest = DS.getData().get(DS.getData().size() - 1);
        idDestination = createdDest.getIdDestination();

        // Créer image avec addEntity2
        DestinationImage img = new DestinationImage("photo2.jpg", idDestination);
        IS.addEntity2(img); // void

        // récupérer dernière image pour l'ID
        List<DestinationImage> images = IS.getData();
        DestinationImage created = images.get(images.size() - 1);
        idImage = created.getIdImage();

        System.out.println("[DEBUG_LOG] Created image with ID : " + idImage);

        assertTrue(idImage > 0, "Generated ID should be greater than 0");

        boolean found = images.stream()
                .anyMatch(im -> im.getIdImage() == idImage && im.getUrlImage().equals("photo2.jpg"));
        assertTrue(found, "Image with generated ID and URL 'photo2.jpg' should exist");
    }

    @Test
    @Order(3)
    public void testUpdateImage() throws SQLException {
        // Créer destination
        Destination d = new Destination("Europe", "Italy", "Rome Tour", true, "Août", 41.9028, 12.4964, 0);
        DS.addEntity2(d);

        Destination createdDest = DS.getData().get(DS.getData().size() - 1);
        idDestination = createdDest.getIdDestination();

        DestinationImage img = new DestinationImage("photo3.jpg", idDestination);
        IS.addEntity(img);

        List<DestinationImage> images = IS.getData();
        DestinationImage created = images.get(images.size() - 1);
        idImage = created.getIdImage();

        // Mise à jour
        DestinationImage updateInfo = new DestinationImage();
        updateInfo.setIdImage(idImage);
        updateInfo.setUrlImage("photo3_updated.jpg");
        updateInfo.setIdDestination(idDestination);

        IS.update(idImage, updateInfo);

        List<DestinationImage> updatedList = IS.getData();
        boolean found = updatedList.stream()
                .anyMatch(im -> im.getIdImage() == idImage && im.getUrlImage().equals("photo3_updated.jpg"));
        assertTrue(found, "Image should be updated to 'photo3_updated.jpg'");
    }

    @Test
    @Order(4)
    public void testDeleteImage() throws SQLException {
        // Créer destination
        Destination d = new Destination("Europe", "Spain", "Madrid Tour", true, "Septembre", 40.4168, -3.7038, 0);
        DS.addEntity2(d);
        Destination createdDest = DS.getData().get(DS.getData().size() - 1);
        idDestination = createdDest.getIdDestination();

        DestinationImage img = new DestinationImage("photo4.jpg", idDestination);
        IS.addEntity(img);

        List<DestinationImage> images = IS.getData();
        DestinationImage created = images.get(images.size() - 1);
        idImage = created.getIdImage();

        // Supprimer
        DestinationImage toDelete = new DestinationImage();
        toDelete.setIdImage(idImage);
        IS.deleteEntity(toDelete);

        List<DestinationImage> updatedList = IS.getData();
        boolean exists = updatedList.stream()
                .anyMatch(im -> im.getIdImage() == idImage);
        assertFalse(exists, "Image should be deleted");
    }

    @Test
    @Order(5)
    public void testGetData() {
        List<DestinationImage> images = IS.getData();
        assertNotNull(images);
    }
    @Test
    @Order(6)
    public void testGetImagesByDestination() throws SQLException {

        //  Créer destination
        Destination d = new Destination("Europe", "Portugal", "Lisbon Tour", true,
                "Octobre", 38.7223, -9.1393, 0);

        DS.addEntity2(d);

        Destination createdDest = DS.getData()
                .get(DS.getData().size() - 1);

        idDestination = createdDest.getIdDestination();

        //  Ajouter 2 images liées à cette destination
        DestinationImage img1 = new DestinationImage("lisbon1.jpg", idDestination);
        DestinationImage img2 = new DestinationImage("lisbon2.jpg", idDestination);

        IS.addEntity(img1);
        IS.addEntity(img2);

        // récupérer IDs pour cleanup
        List<DestinationImage> allImages = IS.getData();
        idImage = allImages.get(allImages.size() - 1).getIdImage();

        //  Tester la méthode
        List<DestinationImage> filtered =
                IS.getImagesByDestination(idDestination);

        assertNotNull(filtered);
        assertTrue(filtered.size() >= 2,
                "Should return at least 2 images");

        for (DestinationImage img : filtered) {
            assertEquals(idDestination, img.getIdDestination(),
                    "Image must belong to correct destination");
        }
    }

}
