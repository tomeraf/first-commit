package Tests;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import Domain.Guest;
import Domain.IRole;
import Domain.ShoppingCart;

public class GuestTest {
    private Guest guest;

    @BeforeEach
    void setUp() {
        guest = new Guest(); // Use any dummy role
    }

    @Test
    void testSuccessfulLogin() {
        long tempId = 123456L;
        ShoppingCart emptyCart = new ShoppingCart();
        boolean result = guest.login(tempId, emptyCart);
        assertTrue(result);
        assertTrue(guest.isInSession());
        assertEquals(tempId, guest.getUserID());
        assertEquals(emptyCart, guest.getCart());
    }

    @Test
    void testLoginTwiceShouldNotOverride() {
        long tempId1 = 111L;
        long tempId2 = 222L;
        ShoppingCart cart1 = new ShoppingCart();
        ShoppingCart cart2 = new ShoppingCart();

        boolean result1 = guest.login(tempId1, cart1);
        boolean result2 = guest.login(tempId2, cart2); // Should not take effect
        assertTrue(result1); // First login should succeed
        assertFalse(result2); // Login should fail since already logged in
        assertTrue(guest.isInSession());
        assertEquals(tempId1, guest.getUserID());
        assertEquals(cart1, guest.getCart());
    }

    @Test
    void testSuccessfulLogout() {
        long tempId = 999L;
        ShoppingCart emptyCart = new ShoppingCart();
        boolean result1 = guest.login(tempId, emptyCart);
        boolean result2 = guest.logout();
        assertTrue(result1); // First login should succeed
        assertTrue(result2); // Logout should succeed
        assertFalse(guest.isInSession());
        assertEquals(-1, guest.getUserID());
        assertNull(guest.getCart());
    }

    @Test
    void testLogoutWithoutLogin() {
        boolean result = guest.logout();

        assertFalse(result);
    }

    @Test
    void testDoubleLogout() {
        long id = 555L;
        ShoppingCart cart = new ShoppingCart();

        guest.login(id, cart);
        assertTrue(guest.logout());
        assertFalse(guest.logout()); // Second one should fail

        assertFalse(guest.isInSession());
        assertEquals(-1, guest.getUserID());
        assertNull(guest.getCart());
    }

    @Test
    void testLoginWithNullCart() {
        long id = 777L;
        boolean result = guest.login(id, null);

        assertFalse(result);
    }

    @Test
    void testLoginWithNegativeID() {
        long id = -1;
        boolean result = guest.login(id, new ShoppingCart());

        assertFalse(result);
    }
}
