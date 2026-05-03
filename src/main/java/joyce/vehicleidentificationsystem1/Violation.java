package joyce.vehicleidentificationsystem1;

// Violation class
public record Violation(String type, double fineAmount, String status) {

    public Violation(String type, double fineAmount, String status) {
        this.type = type;
        this.fineAmount = fineAmount;
        this.status = status;
    }

    public String getType() { return type; }
    public double getFineAmount() { return fineAmount; }
    public String getStatus() { return status; }
}
