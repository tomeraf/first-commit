package Tests;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.List;

import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;

import Domain.Owner;
import Domain.Permission;

public class OwnerTest {
    private Owner owner;       
     List<Integer> appointments;

    @BeforeEach
    public void setUp() {
        // Create a Owner object with appointerID, shopID, and permissions
        int appointerID = -1; // -1 for founder
        int shopID = 101;
        owner = new Owner(appointerID, shopID);
        appointments = owner.getAppointments();
    }
    @Test
    void testHasPermission() {
        assertTrue(owner.hasPermission(Permission.UPDATE_ITEM_QUANTITY));
    }
    @Test
    void testRemoveNonExistentPermission() {
        // Trying to remove a permission that doesn't exist should not change anything
        owner.removePermission(Permission.VIEW);
        assertFalse(owner.hasPermission(Permission.VIEW));
    }
    @Test
    void testAddAppointment() {
        owner.addAppointment(2);
        assertTrue(owner.getAppointments().contains(2));
    }
    @Test
    void testRemoveAppointment() {
        appointments.add(2);
        owner.removeAppointment(2);
        assertFalse(owner.getAppointments().contains(2));
    }
    @Test
    void testGetAppointments() {
        appointments.add(2);
        assertTrue(appointments.contains(2));
    }
    @Test
    void testGetAppointer() {
        assertEquals(-1, owner.getAppointer());
    }
    @Test
    void testGetShopID() {
        assertEquals(101, owner.getShopID());
    }
}
