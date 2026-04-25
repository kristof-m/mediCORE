package sk.medicore.model;

public class Procedura {

    private int id;
    private String nazov;
    private int trvanieMin;
    private String popis;
    private String kategoria;

    public Procedura() {}

    public Procedura(int id, String nazov, int trvanieMin, String popis) {
        this.id = id;
        this.nazov = nazov;
        this.trvanieMin = trvanieMin;
        this.popis = popis;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNazov() { return nazov; }
    public void setNazov(String nazov) { this.nazov = nazov; }

    public int getTrvanieMin() { return trvanieMin; }
    public void setTrvanieMin(int trvanieMin) { this.trvanieMin = trvanieMin; }

    public String getPopis() { return popis; }
    public void setPopis(String popis) { this.popis = popis; }

    public String getKategoria() { return kategoria; }
    public void setKategoria(String kategoria) { this.kategoria = kategoria; }
}
