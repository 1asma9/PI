package hebergement.tests;

import hebergement.entities.Disponibilite;
import hebergement.entities.Hebergement;
import hebergement.entities.TypeHebergement;
import hebergement.services.DisponibiliteService;
import hebergement.services.HebergementService;
import hebergement.services.TypeHebergementService;
import hebergement.tools.MyConnection;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;

public class MainClass {

    public static void main(String[] args) {

        try {
            if (!createTablesIfNotExists()) {
                System.out.println("Arrêt: impossible de se connecter à la base.");
                return;
            }

            TypeHebergementService ts = new TypeHebergementService();
            HebergementService hs = new HebergementService();
            DisponibiliteService ds = new DisponibiliteService();

            // ====== Seed types ======
            if (isTypeTableEmpty()) {
                ts.addEntity(new TypeHebergement("Appartement"));
                ts.addEntity(new TypeHebergement("Villa"));
                ts.addEntity(new TypeHebergement("Hotel"));
                System.out.println("Types insérés.");
            }

            Integer villaId = ts.getIdByLibelle("Villa");
            if (villaId == null) {
                System.out.println("Erreur: type 'Villa' introuvable.");
                return;
            }

            // ====== TEST CRUD HEBERGEMENT ======
            System.out.println("=== TEST CRUD HEBERGEMENT ===");

            hs.addEntity(new Hebergement("Villa avec piscine", "Sousse", 300.0, villaId));

            List<Hebergement> hebergements = hs.getData();
            System.out.println("LIST HEBERGEMENTS = " + hebergements);

            if (hebergements.isEmpty()) return;

            int lastHebId = hebergements.get(hebergements.size() - 1).getId();

            // Update last hebergement
            hs.update(lastHebId, new Hebergement("Villa eco (update)", "Bizerte", 220.0, villaId));
            System.out.println("Après UPDATE HEBERGEMENT = " + hs.getData());

            // ====== TEST CRUD DISPONIBILITE ======
            System.out.println("=== TEST CRUD DISPONIBILITE ===");

            Disponibilite dispo = new Disponibilite(
                    lastHebId,
                    LocalDate.now(),
                    LocalDate.now().plusDays(7),
                    true
            );

            ds.addEntity(dispo);

            List<Disponibilite> dispos = ds.getByHebergement(lastHebId);
            System.out.println("DISPOS pour hebergement " + lastHebId + " = " + dispos);

            if (!dispos.isEmpty()) {
                int lastDispoId = dispos.get(dispos.size() - 1).getId();

                // Update dispo
                Disponibilite d2 = new Disponibilite(
                        lastHebId,
                        LocalDate.now().plusDays(1),
                        LocalDate.now().plusDays(10),
                        false
                );
                ds.update(lastDispoId, d2);

                System.out.println("Après UPDATE DISPO = " + ds.getByHebergement(lastHebId));

                // Delete dispo
                Disponibilite toDelete = new Disponibilite();
                toDelete.setId(lastDispoId);
                ds.deleteEntity(toDelete);

                System.out.println("Après DELETE DISPO = " + ds.getByHebergement(lastHebId));
            }

            // Delete last hebergement (optionnel)
            // Hebergement hDel = new Hebergement();
            // hDel.setId(lastHebId);
            // hs.deleteEntity(hDel);

            System.out.println("Singleton hash: " +
                    MyConnection.getInstance().hashCode() + " - " +
                    MyConnection.getInstance().hashCode());

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            MyConnection.getInstance().close();
        }
    }

    private static boolean createTablesIfNotExists() {
        String sqlType = """
                CREATE TABLE IF NOT EXISTS type_hebergement (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    libelle VARCHAR(100) NOT NULL UNIQUE
                )
                """;

        String sqlHebergement = """
                CREATE TABLE IF NOT EXISTS hebergement (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    description VARCHAR(255),
                    adresse VARCHAR(255),
                    prix DOUBLE,
                    type_id INT NULL,
                    CONSTRAINT fk_hebergement_type
                        FOREIGN KEY (type_id) REFERENCES type_hebergement(id)
                        ON UPDATE CASCADE
                        ON DELETE SET NULL
                )
                """;

        String sqlDispo = """
                CREATE TABLE IF NOT EXISTS disponibilite (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    hebergement_id INT NOT NULL,
                    date_debut DATE NOT NULL,
                    date_fin DATE NOT NULL,
                    disponible BOOLEAN DEFAULT TRUE,
                    CONSTRAINT fk_dispo_hebergement
                        FOREIGN KEY (hebergement_id) REFERENCES hebergement(id)
                        ON DELETE CASCADE
                )
                """;

        Connection cnx = MyConnection.getInstance().getCnx();
        if (cnx == null) return false;

        try (Statement st = cnx.createStatement()) {
            st.execute(sqlType);
            st.execute(sqlHebergement);
            st.execute(sqlDispo);
            System.out.println("Tables prêtes: type_hebergement, hebergement, disponibilite.");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean isTypeTableEmpty() {
        String sql = "SELECT COUNT(*) AS c FROM type_hebergement";
        try (Statement st = MyConnection.getInstance().getCnx().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getInt("c") == 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
