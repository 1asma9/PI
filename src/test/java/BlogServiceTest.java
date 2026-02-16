
import edu.connexion3a8.entities.Blog;
import edu.connexion3a8.services.BlogService;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BlogServiceTest {

    static BlogService blogService;
    private Integer blogId = null;

    @BeforeAll
    static void setup() {
        blogService = new BlogService();
    }

    @Test
    @Order(1)
    void testAddBlog() throws SQLException {

        Blog blog = new Blog();
        blog.setTitre("Test Blog");
        blog.setContenu("Contenu du blog pour test unitaire");
        blog.setAuthor_nom("Asma");

        blogService.ajouter(blog);

        List<Blog> blogs = blogService.afficher();
        assertFalse(blogs.isEmpty());

        blogId = blogs.get(blogs.size() - 1).getId();
        assertNotNull(blogId);

        System.out.println("[DEBUG] Blog ajouté ID: " + blogId);
    }

    @Test
    @Order(2)
    void testUpdateBlog() throws SQLException {

        Blog blog = new Blog();
        blog.setId(blogId);
        blog.setTitre("Blog Modifié");
        blog.setContenu("Contenu modifié");
        blog.setAuthor_nom("Asma");

        blogService.modifier(blog);

        Blog updated = blogService.getById(blogId);

        assertEquals("Blog Modifié", updated.getTitre());
        assertEquals("Contenu modifié", updated.getContenu());
    }

    @Test
    @Order(3)
    void testDeleteBlog() throws SQLException {

        Blog blog = new Blog();
        blog.setId(blogId);

        blogService.supprimer(blog.getId());

        Blog deleted = blogService.getById(blogId);
        assertNull(deleted);

        System.out.println("[DEBUG] Blog supprimé");
    }
}