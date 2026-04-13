package sk.medicore.model;

public class Pacient extends Pouzivatel {

    public Pacient() {
        setTyp("PACIENT");
    }

    public Pacient(int id, String meno, String priezvisko, String email, String hesloHash) {
        super(id, meno, priezvisko, email, hesloHash, "PACIENT");
    }
}
