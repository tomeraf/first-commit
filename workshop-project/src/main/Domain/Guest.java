package Domain;

import java.time.LocalDate;

public class Guest {
    protected ShoppingCart cart;    
    protected String sessionToken = null; // Flag to check if the guest is in session.
    
    public Guest() {}

    public Registered register(String username, String password, LocalDate dateOfBirth) {
        if (!isInSession()) {
            System.out.println("Unauthorized Action: User didn't entered the system yet. TempID: " + cart.getCartID());
            return null;
        }
        if (!validPassword(password) || !validUsername(username)) {
            System.out.println("Invalid registration details.");
            return null;
        }
        Registered newUser = new Registered(username, password, dateOfBirth);
        newUser.setCart(getCart());
        System.out.println("Guest registration successful. UserID: " + newUser.getUserID());
        return newUser;
    }

    public boolean enterToSystem(String sessionToken, int cartID) {
        if (isInSession()) {
            System.out.println("Unauthorized Action: already logged in as guest. TempID: " + getUserID());
            return false;
        }
        if (cartID < 0) {
            System.out.println("Unauthorized Action: invalid cart ID (negative)");
            return false;
        }
        this.sessionToken = sessionToken;
        this.cart = new ShoppingCart(cartID); // According to the USE-CASE guest has an empty cart. 
        System.out.println("Guest login successful. Assigned TempID: " + cartID);
        return true;
    }

    public boolean logout() {
        if (!isInSession()) {
            System.out.println("Unauthorized Action: already logged out.");
            return false;
        }
        this.sessionToken = null;
        this.cart = null; // Clear the cart on logout.
        System.out.println("Guest logout successful. TempID cleared.");
        return true;
    }

    public boolean isInSession() {
        return this.sessionToken != null;
    }

    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }

    public int getUserID() {
        if (this.getCart() == null) return -1;
        return this.getCart().getCartID();
    }
    public String getUsername() { return null; }
    public ShoppingCart getCart() {
        return this.cart;
    }

    public void setCart(ShoppingCart cart) {
        this.cart = cart;
    }

    private boolean validUsername(String username) {
        return username != null && !username.isEmpty();
    }
    private boolean validPassword(String password) {
        return password != null && !password.isEmpty();
    }
}
