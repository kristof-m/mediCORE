package sk.medicore.model;

public class AdministrativnyPracovnik extends Pouzivatel {

    public AdministrativnyPracovnik() {
        setTyp("ADMIN");
    }

    public AdministrativnyPracovnik(int id, String meno, String priezvisko, String email, String hesloHash) {
        super(id, meno, priezvisko, email, hesloHash, "ADMIN");
    }
}
