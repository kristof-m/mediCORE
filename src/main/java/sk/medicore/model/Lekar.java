package sk.medicore.model;

public class Lekar extends Pouzivatel {

    private String specializacia;
    private int pracoviskoId;

    public Lekar() {
        setTyp("LEKAR");
    }

    public Lekar(int id, String meno, String priezvisko, String email, String hesloHash,
                 String specializacia, int pracoviskoId) {
        super(id, meno, priezvisko, email, hesloHash, "LEKAR");
        this.specializacia = specializacia;
        this.pracoviskoId = pracoviskoId;
    }

    public String getSpecializacia() { return specializacia; }
    public void setSpecializacia(String specializacia) { this.specializacia = specializacia; }

    public int getPracoviskoId() { return pracoviskoId; }
    public void setPracoviskoId(int pracoviskoId) { this.pracoviskoId = pracoviskoId; }
}
