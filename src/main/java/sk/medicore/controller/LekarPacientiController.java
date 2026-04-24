package sk.medicore.controller;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.transform.Scale;
import sk.medicore.db.dao.RezervaciaDAO;
import sk.medicore.model.Lekar;
import sk.medicore.util.SessionManager;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class LekarPacientiController {

    @FXML private SidebarLekarController sidebarController;

    @FXML private Label statTotalLabel;
    @FXML private Label statNewLabel;
    @FXML private Label statVisitsLabel;
    @FXML private Label statAvgLabel;
    @FXML private Pane iconStatTotal;
    @FXML private Pane iconStatNew;
    @FXML private Pane iconStatVisits;
    @FXML private Pane iconStatAvg;
    @FXML private Pane searchIconPane;
    @FXML private TextField searchField;
    @FXML private VBox patientListBox;
    @FXML private Label emptyLabel;

    private final RezervaciaDAO rezervaciaDAO = new RezervaciaDAO();
    private List<RezervaciaDAO.PacientInfo> allPacienti;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("d. MMM yyyy", new java.util.Locale("sk"));
    private static final DateTimeFormatter DT_FMT   = DateTimeFormatter.ofPattern("d. MMM · HH:mm", new java.util.Locale("sk"));

    private static final String[] AVATAR_COLORS = {
        "#1a9e8f", "#3b82f6", "#8b5cf6", "#d9904f", "#e05780", "#64748b", "#0d9488"
    };

    @FXML
    public void initialize() {
        sidebarController.setActivePage("pacienti");

        fillIcon(iconStatTotal,  "M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2 M9 3a4 4 0 1 1 0 8 4 4 0 0 1 0-8Z M23 21v-2a4 4 0 0 0-3-3.87 M16 3.13a4 4 0 0 1 0 7.75", "#1a9e8f", 14.0 / 24);
        fillIcon(iconStatNew,    "M12 5v14 M5 12h14", "#059669", 14.0 / 24);
        fillIcon(iconStatVisits, "M5 4h14a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2Z M16 2v4 M8 2v4 M3 10h18", "#f57c00", 14.0 / 24);
        fillIcon(iconStatAvg,    "M18 20V10 M12 20V4 M6 20v-6", "#3b82f6", 14.0 / 24);
        fillSmallIcon(searchIconPane, "M21 21l-4.35-4.35 M17 11A6 6 0 1 1 5 11a6 6 0 0 1 12 0z", "#9aa0a8");

        Lekar lekar = (Lekar) SessionManager.getInstance().getCurrentUser();
        allPacienti = rezervaciaDAO.findPacientiByLekarId(lekar.getId());

        updateStats();
        renderList(allPacienti);

        searchField.textProperty().addListener((obs, old, val) -> filterList(val));
    }

    private void updateStats() {
        int total = allPacienti.size();
        statTotalLabel.setText(String.valueOf(total));

        LocalDateTime now = LocalDateTime.now();
        long newThisMonth = allPacienti.stream()
            .filter(p -> p.lastVisit() != null
                && p.lastVisit().getYear() == now.getYear()
                && p.lastVisit().getMonthValue() == now.getMonthValue())
            .count();
        statNewLabel.setText(String.valueOf(newThisMonth));

        int totalVisits = allPacienti.stream().mapToInt(RezervaciaDAO.PacientInfo::visitCount).sum();
        statVisitsLabel.setText(String.valueOf(totalVisits));

        double avg = total > 0 ? (double) totalVisits / total : 0;
        statAvgLabel.setText(String.format("%.1f", avg));
    }

    private void filterList(String query) {
        if (query == null || query.isBlank()) {
            renderList(allPacienti);
            return;
        }
        String q = query.toLowerCase();
        List<RezervaciaDAO.PacientInfo> filtered = allPacienti.stream()
            .filter(p -> (p.meno() + " " + p.priezvisko()).toLowerCase().contains(q)
                || (p.email() != null && p.email().toLowerCase().contains(q)))
            .collect(Collectors.toList());
        renderList(filtered);
    }

    private void renderList(List<RezervaciaDAO.PacientInfo> list) {
        patientListBox.getChildren().clear();

        if (list.isEmpty()) {
            emptyLabel.setVisible(true);
            emptyLabel.setManaged(true);
            return;
        }
        emptyLabel.setVisible(false);
        emptyLabel.setManaged(false);

        for (int i = 0; i < list.size(); i++) {
            patientListBox.getChildren().add(buildRow(list.get(i), i));
        }
    }

    private HBox buildRow(RezervaciaDAO.PacientInfo p, int idx) {
        HBox row = new HBox(14);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-padding: 12 14;" +
                     "-fx-border-color: #eef0f3; -fx-border-radius: 8; -fx-border-width: 1;");

        // Avatar
        String initials = initial(p.meno()) + initial(p.priezvisko());
        String color = AVATAR_COLORS[idx % AVATAR_COLORS.length];
        Label avatar = new Label(initials);
        avatar.setStyle("-fx-min-width: 38; -fx-min-height: 38; -fx-max-width: 38; -fx-max-height: 38;" +
                        "-fx-background-radius: 19; -fx-background-color: " + color + ";" +
                        "-fx-text-fill: white; -fx-font-size: 12.5px; -fx-font-weight: bold; -fx-alignment: CENTER;");

        // Name + email
        VBox who = new VBox(2);
        HBox.setHgrow(who, Priority.ALWAYS);
        Label name = new Label(p.meno() + " " + p.priezvisko());
        name.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #1a1a2e;");
        Label email = new Label(p.email() != null ? p.email() : "");
        email.setStyle("-fx-font-size: 11px; -fx-text-fill: #9aa0a8;");
        who.getChildren().addAll(name, email);

        // Last visit
        VBox lastCol = new VBox(2);
        lastCol.setMinWidth(120);
        lastCol.setMaxWidth(120);
        Label lastKey = new Label("Posl. návšteva");
        lastKey.setStyle("-fx-font-size: 9.5px; -fx-text-fill: #9aa0a8; -fx-font-weight: 600;");
        Label lastVal = new Label(p.lastVisit() != null ? p.lastVisit().format(DATE_FMT) : "—");
        lastVal.setStyle("-fx-font-size: 11.5px; -fx-font-weight: 600; -fx-text-fill: #1a1a2e;");
        lastCol.getChildren().addAll(lastKey, lastVal);

        // Next visit
        VBox nextCol = new VBox(2);
        nextCol.setMinWidth(130);
        nextCol.setMaxWidth(130);
        Label nextKey = new Label("Ďalší termín");
        nextKey.setStyle("-fx-font-size: 9.5px; -fx-text-fill: #9aa0a8; -fx-font-weight: 600;");
        String nextText = p.nextVisit() != null ? p.nextVisit().format(DT_FMT) : "—";
        String nextColor = p.nextVisit() != null ? "#1a9e8f" : "#9aa0a8";
        Label nextVal = new Label(nextText);
        nextVal.setStyle("-fx-font-size: 11.5px; -fx-font-weight: 600; -fx-text-fill: " + nextColor + ";");
        nextCol.getChildren().addAll(nextKey, nextVal);

        // Visit count badge
        Label badge = new Label(p.visitCount() + " návštev");
        badge.setStyle("-fx-background-color: #e8f5f3; -fx-text-fill: #1a9e8f;" +
                       "-fx-font-size: 11.5px; -fx-font-weight: bold;" +
                       "-fx-padding: 3 8; -fx-background-radius: 12;");

        row.getChildren().addAll(avatar, who, lastCol, nextCol, badge);
        return row;
    }

    private static String initial(String s) {
        return (s != null && !s.isEmpty()) ? s.substring(0, 1).toUpperCase() : "?";
    }

    private static void fillIcon(Pane tile, String pathData, String strokeColor, double scale) {
        tile.getChildren().clear();
        SVGPath svg = new SVGPath();
        svg.setContent(pathData);
        svg.setFill(Color.TRANSPARENT);
        svg.setStroke(Color.web(strokeColor));
        svg.setStrokeWidth(2.0);
        svg.setStrokeLineCap(javafx.scene.shape.StrokeLineCap.ROUND);
        svg.setStrokeLineJoin(javafx.scene.shape.StrokeLineJoin.ROUND);
        svg.getTransforms().add(new Scale(scale, scale, 0, 0));
        StackPane center = new StackPane(new Group(svg));
        center.setMinSize(30, 30);
        center.setMaxSize(30, 30);
        tile.getChildren().add(center);
    }

    private static void fillSmallIcon(Pane tile, String pathData, String strokeColor) {
        tile.getChildren().clear();
        SVGPath svg = new SVGPath();
        svg.setContent(pathData);
        svg.setFill(Color.TRANSPARENT);
        svg.setStroke(Color.web(strokeColor));
        svg.setStrokeWidth(2.0);
        svg.setStrokeLineCap(javafx.scene.shape.StrokeLineCap.ROUND);
        svg.setStrokeLineJoin(javafx.scene.shape.StrokeLineJoin.ROUND);
        svg.getTransforms().add(new Scale(12.0 / 24, 12.0 / 24, 0, 0));
        StackPane center = new StackPane(new Group(svg));
        center.setMinSize(16, 16);
        center.setMaxSize(16, 16);
        tile.getChildren().add(center);
    }
}
