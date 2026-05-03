package joyce.vehicleidentificationsystem1;

public class User {

        private int userId;
        private String username;
        private String fullName;
        private String email;
        private String phone;
        private String address;
        private String role;
        private String status;
        private String password;

        public User() {}

        public User(String username, String password, String fullName, String email, String role) {
            this.username = username;
            this.password = password;
            this.fullName = fullName;
            this.email = email;
            this.role = role;
            this.status = "Pending";
        }

        // Getters
        public int getUserId() { return userId; }
        public String getUsername() { return username; }
        public String getPassword() { return password; }
        public String getFullName() { return fullName; }
        public String getEmail() { return email; }
        public String getPhone() { return phone; }
        public String getAddress() { return address; }
        public String getRole() { return role; }
        public String getStatus() { return status; }

        // Setters
        public void setUserId(int userId) { this.userId = userId; }
        public void setUsername(String username) { this.username = username; }
        public void setPassword(String password) { this.password = password; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        public void setEmail(String email) { this.email = email; }
        public void setPhone(String phone) { this.phone = phone; }
        public void setAddress(String address) { this.address = address; }
        public void setRole(String role) { this.role = role; }
        public void setStatus(String status) { this.status = status; }

        @Override
        public String toString() {
            return fullName + " (" + username + ") - " + role;
        }

}
