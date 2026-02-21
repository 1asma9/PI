package hebergement.controllers;

import hebergement.services.DisponibiliteService;
import hebergement.services.HebergementService;
import hebergement.services.TypeHebergementService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class DashboardController {

    @FXML private Label lblHeb;
    @FXML private Label lblTypes;
    @FXML private Label lblDispo;

    private final HebergementService hs = new HebergementService();
    private final TypeHebergementService ts = new TypeHebergementService();
    private final DisponibiliteService ds = new DisponibiliteService();

    @FXML
    public void initialize() {
        refresh();
    }

    @FXML
    public void refresh() {
        try {
            lblHeb.setText(String.valueOf(hs.getData().size()));
            lblTypes.setText(String.valueOf(ts.getData().size()));

            int total = 0;
            for (var h : hs.getData()) total += ds.getByHebergement(h.getId()).size();
            lblDispo.setText(String.valueOf(total));
        } catch (Exception e) {
            lblHeb.setText("-");
            lblTypes.setText("-");
            lblDispo.setText("-");
            e.printStackTrace();
        }
    }
}
