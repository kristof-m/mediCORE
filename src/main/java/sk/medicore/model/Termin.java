package sk.medicore.model;

import java.time.LocalDateTime;

public class Termin {

    public enum Stav {
        PUBLIKOVANY, REZERVOVANY, UKONCENY, ZRUSENY
    }

    private int id;
    private int lekarId;
    private LocalDateTime datumCas;
    private int trvanieMin;
    private Stav stav;

    public Termin() {}

    public Termin(int id, int lekarId, LocalDateTime datumCas, int trvanieMin, Stav stav) {
        this.id = id;
        this.lekarId = lekarId;
        this.datumCas = datumCas;
        this.trvanieMin = trvanieMin;
        this.stav = stav;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getLekarId() { return lekarId; }
    public void setLekarId(int lekarId) { this.lekarId = lekarId; }

    public LocalDateTime getDatumCas() { return datumCas; }
    public void setDatumCas(LocalDateTime datumCas) { this.datumCas = datumCas; }

    public int getTrvanieMin() { return trvanieMin; }
    public void setTrvanieMin(int trvanieMin) { this.trvanieMin = trvanieMin; }

    public Stav getStav() { return stav; }
    public void setStav(Stav stav) { this.stav = stav; }
}
