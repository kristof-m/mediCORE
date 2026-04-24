package sk.medicore.notifikator;

import java.time.LocalDateTime;

public class Notifikacia {

    public enum Typ {
        POTVRDENA("Rezervácia potvrdená"),
        ZRUSENA("Rezervácia zrušená"),
        PRESUNUTA("Rezervácia presunutá"),
        ZMENENY("Termín bol zmenený lekárom");

        private final String popis;
        Typ(String popis) { this.popis = popis; }
        public String getPopis() { return popis; }
    }

    private int id;
    private int pacientId;
    private Typ typ;
    private String sprava;
    private boolean precitana;
    private LocalDateTime vytvorenaAt;

    public Notifikacia() {}

    public Notifikacia(int pacientId, Typ typ, String sprava) {
        this.pacientId = pacientId;
        this.typ = typ;
        this.sprava = sprava;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getPacientId() { return pacientId; }
    public void setPacientId(int pacientId) { this.pacientId = pacientId; }

    public Typ getTyp() { return typ; }
    public void setTyp(Typ typ) { this.typ = typ; }

    public String getSprava() { return sprava; }
    public void setSprava(String sprava) { this.sprava = sprava; }

    public boolean isPrecitana() { return precitana; }
    public void setPrecitana(boolean precitana) { this.precitana = precitana; }

    public LocalDateTime getVytvorenaAt() { return vytvorenaAt; }
    public void setVytvorenaAt(LocalDateTime vytvorenaAt) { this.vytvorenaAt = vytvorenaAt; }
}
