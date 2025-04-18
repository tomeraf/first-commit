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
        boolean result = guest.login(tempId, 0);
        assertTrue(result);
        assertTrue(guest.isInSession());
        assertEquals(tempId, guest.getUserID());
    }

    @Test
    void testLoginTwiceShouldNotOverride() {
        long tempId1 = 111L;
        long tempId2 = 222L;

        boolean result1 = guest.login(tempId1, 0);
        boolean result2 = guest.login(tempId2, 0); // Should not take effect
        assertTrue(result1); // First login should succeed
        assertFalse(result2); // Login should fail since already logged in
        assertTrue(guest.isInSession());
        assertEquals(tempId1, guest.getUserID());
    }

    @Test
    void testSuccessfulLogout() {
        long tempId = 999L;
        boolean result1 = guest.login(tempId, 0);
        assertTrue(result1); // First login should succeed
        assertTrue(guest.isInSession());
        assertEquals(tempId, guest.getUserID());
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
        long id = 555L;
        guest.login(id, 0);
        assertTrue(guest.logout());
        assertFalse(guest.logout()); // Second one should fail

        assertFalse(guest.isInSession());
        assertEquals(-1, guest.getUserID());
        assertNull(guest.getCart());
    }

    @Test
    void testLoginWithNegativeID() {
        long id = -1;
        boolean result = guest.login(id, 0);

        assertFalse(result);
    }
}
