package sk.medicore.model;

public abstract class Pouzivatel {

    private int id;
    private String meno;
    private String priezvisko;
    private String email;
    private String hesloHash;
    private String typ;

    public Pouzivatel() {}

    public Pouzivatel(int id, String meno, String priezvisko, String email, String hesloHash, String typ) {
        this.id = id;
        this.meno = meno;
        this.priezvisko = priezvisko;
        this.email = email;
        this.hesloHash = hesloHash;
        this.typ = typ;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getMeno() { return meno; }
    public void setMeno(String meno) { this.meno = meno; }

    public String getPriezvisko() { return priezvisko; }
    public void setPriezvisko(String priezvisko) { this.priezvisko = priezvisko; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getHesloHash() { return hesloHash; }
    public void setHesloHash(String hesloHash) { this.hesloHash = hesloHash; }

    public String getTyp() { return typ; }
    public void setTyp(String typ) { this.typ = typ; }

    public String getCeleMeno() {
        return meno + " " + priezvisko;
    }
}
