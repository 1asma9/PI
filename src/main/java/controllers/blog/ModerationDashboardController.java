package controllers.blog;

import edu.connexion3a8.blogmoderation.domain.*;
import edu.connexion3a8.blogmoderation.dto.*;
import edu.connexion3a8.blogmoderation.repository.InMemoryModerationRepository;
import edu.connexion3a8.blogmoderation.repository.JdbcModerationRepository;
import edu.connexion3a8.blogmoderation.service.MockModerationDashboardService;
import edu.connexion3a8.blogmoderation.service.ModerationDashboardService;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class ModerationDashboardController {
    @FXML private TextField searchField;
    @FXML private ComboBox<ReportStatus> statusFilter;
    @FXML private ComboBox<PriorityLevel> priorityFilter;
    @FXML private ComboBox<ReportReason> reasonFilter;
    @FXML private DatePicker fromDate;
    @FXML private DatePicker toDate;
    @FXML private CheckBox onlyPendingCheck;
    @FXML private Label kpiAvgTime;
    @FXML private Label kpiApprovalRate;
    @FXML private Label kpiPending;
    @FXML private Label kpiAiPrecision;
    @FXML private Label detailHeader;
    @FXML private Label detailContent;
    @FXML private Label detailAnalysis;
    @FXML private Label detailHistory;
    @FXML private ComboBox<String> assignCombo;
    @FXML private TextArea noteArea;
    @FXML private TextArea reasonArea;
    @FXML private Pagination pagination;
    @FXML private TableView<ReportRowViewModel> reportsTable;
    @FXML private TableColumn<ReportRowViewModel, String> colReportId;
    @FXML private TableColumn<ReportRowViewModel, String> colCommentId;
    @FXML private TableColumn<ReportRowViewModel, String> colAuthor;
    @FXML private TableColumn<ReportRowViewModel, String> colPreview;
    @FXML private TableColumn<ReportRowViewModel, String> colReportCount;
    @FXML private TableColumn<ReportRowViewModel, String> colReason;
    @FXML private TableColumn<ReportRowViewModel, String> colToxicity;
    @FXML private TableColumn<ReportRowViewModel, String> colPriority;
    @FXML private TableColumn<ReportRowViewModel, String> colStatus;
    @FXML private TableColumn<ReportRowViewModel, String> colUpdated;
    @FXML private TableColumn<ReportRowViewModel, String> colModerator;

    private final ModerationDashboardService moderationService = buildService();
    private final ObservableList<ReportRowViewModel> rows = FXCollections.observableArrayList();
    private final FilteredList<ReportRowViewModel> filtered = new FilteredList<>(rows);
    private final SortedList<ReportRowViewModel> sorted = new SortedList<>(filtered);
    private static final int PAGE_SIZE = 20;
    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final String currentModerator = "moderator1";

    private ModerationDashboardService buildService() {
        try {
            return new MockModerationDashboardService(new JdbcModerationRepository());
        } catch (Exception ignored) {
            return new MockModerationDashboardService(new InMemoryModerationRepository());
        }
    }

    @FXML
    public void initialize() {
        setupFilters();
        setupTable();
        refresh();
        reportsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldV, selected) -> showDetail(selected));
    }

    private void setupFilters() {
        statusFilter.getItems().add(null);
        statusFilter.getItems().addAll(ReportStatus.values());
        priorityFilter.getItems().add(null);
        priorityFilter.getItems().addAll(PriorityLevel.values());
        reasonFilter.getItems().add(null);
        reasonFilter.getItems().addAll(ReportReason.values());
        assignCombo.setItems(FXCollections.observableArrayList(moderationService.listModerators()));

        searchField.textProperty().addListener((obs, ov, nv) -> refresh());
        statusFilter.valueProperty().addListener((obs, ov, nv) -> refresh());
        priorityFilter.valueProperty().addListener((obs, ov, nv) -> refresh());
        reasonFilter.valueProperty().addListener((obs, ov, nv) -> refresh());
        fromDate.valueProperty().addListener((obs, ov, nv) -> refresh());
        toDate.valueProperty().addListener((obs, ov, nv) -> refresh());
        onlyPendingCheck.selectedProperty().addListener((obs, ov, nv) -> refresh());
    }

    private void setupTable() {
        colReportId.setCellValueFactory(c -> new ReadOnlyStringWrapper(String.valueOf(c.getValue().getReportId())));
        colCommentId.setCellValueFactory(c -> new ReadOnlyStringWrapper(String.valueOf(c.getValue().getCommentId())));
        colAuthor.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getAuthor()));
        colPreview.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getPreview()));
        colReportCount.setCellValueFactory(c -> new ReadOnlyStringWrapper(String.valueOf(c.getValue().getReportCount())));
        colReason.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getReason().name()));
        colToxicity.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getToxicity() + "/100"));
        colPriority.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getPriority().name()));
        colStatus.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getStatus().name()));
        colUpdated.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getUpdatedAt().format(DT)));
        colModerator.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getAssignedModerator()));
        sorted.comparatorProperty().bind(reportsTable.comparatorProperty());
        reportsTable.setItems(sorted);
        reportsTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    @FXML
    private void refresh() {
        ModerationFilter filter = new ModerationFilter();
        filter.setQuery(searchField.getText());
        filter.setStatus(statusFilter.getValue());
        filter.setPriority(priorityFilter.getValue());
        filter.setReason(reasonFilter.getValue());
        filter.setFromDate(fromDate.getValue());
        filter.setToDate(toDate.getValue());
        filter.setOnlyPending(onlyPendingCheck.isSelected());

        rows.setAll(moderationService.search(filter, 0, PAGE_SIZE));
        pagination.setPageCount(Math.max(1, (int) Math.ceil(moderationService.count(filter) / (double) PAGE_SIZE)));
        pagination.setPageFactory(pageIndex -> {
            rows.setAll(moderationService.search(filter, pageIndex, PAGE_SIZE));
            return reportsTable;
        });
        updateKpi();
    }

    private void updateKpi() {
        ModerationKpi kpi = moderationService.computeKpi();
        kpiAvgTime.setText(String.format("%.1f h", kpi.getAvgResolutionHours()));
        kpiApprovalRate.setText(String.format("%.1f %%", kpi.getApprovalRate()));
        kpiPending.setText(String.valueOf(kpi.getPendingCount()));
        kpiAiPrecision.setText(String.format("%.1f %%", kpi.getAiPrecision()));
    }

    private void showDetail(ReportRowViewModel row) {
        if (row == null) {
            detailHeader.setText("Aucun signalement selectionne");
            detailContent.setText("-");
            detailAnalysis.setText("-");
            detailHistory.setText("-");
            return;
        }
        ModerationDetailDto detail = moderationService.getDetail(row.getReportId());
        detailHeader.setText("Signalement #" + row.getReportId() + " - " + row.getStatus());
        detailContent.setText("Auteur: " + detail.getReport().getCommentAuthor()
                + "\nContenu: " + detail.getReport().getContentSnapshot()
                + "\nMotif: " + detail.getReport().getMainReason()
                + "\nPriorite: " + detail.getReport().getPriority());

        ModerationAnalysis a = detail.getAnalysis();
        detailAnalysis.setText("Toxicite: " + a.getToxicityScore() + "/100"
                + "\nConfiance: " + String.format("%.2f", a.getConfidence())
                + "\nLangue: " + a.getLanguage()
                + "\nCategories: " + String.join(", ", a.getCategories())
                + "\nMots sensibles: " + String.join(", ", a.getSensitiveWords())
                + "\nReco IA: " + a.getRecommendation()
                + "\nJustification: " + a.getJustification());

        detailHistory.setText(detail.getHistory().stream()
                .map(h -> h.getCreatedAt().format(DT) + " - " + h.getActor() + " - " + h.getAction() + " - " + h.getDetails())
                .collect(Collectors.joining("\n")));

        if (a.getRecommendation() == ModerationAction.DELETE && a.getConfidence() > 0.90) {
            reasonArea.setPromptText("Reco IA forte: DELETE (confiance > 0.90)");
        } else {
            reasonArea.setPromptText("Saisir la raison si action sensible");
        }
    }

    @FXML
    private void approveSelected() { decideOnSelection(ModerationAction.APPROVE); }
    @FXML
    private void hideSelected() { decideOnSelection(ModerationAction.HIDE); }
    @FXML
    private void deleteSelected() { decideOnSelection(ModerationAction.DELETE); }
    @FXML
    private void escalateSelected() { decideOnSelection(ModerationAction.ESCALATE); }

    private void decideOnSelection(ModerationAction action) {
        List<ReportRowViewModel> selected = reportsTable.getSelectionModel().getSelectedItems();
        if (selected.isEmpty()) return;
        List<ModerationDecisionCommand> commands = selected.stream()
                .map(r -> new ModerationDecisionCommand(
                        r.getReportId(),
                        r.getVersion(),
                        action,
                        currentModerator,
                        reasonArea.getText(),
                        noteArea.getText()))
                .collect(Collectors.toList());
        moderationService.bulkDecide(commands, UserRole.SENIOR_MODERATOR);
        refresh();
    }

    @FXML
    private void reassignSelected() {
        ReportRowViewModel selected = reportsTable.getSelectionModel().getSelectedItem();
        if (selected == null || assignCombo.getValue() == null) return;
        moderationService.reassign(selected.getReportId(), selected.getVersion(), assignCombo.getValue(), currentModerator);
        refresh();
    }

    @FXML
    private void addInternalNote() {
        ReportRowViewModel selected = reportsTable.getSelectionModel().getSelectedItem();
        if (selected == null || noteArea.getText() == null || noteArea.getText().isBlank()) return;
        moderationService.addInternalNote(selected.getReportId(), currentModerator, noteArea.getText().trim());
        showDetail(selected);
        noteArea.clear();
    }
}
