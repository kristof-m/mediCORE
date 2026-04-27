package sk.medicore.controller;

import java.time.LocalDateTime;
import java.util.List;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
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

public class ProfilController {

    @FXML private SidebarPacientController sidebarController;

    // Banner
    @FXML private Label avatarLabel;
    @FXML private Label fullNameLabel;
    @FXML private Label emailLabel;
    @FXML private Label typBadge;

    // Osobné údaje form
    @FXML private TextField menoField;
    @FXML private TextField priezviskoField;
    @FXML private TextField emailProfilField;
    @FXML private Label profilFeedbackLabel;

    // Zmena hesla form
    @FXML private PasswordField currentPwField;
    @FXML private PasswordField newPwField;
    @FXML private PasswordField confirmPwField;
    @FXML private Label pwFeedbackLabel;

    // Stats
    @FXML private Label statCelkovo;
    @FXML private Label statAktivne;
    @FXML private Label statZrusene;

    private final RezervaciaDAO rezervaciaDAO = new RezervaciaDAO();
    private final TerminDAO terminDAO = new TerminDAO();
    private final PouzivatelDAO pouzivatelDAO = new PouzivatelDAO();

    private String originalMeno;
    private String originalPriezvisko;
    private String originalEmail;

    @FXML
    public void initialize() {
        Pouzivatel user = SessionManager.getInstance().getCurrentUser();
        if (user == null) return;

        String m = user.getMeno(), p = user.getPriezvisko();
        String initials = (m.isEmpty() ? "?" : m.substring(0, 1).toUpperCase())
            + (p.isEmpty() ? "?" : p.substring(0, 1).toUpperCase());
        avatarLabel.setText(initials);
        fullNameLabel.setText(user.getCeleMeno());
        emailLabel.setText(user.getEmail());

        String typStr = formatTyp(user.getTyp());
        typBadge.setText(typStr);
        sidebarController.setActivePage("profil");

        originalMeno = user.getMeno();
        originalPriezvisko = user.getPriezvisko();
        originalEmail = user.getEmail();

        menoField.setText(originalMeno);
        priezviskoField.setText(originalPriezvisko);
        emailProfilField.setText(originalEmail);

        loadStats(user.getId());
    }

    @FXML
    private void handleSaveProfile() {
        Pouzivatel user = SessionManager.getInstance().getCurrentUser();
        if (user == null) return;

        String meno = menoField.getText().trim();
        String priezvisko = priezviskoField.getText().trim();
        String email = emailProfilField.getText().trim();

        if (meno.isBlank() || priezvisko.isBlank() || email.isBlank()) {
            showProfilFeedback("Vyplňte všetky polia.", false);
            return;
        }
        if (!email.contains("@")) {
            showProfilFeedback("Zadajte platný e-mail.", false);
            return;
        }
        if (!email.equals(user.getEmail()) && pouzivatelDAO.emailExists(email)) {
            showProfilFeedback("Tento e-mail je už zaregistrovaný.", false);
            return;
        }

        pouzivatelDAO.updateProfil(user.getId(), meno, priezvisko, email);
        user.setMeno(meno);
        user.setPriezvisko(priezvisko);
        user.setEmail(email);
        originalMeno = meno;
        originalPriezvisko = priezvisko;
        originalEmail = email;

        // Refresh banner
        String initials = (meno.isEmpty() ? "?" : meno.substring(0, 1).toUpperCase())
            + (priezvisko.isEmpty() ? "?" : priezvisko.substring(0, 1).toUpperCase());
        avatarLabel.setText(initials);
        fullNameLabel.setText(user.getCeleMeno());
        emailLabel.setText(email);

        showProfilFeedback("Zmeny boli uložené.", true);
    }

    @FXML
    private void handleCancelProfile() {
        menoField.setText(originalMeno);
        priezviskoField.setText(originalPriezvisko);
        emailProfilField.setText(originalEmail);
        profilFeedbackLabel.setVisible(false);
        profilFeedbackLabel.setManaged(false);
    }

    @FXML
    private void handleChangePassword() {
        Pouzivatel user = SessionManager.getInstance().getCurrentUser();
        if (user == null) return;

        String current = currentPwField.getText();
        String newPw = newPwField.getText();
        String confirm = confirmPwField.getText();

        if (current.isBlank() || newPw.isBlank() || confirm.isBlank()) {
            showPwFeedback("Vyplňte všetky polia.", false);
            return;
        }
        if (!PasswordUtil.verify(current, user.getHesloHash())) {
            showPwFeedback("Aktuálne heslo je nesprávne.", false);
            return;
        }
        String passwordError = PasswordUtil.validate(newPw);
        if (passwordError != null) {
            showPwFeedback(passwordError, false);
            return;
        }
        if (!newPw.equals(confirm)) {
            showPwFeedback("Heslá sa nezhodujú.", false);
            return;
        }

        String newHash = PasswordUtil.hash(newPw);
        pouzivatelDAO.updateHeslo(user.getId(), newHash);
        user.setHesloHash(newHash);

        currentPwField.clear();
        newPwField.clear();
        confirmPwField.clear();
        showPwFeedback("Heslo bolo úspešne zmenené.", true);
    }

    @FXML
    private void handleDeleteAccount() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Zmazať účet");
        confirm.setHeaderText("Naozaj chcete zmazať svoj účet?");
        confirm.setContentText("Táto akcia je nevratná. Všetky budúce rezervácie sa zrušia.");
        confirm.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                int userId = SessionManager.getInstance().getCurrentUser().getId();
                pouzivatelDAO.delete(userId);
                SessionManager.getInstance().logout();
                Stage stage = (Stage) avatarLabel.getScene().getWindow();
                SceneManager.switchTo(stage, "/view/prihlasenie.fxml");
            }
        });
    }

    @FXML
    private void handleLogout() {
        SessionManager.getInstance().logout();
        Stage stage = (Stage) avatarLabel.getScene().getWindow();
        SceneManager.switchTo(stage, "/view/prihlasenie.fxml");
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

    private void showProfilFeedback(String msg, boolean success) {
        profilFeedbackLabel.setText(msg);
        profilFeedbackLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: " + (success ? "#388e3c" : "#d32f2f") + ";");
        profilFeedbackLabel.setVisible(true);
        profilFeedbackLabel.setManaged(true);
    }

    private void showPwFeedback(String msg, boolean success) {
        pwFeedbackLabel.setText(msg);
        pwFeedbackLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: " + (success ? "#388e3c" : "#d32f2f") + ";");
        pwFeedbackLabel.setVisible(true);
        pwFeedbackLabel.setManaged(true);
    }

    private String formatTyp(String typ) {
        return switch (typ) {
            case "LEKAR" -> "Lekár";
            case "ADMIN" -> "Administrátor";
            default -> "Pacient";
        };
    }
}
