package sk.medicore.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import sk.medicore.db.dao.PouzivatelDAO;
import sk.medicore.db.dao.RezervaciaDAO;
import sk.medicore.db.dao.TerminDAO;
import sk.medicore.model.Pouzivatel;
import sk.medicore.model.Rezervacia;
import sk.medicore.model.Termin;
import sk.medicore.util.PasswordUtil;
import sk.medicore.util.SceneManager;
import sk.medicore.util.SessionManager;

import java.time.LocalDateTime;
import java.util.List;

public class ProfilController {

    @FXML private Label sidebarMenoLabel;
    @FXML private Label sidebarTypLabel;
    @FXML private Label avatarLabel;
    @FXML private Label fullNameLabel;
    @FXML private Label typBadge;
    @FXML private Label menoLabel;
    @FXML private Label emailLabel;
    @FXML private Label typLabel;
    @FXML private Label statCelkovo;
    @FXML private Label statAktivne;
    @FXML private Label statZrusene;

    private final RezervaciaDAO rezervaciaDAO = new RezervaciaDAO();
    private final TerminDAO terminDAO = new TerminDAO();
    private final PouzivatelDAO pouzivatelDAO = new PouzivatelDAO();

    @FXML
    public void initialize() {
        Pouzivatel user = SessionManager.getInstance().getCurrentUser();
        if (user == null) return;

        String initials = user.getMeno().substring(0, 1).toUpperCase()
            + user.getPriezvisko().substring(0, 1).toUpperCase();
        avatarLabel.setText(initials);
        fullNameLabel.setText(user.getCeleMeno());
        menoLabel.setText(user.getCeleMeno());
        emailLabel.setText(user.getEmail());

        String typStr = formatTyp(user.getTyp());
        typLabel.setText(typStr);
        typBadge.setText(typStr);
        sidebarMenoLabel.setText(user.getCeleMeno());
        sidebarTypLabel.setText(typStr);

        loadStats(user.getId());
    }

    private void loadStats(int pacientId) {
        List<Rezervacia> all = rezervaciaDAO.findByPacientId(pacientId);
        LocalDateTime now = LocalDateTime.now();

        long aktivne = all.stream()
            .filter(r -> r.getStav() == Rezervacia.Stav.POTVRDENA)
            .filter(r -> getTerminTime(r.getTerminId()).isAfter(now))
            .count();
        long zrusene = all.stream()
            .filter(r -> r.getStav() == Rezervacia.Stav.ZRUSENA)
            .count();

        statCelkovo.setText(String.valueOf(all.size()));
        statAktivne.setText(String.valueOf(aktivne));
        statZrusene.setText(String.valueOf(zrusene));
    }

    private LocalDateTime getTerminTime(int terminId) {
        Termin t = terminDAO.findById(terminId);
        return t != null ? t.getDatumCas() : LocalDateTime.MIN;
    }

    @FXML
    private void handleChangePassword() {
        Pouzivatel user = SessionManager.getInstance().getCurrentUser();
        if (user == null) return;

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Zmena hesla");
        dialog.setHeaderText("Zadajte nové heslo");

        ButtonType confirmType = new ButtonType("Zmeniť", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmType, ButtonType.CANCEL);

        PasswordField oldPw = new PasswordField();
        oldPw.setPromptText("Aktuálne heslo");

        PasswordField newPw = new PasswordField();
        newPw.setPromptText("Nové heslo (min. 6 znakov)");

        PasswordField confirmPw = new PasswordField();
        confirmPw.setPromptText("Potvrdenie nového hesla");

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #d32f2f; -fx-font-size: 12px;");

        javafx.scene.layout.VBox content = new javafx.scene.layout.VBox(10,
            new Label("Aktuálne heslo:"), oldPw,
            new Label("Nové heslo:"), newPw,
            new Label("Potvrdiť heslo:"), confirmPw,
            errorLabel
        );
        content.setStyle("-fx-padding: 10 0 0 0;");
        dialog.getDialogPane().setContent(content);

        // Disable OK until fields are non-empty
        javafx.scene.Node okButton = dialog.getDialogPane().lookupButton(confirmType);
        okButton.setDisable(true);
        Runnable checkFields = () -> okButton.setDisable(
            oldPw.getText().isBlank() || newPw.getText().isBlank() || confirmPw.getText().isBlank()
        );
        oldPw.textProperty().addListener((o, a, b) -> checkFields.run());
        newPw.textProperty().addListener((o, a, b) -> checkFields.run());
        confirmPw.textProperty().addListener((o, a, b) -> checkFields.run());

        // Validate on OK click
        okButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            if (!PasswordUtil.verify(oldPw.getText(), user.getHesloHash())) {
                errorLabel.setText("Aktuálne heslo je nesprávne.");
                event.consume();
                return;
            }
            if (newPw.getText().length() < 6) {
                errorLabel.setText("Nové heslo musí mať aspoň 6 znakov.");
                event.consume();
                return;
            }
            if (!newPw.getText().equals(confirmPw.getText())) {
                errorLabel.setText("Heslá sa nezhodujú.");
                event.consume();
            }
        });

        dialog.showAndWait().ifPresent(result -> {
            if (result == confirmType) {
                String newHash = PasswordUtil.hash(newPw.getText());
                pouzivatelDAO.updateHeslo(user.getId(), newHash);
                user.setHesloHash(newHash);

                Alert ok = new Alert(Alert.AlertType.INFORMATION);
                ok.setTitle("Heslo zmenené");
                ok.setHeaderText(null);
                ok.setContentText("Heslo bolo úspešne zmenené.");
                ok.showAndWait();
            }
        });
    }

    private String formatTyp(String typ) {
        return switch (typ) {
            case "LEKAR" -> "Lekár";
            case "ADMIN" -> "Administrátor";
            default -> "Pacient";
        };
    }

    @FXML private void handleNavDashboard() {
        Stage stage = (Stage) avatarLabel.getScene().getWindow();
        SceneManager.switchTo(stage, "/view/dashboard.fxml");
    }

    @FXML private void handleNavRezervacje() {
        Stage stage = (Stage) avatarLabel.getScene().getWindow();
        SceneManager.switchTo(stage, "/view/moje-rezervacie.fxml");
    }

    @FXML private void handleNavRezervovat() {
        Stage stage = (Stage) avatarLabel.getScene().getWindow();
        SceneManager.switchTo(stage, "/view/rezervacia-wizard.fxml");
    }

    @FXML private void handleNavKalendar() {
        Stage stage = (Stage) avatarLabel.getScene().getWindow();
        SceneManager.switchTo(stage, "/view/patient-kalendar.fxml");
    }

    @FXML private void handleLogout() {
        SessionManager.getInstance().logout();
        Stage stage = (Stage) avatarLabel.getScene().getWindow();
        SceneManager.switchTo(stage, "/view/prihlasenie.fxml");
    }
}
