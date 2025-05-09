package Domain.User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

public class Guest {
    protected ShoppingCart cart;    
    protected String sessionToken = null; // Flag to check if the guest is in session.
    
    public Guest() {}

    public Registered register(String username, String password, LocalDate dateOfBirth){
        if (!isInSession()) {
            throw new IllegalArgumentException("Unauthorized Action: User didn't entered the system yet. TempID: " + cart.getCartID());
        }
        if (!validPassword(password) || !validUsername(username)) {
            throw new IllegalArgumentException("Unauthorized Action: invalid registration details.");
        }
        Registered newUser = new Registered(username, password, dateOfBirth);
        newUser.setCart(getCart());
        return newUser;
    }

    public boolean enterToSystem(String sessionToken, int cartID) {
        if (isInSession()) {
            throw new IllegalArgumentException("Unauthorized Action: already logged in as guest. TempID: " + getUserID());
        }
        if (cartID < 0) {
            throw new IllegalArgumentException("Unauthorized Action: invalid cart ID (negative)");
        }
        this.sessionToken = sessionToken;
        this.cart = new ShoppingCart(cartID); // According to the USE-CASE guest has an empty cart. 
        return true;
    }

    public boolean logout() {
        if (!isInSession()) {
            return false;
        }
        this.sessionToken = null;
        this.cart = null; // Clear the cart on logout.
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
    public boolean isSuspended() {
        return false; 
    }

}
