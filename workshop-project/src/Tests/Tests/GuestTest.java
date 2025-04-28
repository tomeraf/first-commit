package Tests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import Domain.Guest;

public class GuestTest {
    private Guest guest;

    @BeforeEach
    void setUp() {
        guest = new Guest(); // Use any dummy role
    }

    @Test
    void testSuccessfulLogin() {
        boolean result = guest.enterToSystem("1", 0);
        assertTrue(result);
        assertTrue(guest.isInSession());
        assertEquals(0, guest.getUserID());
    }

    @Test
    void testLoginTwiceShouldNotOverride() {
        boolean result1 = guest.enterToSystem("1", 0);
        boolean result2 = guest.enterToSystem("1", 1); // Should not take effect
        assertTrue(result1); // First login should succeed
        assertFalse(result2); // Login should fail since already logged in
        assertTrue(guest.isInSession());
        assertEquals(0, guest.getUserID());
    }

    @Test
    void testSuccessfulLogout() {
        boolean result1 = guest.enterToSystem("1", 0);
        assertTrue(result1); // First login should succeed
        assertTrue(guest.isInSession());
        assertEquals(0, guest.getUserID());
        assertNotNull(guest.getCart()); // Cart should be initialized on login
        boolean result2 = guest.logout();
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
        guest.enterToSystem("1", 0);
        assertTrue(guest.logout());
        assertFalse(guest.logout()); // Second one should fail

        assertFalse(guest.isInSession());
        assertEquals(-1, guest.getUserID());
        assertNull(guest.getCart());
    }

    @Test
    void testLoginWithNegativeID() {
        boolean result = guest.enterToSystem("1", -1);

        assertFalse(result);
    }
}
