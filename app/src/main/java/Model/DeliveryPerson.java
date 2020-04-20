package Model;

public class DeliveryPerson {
    private String Name, Password, Email, Address, Phone, Radius, Status, Lat, Lng;

    public DeliveryPerson() {
    }

    public DeliveryPerson(String name, String password, String email, String address, String phone, String radius, String status, String lat, String lng) {
        Name = name;
        Password = password;
        Email = email;
        Address = address;
        Phone = phone;
        Radius = radius;
        Status = status;
        Lat = lat;
        Lng = lng;
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

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        Email = email;
    }

    public String getAddress() {
        return Address;
    }

    public void setAddress(String address) {
        Address = address;
    }

    public String getPhone() {
        return Phone;
    }

    public void setPhone(String phone) {
        Phone = phone;
    }

    public String getRadius() {
        return Radius;
    }

    public void setRadius(String radius) {
        Radius = radius;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }

    public String getLat() {
        return Lat;
    }

    public void setLat(String lat) {
        Lat = lat;
    }

    public String getLng() {
        return Lng;
    }

    public void setLng(String lng) {
        Lng = lng;
    }
}
