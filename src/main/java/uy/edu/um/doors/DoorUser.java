package uy.edu.um.doors;

public class DoorUser {
    private int uid;
    private String alias;
    private String type;

    public DoorUser(int uid, String alias, String type) {
        this.uid = uid;
        this.alias = alias;
        this.type = type;
    }

    public int getUid() {
        return uid;
    }

    public String getAlias() {
        return alias;
    }

    public String getType() {
        return type;
    }

    public int getWeight() {
        if (type.equals("ADMIN")) {
            return 32;
        } else {
            return 16;
        }
    }

    @Override
    public String toString() {
        return "USER:" + alias + " UID:" + uid;
    }
}
