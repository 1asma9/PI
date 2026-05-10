package org.example.PI_Gestion_des_utilisateurs.ui;

import hebergement.controllers.AdminLayoutController;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.example.PI_Gestion_des_utilisateurs.entities.utilisateur;
import org.example.PI_Gestion_des_utilisateurs.services.utilisateur_service;
import tools.AlertHelper;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class ListUsersController {

    @FXML private Label lblTitleCount;
    @FXML private TableView<utilisateur> tableUsers;

    @FXML private TableColumn<utilisateur, Integer> colId;
    @FXML private TableColumn<utilisateur, String> colNom;
    @FXML private TableColumn<utilisateur, String> colPrenom;
    @FXML private TableColumn<utilisateur, String> colEmail;
    @FXML private TableColumn<utilisateur, String> colTelephone;
    @FXML private TableColumn<utilisateur, String> colRole;
    @FXML private TableColumn<utilisateur, String> colStatut;
    @FXML private TableColumn<utilisateur, String> colDate;
    @FXML private TableColumn<utilisateur, utilisateur> colActions;

    private final utilisateur_service service = new utilisateur_service();
    private ObservableList<utilisateur> userList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTable();
        loadData();
    }

    private void setupTable() {
        // ID
        colId.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getId()));
        colId.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.valueOf(item));
                    setStyle("-fx-text-fill: #a0a0a0; -fx-alignment: CENTER-LEFT; -fx-padding: 0 0 0 10px;");
                }
            }
        });

        // Nom
        colNom.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getNom()));
        colNom.setCellFactory(createOrangeTextCell());

        // Prenom
        colPrenom.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getPrenom()));
        colPrenom.setCellFactory(createOrangeTextCell());

        // Email
        colEmail.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getEmail()));
        colEmail.setCellFactory(createGrayTextCell());

        // Telephone
        colTelephone.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getTelephone()));
        colTelephone.setCellFactory(createGrayTextCell());

        // Role
        colRole.setCellValueFactory(data -> {
            String role = data.getValue().getRoleName();
            return new ReadOnlyStringWrapper(role != null ? role : "Aucun rôle");
        });
        colRole.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label badge = new Label(item);
                    if ("Aucun rôle".equals(item)) {
                        badge.setStyle("-fx-text-fill: #888888; -fx-font-style: italic;");
                    } else {
                        badge.setStyle("-fx-background-color: #d97706; -fx-text-fill: #fffbeb; -fx-padding: 3 10; -fx-background-radius: 12; -fx-font-size: 11px; -fx-font-weight: bold;");
                    }
                    setGraphic(badge);
                    setStyle("-fx-alignment: CENTER-LEFT;");
                }
            }
        });

        // Statut
        colStatut.setCellValueFactory(data -> {
            boolean active = data.getValue().isActive();
            return new ReadOnlyStringWrapper(active ? "Actif" : "Inactif");
        });
        colStatut.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label badge = new Label(item);
                    if ("Actif".equals(item)) {
                        badge.setStyle("-fx-background-color: #065f46; -fx-text-fill: #a7f3d0; -fx-padding: 3 10; -fx-background-radius: 12; -fx-font-size: 11px; -fx-font-weight: bold;");
                    } else {
                        badge.setStyle("-fx-background-color: #991b1b; -fx-text-fill: #fecaca; -fx-padding: 3 10; -fx-background-radius: 12; -fx-font-size: 11px; -fx-font-weight: bold;");
                    }
                    setGraphic(badge);
                    setStyle("-fx-alignment: CENTER-LEFT;");
                }
            }
        });

        // Date de création
        colDate.setCellValueFactory(data -> {
            if (data.getValue().getDateCreation() != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy\nHH:mm");
                return new ReadOnlyStringWrapper(data.getValue().getDateCreation().format(formatter));
            }
            return new ReadOnlyStringWrapper("");
        });
        colDate.setCellFactory(createGrayTextCell());

        // Actions
        colActions.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue()));
        colActions.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(utilisateur user, boolean empty) {
                super.updateItem(user, empty);
                if (empty || user == null) {
                    setGraphic(null);
                } else {
                    Button btnEdit = new Button("✏");
                    btnEdit.setStyle("-fx-background-color: transparent; -fx-border-color: #d97706; -fx-border-radius: 4; -fx-text-fill: #d97706; -fx-cursor: hand;");
                    btnEdit.setOnAction(e -> onEdit(user));

                    Button btnSuspend = new Button(user.isActive() ? "🚫" : "✅");
                    btnSuspend.setStyle("-fx-background-color: transparent; -fx-border-color: #d97706; -fx-border-radius: 4; -fx-text-fill: #d97706; -fx-cursor: hand;");
                    btnSuspend.setOnAction(e -> onToggleStatus(user));
                    
                    Button btnDelete = new Button("🗑");
                    btnDelete.setStyle("-fx-background-color: transparent; -fx-border-color: #d97706; -fx-border-radius: 4; -fx-text-fill: #d97706; -fx-cursor: hand;");
                    btnDelete.setOnAction(e -> onDelete(user));

                    HBox actionBox = new HBox(8, btnEdit, btnSuspend, btnDelete);
                    actionBox.setAlignment(Pos.CENTER_LEFT);
                    setGraphic(actionBox);
                }
            }
        });
    }

    private javafx.util.Callback<TableColumn<utilisateur, String>, TableCell<utilisateur, String>> createOrangeTextCell() {
        return col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    setStyle("-fx-text-fill: #ea580c; -fx-font-weight: bold; -fx-alignment: CENTER-LEFT;");
                }
            }
        };
    }

    private javafx.util.Callback<TableColumn<utilisateur, String>, TableCell<utilisateur, String>> createGrayTextCell() {
        return col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    setStyle("-fx-text-fill: #9ca3af; -fx-alignment: CENTER-LEFT;");
                }
            }
        };
    }

    private void loadData() {
        List<utilisateur> users = service.afficherutilisateurs();
        userList.setAll(users);
        tableUsers.setItems(userList);
        lblTitleCount.setText("Utilisateurs (" + users.size() + ")");
    }

    @FXML
    public void onAddUser() {
        if (AdminLayoutController.getInstance() != null) {
            AdminLayoutController.getInstance().loadPage("/app/add_user.fxml", "Ajouter Utilisateur");
        }
    }

    private void onEdit(utilisateur user) {
        // Enregistrer l'utilisateur sélectionné pour l'édition via SessionManager
        tools.SessionManager.setTempUser(user);
        if (AdminLayoutController.getInstance() != null) {
            AdminLayoutController.getInstance().loadPage("/app/edit_user.fxml", "Modifier Utilisateur");
        }
    }

    private void onToggleStatus(utilisateur user) {
        boolean newStatus = !user.isActive();
        String action = newStatus ? "activer" : "suspendre";
        boolean confirmed = AlertHelper.showConfirmation("Changement de statut", "Voulez-vous vraiment " + action + " l'utilisateur " + user.getNom() + " ?");
        
        if (confirmed) {
            if (service.changerStatutUtilisateur(user.getId(), newStatus)) {
                loadData();
            } else {
                AlertHelper.showError("Erreur", "Impossible de changer le statut.");
            }
        }
    }

    private void onDelete(utilisateur user) {
        boolean confirmed = AlertHelper.showConfirmation("Suppression", "Voulez-vous vraiment supprimer " + user.getNom() + " ?");
        if (confirmed) {
            if (service.supprimerutilisateur(user.getId())) {
                loadData();
            } else {
                AlertHelper.showError("Erreur", "Impossible de supprimer l'utilisateur.");
            }
        }
    }
}
