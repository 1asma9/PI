package edu.destination.services;

import edu.destination.entities.Image;
import edu.destination.interfaces.IService;
import edu.destination.tools.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ImageService implements IService<Image> {

    // ================= AJOUT =================
    @Override
    public void addEntity(Image image) throws SQLException {
        String sql = "INSERT INTO image (url_image, destination_id) VALUES (?, ?)";
        PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(sql);
        pst.setString(1, image.getUrlImage());
        pst.setInt(2, image.getDestinationId());
        pst.executeUpdate();
        System.out.println("Image ajoutée");
    }

    // ================= AJOUT2 =================
    @Override
    public void addEntity2(Image image) throws SQLException {
        addEntity(image);
    }

    // ================= SUPPRESSION =================
    @Override
    public void deleteEntity(Image image) {
        String sql = "DELETE FROM image WHERE id=?";
        try {
            PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(sql);
            pst.setInt(1, image.getId());
            pst.executeUpdate();
            System.out.println("Image supprimée");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // ================= MODIFICATION =================
    @Override
    public void update(int id, Image image) {
        String sql = "UPDATE image SET url_image=?, destination_id=? WHERE id=?";
        try {
            PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(sql);
            pst.setString(1, image.getUrlImage());
            pst.setInt(2, image.getDestinationId());
            pst.setInt(3, id);
            pst.executeUpdate();
            System.out.println("Image modifiée");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // ================= AFFICHAGE =================
    @Override
    public List<Image> getData() {
        List<Image> list = new ArrayList<>();
        String sql = "SELECT * FROM image";
        try {
            Statement st = MyConnection.getInstance().getCnx().createStatement();
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                Image img = new Image();
                img.setId(rs.getInt("id"));
                img.setUrlImage(rs.getString("url_image"));
                img.setDestinationId(rs.getInt("destination_id"));
                list.add(img);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return list;
    }

    // ================= FILTRAGE PAR DESTINATION =================
    public List<Image> getImagesByDestination(int destinationId) {
        List<Image> list = new ArrayList<>();
        String sql = "SELECT * FROM image WHERE destination_id=?";
        try {
            PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(sql);
            pst.setInt(1, destinationId);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                Image img = new Image();
                img.setId(rs.getInt("id"));
                img.setUrlImage(rs.getString("url_image"));
                img.setDestinationId(rs.getInt("destination_id"));
                list.add(img);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return list;
    }
}