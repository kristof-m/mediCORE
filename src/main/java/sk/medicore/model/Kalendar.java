package sk.medicore.model;

import sk.medicore.db.dao.TerminDAO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class Kalendar {

    private final int lekarId;
    private final TerminDAO terminDAO;

    public Kalendar(int lekarId) {
        this.lekarId = lekarId;
        this.terminDAO = new TerminDAO();
    }

    public List<Termin> getTerminyPreObdobie(LocalDate od, LocalDate do_) {
        return terminDAO.findByLekarId(lekarId).stream()
            .filter(t -> !t.getDatumCas().toLocalDate().isBefore(od)
                      && !t.getDatumCas().toLocalDate().isAfter(do_))
            .collect(Collectors.toList());
    }

    public boolean skontrolujKonflikt(LocalDateTime datumCas) {
        return terminDAO.hasConflict(lekarId, datumCas);
    }

    public boolean skontrolujKonflikt(LocalDateTime datumCas, int excludeTerminId) {
        return terminDAO.hasConflictExcluding(lekarId, datumCas, excludeTerminId);
    }

    public void pridajTermin(LocalDateTime datumCas, int trvanieMin) {
        terminDAO.insert(lekarId, datumCas, trvanieMin);
    }

    public void aktualizujTermin(int terminId, LocalDateTime novyDatumCas) {
        terminDAO.updateDatumCas(terminId, novyDatumCas);
    }

    public int getLekarId() { return lekarId; }
}
