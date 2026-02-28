package edu.destination.controllers;

import edu.destination.entities.Destination;
import edu.destination.services.DestinationService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

public class AdminDestinationFormController {

    // ============================================================
    // 🔑 CLÉ API GROQ
    // ============================================================
    private static final String GROQ_API_KEY = "";

    // ==============================
    // FXML — champs
    // ==============================
    @FXML private TextField  txtNom, txtPays, txtDescription, txtSaison;
    @FXML private TextField  txtLatitude, txtLongitude, txtNbVisites;
    @FXML private TextField  txtPrix;
    @FXML private CheckBox   chkStatut;
    @FXML private Button     btnSave, btnCancel;
    @FXML private DatePicker dateDepart;
    @FXML private DatePicker dateArrivee;

    // Labels d'erreur
    @FXML private Label errNom, errPays, errDescription, errSaison;
    @FXML private Label errLatitude, errLongitude, errNbVisites;
    @FXML private Label errPrix, errDateDepart, errDateArrivee;

    // IA
    @FXML private Button btnGenererDescription;
    @FXML private Label  lblIaStatus;

    private final DestinationService service = new DestinationService();
    private Destination destination;

    // ==============================
    // INIT
    // ==============================
    public void setDestination(Destination destination) {
        this.destination = destination;

        if (destination != null) {
            txtNom.setText(destination.getNom());
            txtPays.setText(destination.getPays());
            txtDescription.setText(destination.getDescription());
            txtSaison.setText(destination.getMeilleureSaison());

            txtLatitude.setText(String.valueOf(destination.getLatitude()));
            txtLongitude.setText(String.valueOf(destination.getLongitude()));
            txtNbVisites.setText(String.valueOf(destination.getNbVisites()));
            txtPrix.setText(String.valueOf(destination.getPrix()));

            if (destination.getDateDepart()  != null) dateDepart.setValue(destination.getDateDepart());
            if (destination.getDateArrivee() != null) dateArrivee.setValue(destination.getDateArrivee());

            chkStatut.setSelected(destination.getStatut());
        }
    }

    @FXML
    private void initialize() {
        btnSave.setOnAction(e -> save());
        btnCancel.setOnAction(e -> closeWindow());
        btnGenererDescription.setOnAction(e -> genererDescriptionIA());
    }

    // ==============================
    // 🤖 GÉNÉRATION IA
    // ==============================
    private void genererDescriptionIA() {
        String nom    = txtNom.getText().trim();
        String pays   = txtPays.getText().trim();
        String saison = txtSaison.getText().trim();

        if (nom.isEmpty() || pays.isEmpty()) {
            lblIaStatus.setText("⚠️ Remplis le nom et le pays d'abord");
            lblIaStatus.setStyle("-fx-text-fill: #e74c3c;");
            return;
        }

        btnGenererDescription.setDisable(true);
        lblIaStatus.setText("⏳ Génération en cours...");
        lblIaStatus.setStyle("-fx-text-fill: #f39c12;");

        Thread thread = new Thread(() -> {
            try {
                String description = callGroq(nom, pays, saison);
                Platform.runLater(() -> {
                    txtDescription.setText(description);
                    lblIaStatus.setText("✅ Description générée avec succès !");
                    lblIaStatus.setStyle("-fx-text-fill: #27ae60;");
                    btnGenererDescription.setDisable(false);
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    lblIaStatus.setText("❌ " + ex.getMessage());
                    lblIaStatus.setStyle("-fx-text-fill: #e74c3c;");
                    btnGenererDescription.setDisable(false);
                });
                System.err.println("Erreur Groq : " + ex.getMessage());
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    // ==============================
    // APPEL GROQ
    // ==============================
    private String callGroq(String nom, String pays, String saison) throws Exception {
        URL url = new URL("https://api.groq.com/openai/v1/chat/completions");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + GROQ_API_KEY);
        conn.setDoOutput(true);
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(15000);

        String prompt = "Tu es un assistant de voyage strict. "
                + "Si le nom '" + nom + "' ou le pays '" + pays + "' ne correspondent pas "
                + "à une vraie destination géographique connue (ville, région, site touristique réel), "
                + "réponds UNIQUEMENT avec le mot : DESTINATION_INVALIDE "
                + "Sans rien ajouter d'autre. "
                + "Sinon, rédige une description de voyage courte et attractive (3 à 4 phrases) "
                + "pour la destination : " + nom + ", située en " + pays + ". "
                + (saison != null && !saison.isBlank()
                ? "La meilleure saison pour visiter est : " + saison + ". " : "")
                + "Réponds uniquement avec la description, sans titre ni introduction.";

        String body = "{\"model\":\"llama-3.3-70b-versatile\","
                + "\"messages\":[{\"role\":\"user\",\"content\":\"" + escapeJson(prompt) + "\"}],"
                + "\"max_tokens\":300,\"temperature\":0.2}";

        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.getBytes(StandardCharsets.UTF_8));
        }

        int status = conn.getResponseCode();
        InputStream is = (status == 200) ? conn.getInputStream() : conn.getErrorStream();
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
        }

        if (status != 200) throw new Exception("Groq HTTP " + status);

        // Parser robuste caractère par caractère
        String json = sb.toString();
        int start = json.indexOf("\"content\":\"");
        if (start == -1) throw new Exception("Réponse Groq invalide");
        start += 11;

        StringBuilder result = new StringBuilder();
        boolean escape = false;
        for (int i = start; i < json.length(); i++) {
            char c = json.charAt(i);
            if (escape) {
                switch (c) {
                    case 'n':  result.append('\n'); break;
                    case 't':  result.append('\t'); break;
                    case '"':  result.append('"');  break;
                    case '\\': result.append('\\'); break;
                    case 'u':
                        if (i + 4 < json.length()) {
                            String hex = json.substring(i + 1, i + 5);
                            try { result.append((char) Integer.parseInt(hex, 16)); i += 4; }
                            catch (NumberFormatException ex) { result.append("\\u").append(hex); }
                        }
                        break;
                    default: result.append(c);
                }
                escape = false;
            } else if (c == '\\') {
                escape = true;
            } else if (c == '"') {
                break;
            } else {
                result.append(c);
            }
        }

        String description = result.toString()
                .replaceAll("\\*\\*(.*?)\\*\\*", "$1")
                .replaceAll("\\*(.*?)\\*",       "$1")
                .trim();

        if (description.toUpperCase().contains("DESTINATION_INVALIDE")) {
            throw new Exception("Destination invalide. Veuillez entrer un nom et un pays réels.");
        }

        return description;
    }

    // ==============================
    // ESCAPE JSON
    // ==============================
    private String escapeJson(String text) {
        return text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    // ==============================
    // SAUVEGARDE
    // ==============================
    private void closeWindow() {
        ((Stage) btnCancel.getScene().getWindow()).close();
    }

    private void save() {
        clearErrors();
        if (!validateFields()) return;

        try {
            if (destination == null) destination = new Destination();

            destination.setNom(txtNom.getText().trim());
            destination.setPays(txtPays.getText().trim());
            destination.setDescription(txtDescription.getText().trim());
            destination.setMeilleureSaison(capitalize(txtSaison.getText().trim()));
            destination.setStatut(chkStatut.isSelected());

            destination.setLatitude(Double.parseDouble(txtLatitude.getText().trim()));
            destination.setLongitude(Double.parseDouble(txtLongitude.getText().trim()));
            destination.setNbVisites(Integer.parseInt(txtNbVisites.getText().trim()));

            destination.setPrix(Double.parseDouble(txtPrix.getText().trim()));
            destination.setDateDepart(dateDepart.getValue());
            destination.setDateArrivee(dateArrivee.getValue());

            if (destination.getIdDestination() == 0) {
                service.addEntity2(destination);
            } else {
                service.update(destination.getIdDestination(), destination);
            }

            closeWindow();

        } catch (SQLException ex) {
            showAlert("Erreur base de données", ex.getMessage());
        } catch (Exception ex) {
            showAlert("Erreur", ex.getMessage());
        }
    }

    // ==============================
    // VALIDATION
    // ==============================
    private boolean validateFields() {
        boolean isValid = true;

        String nom = txtNom.getText().trim();
        if (nom.isEmpty()) {
            setError(txtNom, errNom, "Le nom est obligatoire");
            isValid = false;
        } else if (!nom.matches("^[A-Za-zÀ-ÿ\\s-]+$")) {
            setError(txtNom, errNom, "Lettres uniquement (pas de chiffres)");
            isValid = false;
        }

        String pays = txtPays.getText().trim();
        if (pays.isEmpty()) {
            setError(txtPays, errPays, "Le pays est obligatoire");
            isValid = false;
        } else if (!pays.matches("^[A-Za-zÀ-ÿ\\s-]+$")) {
            setError(txtPays, errPays, "Lettres uniquement (pas de chiffres)");
            isValid = false;
        }

        String desc = txtDescription.getText().trim();
        if (desc.isEmpty()) {
            setError(txtDescription, errDescription, "Description obligatoire");
            isValid = false;
        } else if (desc.length() < 10) {
            setError(txtDescription, errDescription, "Minimum 10 caractères");
            isValid = false;
        }

        String saison = txtSaison.getText().trim().toLowerCase();
        if (saison.isEmpty()) {
            setError(txtSaison, errSaison, "Champ obligatoire");
            isValid = false;
        } else if (!(saison.equals("printemps") || saison.equals("été") || saison.equals("ete")
                || saison.equals("automne") || saison.equals("hiver"))) {
            setError(txtSaison, errSaison, "Valeurs : Printemps, Été, Automne, Hiver");
            isValid = false;
        }

        isValid = validateDouble(txtLatitude,  errLatitude,  "Latitude entre -90 et 90",    -90,  90)  && isValid;
        isValid = validateDouble(txtLongitude, errLongitude, "Longitude entre -180 et 180", -180, 180) && isValid;

        try {
            int nb = Integer.parseInt(txtNbVisites.getText().trim());
            if (nb < 0) { setError(txtNbVisites, errNbVisites, "Doit être positif"); isValid = false; }
        } catch (NumberFormatException e) {
            setError(txtNbVisites, errNbVisites, "Nombre invalide"); isValid = false;
        }

        try {
            double prix = Double.parseDouble(txtPrix.getText().trim());
            if (prix < 0) { setError(txtPrix, errPrix, "Doit être positif"); isValid = false; }
        } catch (NumberFormatException e) {
            setError(txtPrix, errPrix, "Nombre invalide"); isValid = false;
        }

        if (dateDepart.getValue() == null) {
            errDateDepart.setText("La date de départ est obligatoire");
            dateDepart.getStyleClass().add("input-error");
            isValid = false;
        }

        if (dateArrivee.getValue() == null) {
            errDateArrivee.setText("La date d'arrivée est obligatoire");
            dateArrivee.getStyleClass().add("input-error");
            isValid = false;
        }

        if (dateDepart.getValue() != null && dateArrivee.getValue() != null) {
            if (dateArrivee.getValue().isBefore(dateDepart.getValue())) {
                errDateArrivee.setText("La date d'arrivée doit être après le départ");
                dateArrivee.getStyleClass().add("input-error");
                isValid = false;
            }
        }

        return isValid;
    }

    private boolean validateDouble(TextField field, Label errLabel, String msg, double min, double max) {
        try {
            double value = Double.parseDouble(field.getText().trim());
            if (value < min || value > max) { setError(field, errLabel, msg); return false; }
            return true;
        } catch (NumberFormatException e) {
            setError(field, errLabel, "Nombre invalide"); return false;
        }
    }

    private void setError(Control field, Label label, String message) {
        if (field != null && !field.getStyleClass().contains("input-error"))
            field.getStyleClass().add("input-error");
        if (label != null) label.setText(message);
    }

    private void clearErrors() {
        Control[] fields = { txtNom, txtPays, txtDescription, txtSaison,
                txtLatitude, txtLongitude, txtNbVisites, txtPrix, dateDepart, dateArrivee };
        Label[] labels   = { errNom, errPays, errDescription, errSaison,
                errLatitude, errLongitude, errNbVisites, errPrix, errDateDepart, errDateArrivee };

        for (Control c : fields) if (c != null) c.getStyleClass().remove("input-error");
        for (Label  l : labels)  if (l != null) l.setText("");
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private String capitalize(String text) {
        if (text == null || text.isEmpty()) return text;
        return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
    }
}