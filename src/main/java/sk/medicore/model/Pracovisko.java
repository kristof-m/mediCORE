package sk.medicore.model;

public class Pracovisko {

    private int id;
    private String nazov;
    private String budova;
    private String poschodie;
    private String miestnost;

    public Pracovisko() {}

    public Pracovisko(int id, String nazov, String budova, String poschodie, String miestnost) {
        this.id = id;
        this.nazov = nazov;
        this.budova = budova;
        this.poschodie = poschodie;
        this.miestnost = miestnost;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNazov() { return nazov; }
    public void setNazov(String nazov) { this.nazov = nazov; }

    public String getBudova() { return budova; }
    public void setBudova(String budova) { this.budova = budova; }

    public String getPoschodie() { return poschodie; }
    public void setPoschodie(String poschodie) { this.poschodie = poschodie; }

    public String getMiestnost() { return miestnost; }
    public void setMiestnost(String miestnost) { this.miestnost = miestnost; }

    public String getAdresa() {
        return budova + ", poschodie " + poschodie + ", miestnosť " + miestnost;
    }
}
