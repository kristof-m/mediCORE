package sk.medicore.util;

import sk.medicore.model.Pouzivatel;

public class SessionManager {

    private static SessionManager instance;
    private Pouzivatel currentUser;

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

    public void logout() {
        this.currentUser = null;
    }
}
