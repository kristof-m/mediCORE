package sk.medicore.model;

import java.time.LocalDateTime;

public class Rezervacia {

    public enum Stav {
        POTVRDENA, ZRUSENA, PRESUNUTA
    }

    private int id;
    private int pacientId;
    private int lekarId;
    private int terminId;
    private int proceduraId;
    private Stav stav;
    private LocalDateTime vytvorenaAt;

    public Rezervacia() {}

    public Rezervacia(int id, int pacientId, int lekarId, int terminId, int proceduraId,
                      Stav stav, LocalDateTime vytvorenaAt) {
        this.id = id;
        this.pacientId = pacientId;
        this.lekarId = lekarId;
        this.terminId = terminId;
        this.proceduraId = proceduraId;
        this.stav = stav;
        this.vytvorenaAt = vytvorenaAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getPacientId() { return pacientId; }
    public void setPacientId(int pacientId) { this.pacientId = pacientId; }

    public int getLekarId() { return lekarId; }
    public void setLekarId(int lekarId) { this.lekarId = lekarId; }

    public int getTerminId() { return terminId; }
    public void setTerminId(int terminId) { this.terminId = terminId; }

    public int getProceduraId() { return proceduraId; }
    public void setProceduraId(int proceduraId) { this.proceduraId = proceduraId; }

    public Stav getStav() { return stav; }
    public void setStav(Stav stav) { this.stav = stav; }

    public LocalDateTime getVytvorenaAt() { return vytvorenaAt; }
    public void setVytvorenaAt(LocalDateTime vytvorenaAt) { this.vytvorenaAt = vytvorenaAt; }
}
