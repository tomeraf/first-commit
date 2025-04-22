package Tests;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestFactory;

import Domain.Manager;
import Domain.Permission;

public class ManagerTest {
    private Manager manager;

    @BeforeEach
    public void setUp() {
        // Create a Manager object with appointerID, shopID, and permissions
        int appointerID = 1;
        int shopID = 101;
        HashSet<Permission> permissions = new HashSet<>();
        permissions.add(Permission.UPDATE_ITEM_QUANTITY);
        manager = new Manager(appointerID, shopID, permissions);
    }

    @Test
    void testHasPermission() {
        assertTrue(manager.hasPermission(Permission.UPDATE_ITEM_QUANTITY));
    }
    @Test
    void testHasNoPermission() {
        assertFalse(manager.hasPermission(Permission.VIEW));
    }

    @Test
    void testAddPermission() {
        manager.addPermission(Permission.VIEW);
        assertTrue(manager.hasPermission(Permission.VIEW));
    }
    @Test
    void testAddDuplicatePermission() {
        // Adding the same permission again should not change anything
        manager.addPermission(Permission.UPDATE_ITEM_QUANTITY);
        assertTrue(manager.hasPermission(Permission.UPDATE_ITEM_QUANTITY));
    }

    @Test
    void testRemovePermission() {
        manager.removePermission(Permission.UPDATE_ITEM_QUANTITY);
        assertFalse(manager.hasPermission(Permission.UPDATE_ITEM_QUANTITY));
    }
    @Test
    void testRemoveNonExistentPermission() {
        // Trying to remove a permission that doesn't exist should not change anything
        manager.removePermission(Permission.VIEW);
        assertFalse(manager.hasPermission(Permission.VIEW));
    }
    @Test
    void testGetShopID() {
        assertEquals(101, manager.getShopID());
    }
    @Test
    void testGetAppointer() {
        assertEquals(1, manager.getAppointer());
    }
    @Test
    void testAddAppointment() {
        // Adding an appointment should throw an exception
        assertThrows(UnsupportedOperationException.class, () -> {
            manager.AddAppointment(2);
        });
    }
    @Test
    void testRemoveAppointment() {
        // Removing an appointment should throw an exception
        assertThrows(UnsupportedOperationException.class, () -> {
            manager.RemoveAppointment(2);
        });
    }
    @Test
    void testGetAppointments() {
        // Getting appointments should return null
        assertNull(manager.getAppointments());
    }
    @Test
    void testGetPermissions() {
        HashSet<Permission> expectedPermissions = new HashSet<>();
        expectedPermissions.add(Permission.UPDATE_ITEM_QUANTITY);
        assertEquals(expectedPermissions, manager.getPermissions());
    }
}
