package sk.medicore.notifikator;

import sk.medicore.db.dao.NotifikaciaDAO;

import java.util.List;

public class Notifikator {

    private static final NotifikaciaDAO dao = new NotifikaciaDAO();

    public static void odosliNotifikaciu(int pacientId, Notifikacia.Typ typ) {
        Notifikacia n = new Notifikacia(pacientId, typ, typ.getPopis());
        dao.insert(n);
    }

    public static List<Notifikacia> getNeprecitane(int pacientId) {
        return dao.findUnread(pacientId);
    }

    public static void oznacVsetkyPrecitane(int pacientId) {
        dao.markAllRead(pacientId);
    }
}
