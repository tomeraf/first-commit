// package Tests;

// import Domain.Registered;
// import Domain.IRole;
// import Domain.Permission;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;

// import java.time.LocalDate;
// import java.time.Period;
// import java.util.HashSet;
// import java.util.List;
// import java.util.Map;
// import java.util.Set;
// import java.util.ArrayList;

// import static org.junit.jupiter.api.Assertions.*;

// public class RegisteredTests {
//     private Registered registered;
//     private IRole stubRole;
//     private final int shopId = 1;

//     @BeforeEach
//     void setUp() {
//         // Use a birth date 30 years ago for getAge tests
//         LocalDate dob = LocalDate.now().minusYears(30);
//         registered = new Registered("user", "pass", dob);
//         //stubRole = new StubRole();
//         registered.setRoleToShop(shopId, stubRole);
//     }

//     @Test
//     void testRoleAssignmentAndRetrieval() {
//         assertSame(stubRole, registered.getRoleInShop(shopId));
//     }

//     @Test
//     void testHasPermissionWhenNoneSet() {
//         // Pick any existing Permission value for testing
//         Permission permission = Permission.values()[0];
//         assertFalse(registered.hasPermission(shopId, permission));
//     }

//     @Test
//     void testAddAndRemovePermission() {
//         Permission permission = Permission.values()[0];
//         registered.addPermission(shopId, permission);
//         assertTrue(registered.hasPermission(shopId, permission));
//         registered.removePermission(shopId, permission);
//         assertFalse(registered.hasPermission(shopId, permission));
//     }

//     @Test
//     void testAddAndRemoveAppointment() {
//         int nomineeId = 42;
//         registered.addAppointment(shopId, nomineeId);
//         Map<Integer, IRole> apps = registered.getAppointments(shopId);
//         assertNotNull(apps);
//         assertEquals(1, apps.size());
//         assertTrue(apps.contains(nomineeId));
//         assertEquals(nomineeId, registered.getAppointer(shopId));

//         registered.removeAppointment(shopId, nomineeId);
//         apps = registered.getAppointments(shopId);
//         assertTrue(apps.isEmpty());
//     }

//     @Test
//     void testGetAppointmentsAndAppointerNoRole() {
//         int unknownShop = 99;
//         assertNull(registered.getAppointments(unknownShop));
//         assertEquals(-1, registered.getAppointer(unknownShop));
//     }

//     @Test
//     void testBasketOperationsRequiresLogin() {
//         // Before login, cart should be null
//         assertNull(registered.getCart());
//         // Perform login to initialize the shopping cart
//         registered.login(100L, 0);
//         assertNotNull(registered.getCart());

//         // Test adding and removing items
//         registered.addItemToBasket(10);
//         assertTrue(registered.getCart().getItems().contains(10));
//         registered.removeItemFromBasket(10);
//         assertFalse(registered.getCart().getItems().contains(10));
//     }

//     @Test
//     void testGetAgeReturnsCorrectYears() {
//         LocalDate dob = LocalDate.now().minusYears(25).minusMonths(3);
//         Registered r2 = new Registered("u2", "p2", dob);
//         int expected = Period.between(dob, LocalDate.now()).getYears();
//         assertEquals(expected, r2.getAge());
//     }
// }
