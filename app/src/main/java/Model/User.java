package Model;

public class User {
    private String Name;
    private String Password;
    private String Uid;
    public User() {

    }

    public User(String name, String password) {
        Name = name;
        Password = password;
    }

    public void setUid(String uid) {
        Uid = uid;
    }

    public String getUid() {
        return Uid;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getPassword() {
        return Password;
    }

    public void setPassword(String password) {
        Password = password;
    }
}
