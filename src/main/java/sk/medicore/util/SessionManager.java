package sk.medicore.util;

import sk.medicore.model.Lekar;
import sk.medicore.model.Procedura;
import sk.medicore.model.Pouzivatel;
import sk.medicore.model.Rezervacia;
import sk.medicore.model.Termin;

public class SessionManager {

    private static SessionManager instance;
    private Pouzivatel currentUser;
    private Rezervacia rezervaciaToReschedule;
    private Lekar preselectedLekar;
    private Termin preselectedTermin;
    private Procedura preselectedProcedura;
    private Integer editTerminId;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public void setCurrentUser(Pouzivatel user) {
        this.currentUser = user;
    }

    public Pouzivatel getCurrentUser() {
        return currentUser;
    }

    public void setRezervaciaToReschedule(Rezervacia r) {
        this.rezervaciaToReschedule = r;
    }

    public Rezervacia getRezervaciaToReschedule() {
        return rezervaciaToReschedule;
    }

    public void setPreselectedBooking(Lekar lekar, Termin termin, Procedura procedura) {
        this.preselectedLekar = lekar;
        this.preselectedTermin = termin;
        this.preselectedProcedura = procedura;
    }

    public Lekar getPreselectedLekar() { return preselectedLekar; }
    public Termin getPreselectedTermin() { return preselectedTermin; }
    public Procedura getPreselectedProcedura() { return preselectedProcedura; }

    public void setEditTerminId(Integer id) { this.editTerminId = id; }
    public Integer getEditTerminId() { return editTerminId; }
    public Integer consumeEditTerminId() {
        Integer id = editTerminId;
        editTerminId = null;
        return id;
    }

    public void clearPreselectedBooking() {
        this.preselectedLekar = null;
        this.preselectedTermin = null;
        this.preselectedProcedura = null;
    }

    public void logout() {
        this.currentUser = null;
        this.rezervaciaToReschedule = null;
        this.editTerminId = null;
        clearPreselectedBooking();
    }
}
