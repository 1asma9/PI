import edu.connexion3a8.entities.Blog;
import edu.connexion3a8.entities.Commentaire;
import edu.connexion3a8.services.BlogService;
import edu.connexion3a8.services.CommentaireService;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CommentaireServiceTest {

    static CommentaireService commentaireService;
    static BlogService blogService;

    private Integer blogId = null;
    private Integer commentaireId = null;

    @BeforeAll
    static void setup() {
        commentaireService = new CommentaireService();
        blogService = new BlogService();
    }

    @Test
    @Order(1)
    void testAddCommentaire() throws SQLException {

        // Création d’un blog pour rattacher le commentaire
        Blog blog = new Blog();
        blog.setTitre("Blog Test Commentaire");
        blog.setContenu("Contenu");
        blog.setAuthor_nom("Asma");

        blogService.ajouter(blog);

        List<Blog> blogs = blogService.afficher();
        blogId = blogs.get(blogs.size() - 1).getId();

        // Création commentaire
        Commentaire c = new Commentaire();
        c.setContenu("Ceci est un commentaire test");
        c.setNomuser("UserTest");
        c.setBlogId(blogId);

        commentaireService.ajouter(Commentaire c, int blogId);

        List<Commentaire> commentaires = commentaireService.afficher();
        assertFalse(commentaires.isEmpty());

        commentaireId = commentaires.get(commentaires.size() - 1).getId();
        assertNotNull(commentaireId);

        System.out.println("[DEBUG] Commentaire ajouté ID: " + commentaireId);
    }

    @Test
    @Order(2)
    void testUpdateCommentaire() throws SQLException {

        Commentaire c = new Commentaire();
        c.setId(commentaireId);
        c.setContenu("Commentaire modifié");
        c.setNomuser("UserTest");
        c.setBlogId(blogId);

        commentaireService.modifier(c);

        Commentaire updated = commentaireService.getById(commentaireId);

        assertEquals("Commentaire modifié", updated.getContenu());
    }

    @Test
    @Order(3)
    void testDeleteCommentaire() throws SQLException {

        Commentaire c = new Commentaire();
        c.setId(commentaireId);

        commentaireService.supprimer(commentaireId);

        Commentaire deleted = commentaireService.getById(commentaireId);
        assertNull(deleted);

        // Nettoyage blog
        Blog blog = new Blog();
        blog.setId(blogId);
        blogService.supprimer(blogId);

        System.out.println("[DEBUG] Commentaire et Blog supprimés");
    }
}