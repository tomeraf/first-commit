package Tests;

import Domain.*;
import Domain.DTOs.ItemDTO;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class RegisteredTest {
    private static final int APPOINTER_ID = 1;
    private static final int APPOINTEE_ID = 2;
    private static final int SHOP_ID = 10;

    @Test
    void testAddPermission() {
        Registered user = new Registered("bob", "hunter2", LocalDate.of(1990, 1, 1));
        Manager mgrRole = new Manager(APPOINTER_ID, SHOP_ID, new HashSet<>());
        user.setRoleToShop(SHOP_ID, mgrRole);

        assertTrue(user.addPermission(SHOP_ID, Permission.VIEW));
        assertTrue(mgrRole.hasPermission(Permission.VIEW));
    }

    @Test
    void testAddPermissionNoRole() {
        Registered user = new Registered("bob", "hunter2", LocalDate.of(1990, 1, 1));
        assertFalse(user.addPermission(SHOP_ID, Permission.VIEW));
    }

    @Test
    void testRemovePermission() {
        Registered user = new Registered("bob", "hunter2", LocalDate.of(1990, 1, 1));
        Manager mgrRole = new Manager(APPOINTER_ID, SHOP_ID, new HashSet<>(Collections.singleton(Permission.VIEW)));
        user.setRoleToShop(SHOP_ID, mgrRole);

        assertTrue(user.removePermission(SHOP_ID, Permission.VIEW));
        assertFalse(mgrRole.hasPermission(Permission.VIEW));
    }

    @Test
    void testRemovePermissionNoRole() {
        Registered user = new Registered("bob", "hunter2", LocalDate.of(1990, 1, 1));
        assertFalse(user.removePermission(SHOP_ID, Permission.VIEW));
    }

    @Test
    void testAddAppointmentSuccess() {
        Registered appointer = new Registered("bob", "hunter2", LocalDate.of(1990, 1, 1));
        appointer.setUserID(APPOINTER_ID);
        // Owner always has APPOINTMENT permission
        Owner owner = new Owner(APPOINTER_ID, SHOP_ID);
        appointer.setRoleToShop(SHOP_ID, owner);

        Manager appointeeRole = new Manager(APPOINTEE_ID, SHOP_ID, new HashSet<>());
        assertTrue(appointer.addAppointment(SHOP_ID, APPOINTEE_ID, appointeeRole));

        Map<Integer, IRole> apps = appointer.getAppointments(SHOP_ID);
        assertNotNull(apps);
        assertTrue(apps.containsKey(APPOINTEE_ID));
        assertEquals(APPOINTER_ID, appointer.getAppointer(SHOP_ID));
    }

    @Test
    void testAddAppointmentNoPermission() {
        // Manager without APPOINTMENT permission
        Registered appointer = new Registered("bob", "hunter2", LocalDate.of(1990, 1, 1));
        appointer.setUserID(APPOINTER_ID);
        Manager manager = new Manager(APPOINTER_ID, SHOP_ID, new HashSet<>());
        appointer.setRoleToShop(SHOP_ID, manager)
        ;
        Registered appointee = new Registered("Alice", "password", LocalDate.of(2000, 1, 1));
        appointer.setUserID(APPOINTEE_ID);
        Manager appointeeRole = new Manager(APPOINTEE_ID, SHOP_ID, new HashSet<>());
        
        assertFalse(appointer.addAppointment(SHOP_ID, APPOINTEE_ID, appointeeRole));

        assertNull(appointer.getAppointments(SHOP_ID));             // no appointments recorded
        assertEquals(-1, appointee.getAppointer(SHOP_ID));         // no appointer
    }

    @Test
    void testRemoveAppointmentSuccess() {
        Registered appointer = new Registered("bob", "hunter2", LocalDate.of(1990, 1, 1));
        appointer.setUserID(APPOINTER_ID);
        Owner owner = new Owner(APPOINTER_ID, SHOP_ID);
        appointer.setRoleToShop(SHOP_ID, owner);

        Owner appointeeRole = new Owner(APPOINTER_ID, SHOP_ID);
        assertTrue(appointer.addAppointment(SHOP_ID, APPOINTEE_ID, appointeeRole));

        assertTrue(appointer.removeAppointment(SHOP_ID, APPOINTEE_ID));
        assertTrue(appointer.getAppointments(SHOP_ID).isEmpty());

        
    }

    @Test
    void testRemoveAppointmentNoRole() {
        Registered appointer = new Registered("bob", "hunter2", LocalDate.of(1990, 1, 1));
        assertFalse(appointer.removeAppointment(SHOP_ID, APPOINTEE_ID));
    }

    @Test
    void testGetAppointmentsNoRole() {
        Registered user = new Registered("alice", "pw", LocalDate.of(2001, 2, 3));
        assertNull(user.getAppointments(SHOP_ID));
    }

    @Test
    void testGetAppointerNoRole() {
        Registered user = new Registered("alice", "pw", LocalDate.of(2001, 2, 3));
        assertEquals(-1, user.getAppointer(SHOP_ID));
    }

    @Test
    void testGetAgeAlwaysZero() {
        Registered user = new Registered("d", "d", LocalDate.of(LocalDate.now().getYear()-10, 5, 5));
        assertEquals(9, user.getAge());
    }

    @Test
    void testRemoveShopRoleSuccess() {
        Registered user = new Registered("bob", "hunter2", LocalDate.of(1990, 1, 1));
        user.setUserID(APPOINTER_ID);
        Owner owner = new Owner(APPOINTER_ID, SHOP_ID);
        user.setRoleToShop(SHOP_ID, owner);

        assertTrue(user.removeShopRole(SHOP_ID));
        assertNull(user.getRoleInShop(SHOP_ID));
    }

    @Test
    void testRemoveShopRoleNoRole() {
        Registered user = new Registered("bob", "hunter2", LocalDate.of(1990, 1, 1));
        assertFalse(user.removeShopRole(SHOP_ID));
    }
}
