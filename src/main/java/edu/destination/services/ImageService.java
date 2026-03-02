package edu.destination.services;

import edu.destination.entities.DestinationImage;
import edu.destination.interfaces.IService;
import edu.destination.tools.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ImageService implements IService<DestinationImage> {

    // ================= AJOUT (Statement simple) =================
    @Override
    public void addEntity(DestinationImage image) throws SQLException {

        String sql = "INSERT INTO destination_image (url_image, id_destination) " +
                "VALUES ('" + image.getUrlImage() + "'," + image.getIdDestination() + ")";

        Statement st = new MyConnection().getCnx().createStatement();
        st.executeUpdate(sql);
        System.out.println("Image ajoutée");
    }

    // ================= AJOUT (PreparedStatement) =================
    @Override
    public void addEntity2(DestinationImage image) throws SQLException {

        String sql = "INSERT INTO destination_image (url_image, id_destination) VALUES (?, ?)";
        PreparedStatement pst = new MyConnection().getCnx().prepareStatement(sql);

        pst.setString(1, image.getUrlImage());
        pst.setInt(2, image.getIdDestination());

        pst.executeUpdate();
        System.out.println("Image ajoutée");
    }

    // ================= SUPPRESSION =================
    @Override
    public void deleteEntity(DestinationImage image) {

        String sql = "DELETE FROM destination_image WHERE id_image=?";

        try {
            PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(sql);
            pst.setInt(1, image.getIdImage());
            pst.executeUpdate();
            System.out.println("Image supprimée");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // ================= MODIFICATION =================
    @Override
    public void update(int id, DestinationImage image) {

        String sql = "UPDATE destination_image SET url_image=?, id_destination=? WHERE id_image=?";

        try {
            PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(sql);
            pst.setString(1, image.getUrlImage());
            pst.setInt(2, image.getIdDestination());
            pst.setInt(3, id);

            pst.executeUpdate();
            System.out.println("Image modifiée");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // ================= AFFICHAGE =================
    @Override
    public List<DestinationImage> getData() {

        List<DestinationImage> list = new ArrayList<>();
        String sql = "SELECT * FROM destination_image";

        try {
            Statement st = new MyConnection().getCnx().createStatement();
            ResultSet rs = st.executeQuery(sql);

            while (rs.next()) {
                DestinationImage img = new DestinationImage();

                img.setIdImage(rs.getInt("id_image"));
                img.setUrlImage(rs.getString("url_image"));
                img.setIdDestination(rs.getInt("id_destination"));

                list.add(img);
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return list;
    }
    // ================= FILTRAGE PAR DESTINATION (OPTIMISÉ SQL) =================
    public List<DestinationImage> getImagesByDestination(int idDestination) {

        List<DestinationImage> list = new ArrayList<>();
        String sql = "SELECT * FROM destination_image WHERE id_destination = ?";

        try {
            PreparedStatement pst = MyConnection.getInstance()
                    .getCnx()
                    .prepareStatement(sql);

            pst.setInt(1, idDestination);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                DestinationImage img = new DestinationImage();
                img.setIdImage(rs.getInt("id_image"));
                img.setUrlImage(rs.getString("url_image"));
                img.setIdDestination(rs.getInt("id_destination"));
                list.add(img);
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return list;
    }


}
