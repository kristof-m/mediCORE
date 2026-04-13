package sk.medicore.util;

import sk.medicore.model.Pouzivatel;
import sk.medicore.model.Rezervacia;

public class SessionManager {

    private static SessionManager instance;
    private Pouzivatel currentUser;
    private Rezervacia rezervaciaToReschedule;

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

    public void logout() {
        this.currentUser = null;
        this.rezervaciaToReschedule = null;
    }
}
