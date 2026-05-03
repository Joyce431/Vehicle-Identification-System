package joyce.vehicleidentificationsystem1;

import java.time.LocalDateTime;

public abstract class Person {
    protected String id;
    protected String name;
    protected String email;
    protected String phone;
    protected String address;
    protected LocalDateTime registeredDate;

    public Person() {
        this.registeredDate = LocalDateTime.now();
    }

    public Person(String id, String name, String email, String phone, String address) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.registeredDate = LocalDateTime.now();
    }

    // Abstract methods - Polymorphism
    public abstract String getRole();
    public abstract boolean hasAccess(String module);

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public LocalDateTime getRegisteredDate() { return registeredDate; }

    // Common method
    public String getDisplayInfo() {
        return name + " (" + getRole() + ")";
    }

    @Override
    public String toString() {
        return String.format("%s [ID: %s, Name: %s, Role: %s]",
                getClass().getSimpleName(), id, name, getRole());
    }
}