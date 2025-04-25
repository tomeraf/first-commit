package Tests;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashSet;

import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;

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
    public void testHasPermission() {
        assertTrue(manager.hasPermission(Permission.UPDATE_ITEM_QUANTITY));
    }
    @Test
    public void testHasNoPermission() {
        assertFalse(manager.hasPermission(Permission.VIEW));
    }

    @Test
    public void testAddPermission() {
        manager.addPermission(Permission.VIEW);
        assertTrue(manager.hasPermission(Permission.VIEW));
    }
    @Test
    public void testAddDuplicatePermission() {
        // Adding the same permission again should not change anything
        manager.addPermission(Permission.UPDATE_ITEM_QUANTITY);
        assertTrue(manager.hasPermission(Permission.UPDATE_ITEM_QUANTITY));
    }
    @Test
    public void testRemovePermission() {
        manager.removePermission(Permission.UPDATE_ITEM_QUANTITY);
        assertFalse(manager.hasPermission(Permission.UPDATE_ITEM_QUANTITY));
    }
    @Test
    public void testRemoveNonExistentPermission() {
        // Trying to remove a permission that doesn't exist should not change anything
        manager.removePermission(Permission.VIEW);
        assertFalse(manager.hasPermission(Permission.VIEW));
    }
    @Test
    public void testGetShopID() {
        assertEquals(101, manager.getShopID());
    }
    @Test
    public void testGetAppointer() {
        assertEquals(1, manager.getAppointer());
    }
    @Test
    public void testGetAppointments() {
        // Getting appointments should return null
        assertNull(manager.getAppointments());
    }
}
