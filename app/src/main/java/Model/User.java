package Model;

public class User {
    private String Name;
    private String Password;
    private String Address;
    private String Email;
    private String Count;
    private String Phone;
    private String Uid;

    public User() {

    }

    public User(String name, String password) {
        Name = name;
        Password = password;
    }

    public User(String name, String password, String address) {
        Name = name;
        Password = password;
        Address = address;
    }

    public User(String name, String password, String address, String email, String count) {
        Name = name;
        Password = password;
        Address = address;
        Email = email;
        Count = count;
    }

    public User(String name, String password, String address, String email, String count, String phone, String uid) {
        Name = name;
        Password = password;
        Address = address;
        Email = email;
        Count = count;
        Phone = phone;
        Uid = uid;
    }

    public String getPhone() {
        return Phone;
    }

    public void setPhone(String phone) {
        Phone = phone;
    }

    public String getUid() {
        return Uid;
    }

    public void setUid(String uid) {
        Uid = uid;
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

    public String getAddress() {
        return Address;
    }

    public void setAddress(String address) {
        Address = address;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        Email = email;
    }

    public String getCount() {
        return Count;
    }

    public void setCount(String count) {
        Count = count;
    }
}
