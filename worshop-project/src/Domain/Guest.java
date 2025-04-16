package Domain;

public class Guest {
    private IRole role;

    // New fields for guest-specific session data:
    private long userID;    // For a guest's temporary ID.
    private ShoppingCart cart;    // For the guest's (temporary) shopping cart.
    private boolean isInSession = false; // Flag to check if the guest is in session.

    public Guest() {}

    public Guest(IRole initialRole, long userID, ShoppingCart cart) {
        this.userID = userID;
        this.role = initialRole;
        this.cart = cart;
    }

    public void changeRole(IRole newRole) {
        this.role = newRole;
    }

    public boolean login(long userID, ShoppingCart cart) {
        if (isInSession) {
            System.out.println("Unauthorized Action: already logged in as guest. TempID: " + userID);
            return false;
        }
        if (userID <= 0) {
            System.out.println("Negative user ID is not valid: " + userID);
            return false;
        }
        if (cart == null) {
            System.out.println("Null cart is not valid.");
            return false;
        }
        this.isInSession = true;
        this.userID = userID;
        this.cart = cart; // According to the USE-CASE guest has an empty cart. 
        System.out.println("Guest login successful. Assigned TempID: " + userID);
        return true;
    }
    public boolean logout() {
        if (!isInSession) {
            System.out.println("Unauthorized Action: already logged out.");
            return false;
        }
        this.isInSession = false;
        this.userID = -1; // Reset tempUserID on logout.
        this.cart = null; // Clear the cart on logout.
        System.out.println("Guest logout successful. TempID cleared.");
        return true;
    }

    public boolean isInSession() {
        return this.isInSession;
    }

    public long getUserID() {
        return this.userID;
    }

    public ShoppingCart getCart() {
        return this.cart;
    }
    
    public long setUserID(long userID) {
        return this.userID;
    }

    public ShoppingCart getCart(ShoppingCart cart) {
        return this.cart;
    }
}
