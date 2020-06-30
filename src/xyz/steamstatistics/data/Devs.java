package xyz.steamstatistics.data;

public enum Devs {

    A(205011984675110922L),
    B(205939483009482752L),
    V(188391785104539649L);

    private long ID;

    Devs(long id) {
        this.ID = id;
    }

    public static boolean isDev(long id) {
        for (Devs d : values()) {
            if (d.ID == id) {
                return true;
            }
        }

        return false;
    }
}
