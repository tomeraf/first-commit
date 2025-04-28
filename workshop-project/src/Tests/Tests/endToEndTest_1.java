package Tests;
import Service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import Service.ShopService;
import Service.UserService;
import Domain.Guest;
import Domain.Manager;
import Domain.Owner;
import Domain.Registered;
import Domain.Response;
import Domain.Shop;
import Domain.DTOs.ItemDTO;
import Domain.DTOs.ShopDTO;
import Domain.Category;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import Domain.DTOs.Order;
import Domain.Permission;
import Domain.Adapters_and_Interfaces.*;
import Domain.Repositories.*;


public class endToEndTest_1 {

    private IShopRepository shopRepository;
    private IUserRepository userRepository;
    private IOrderRepository orderRepository;
    private IAuthentication jwtAdapter;
    private IShipment shipment;
    private IPayment payment;
    private UserService userService;
    private ShopService shopService;
    private OrderService cartService;
    
    
    public endToEndTest_1() {
        shopRepository = new MemoryShopRepository();
        userRepository = new MemoryUserRepository();
        orderRepository = new MemoryOrderRepository();
        jwtAdapter = new JWTAdapter();
        shipment = new ProxyShipment();
        payment = new ProxyPayment();


        userService = new UserService(userRepository, shopRepository, orderRepository, jwtAdapter, payment, shipment);
        shopService = new ShopService(userRepository, shopRepository, orderRepository, jwtAdapter);
        cartService = new OrderService();
    }

    @Test
    public void successfulGuestLogin() {
        Response<String> guestToken = userService.enterToSystem();
        assertNotNull(guestToken.getData(), "Guest login failed: token is null.");
        Response<List<ItemDTO>> items = cartService.checkCartContent(guestToken.getData());
        assertNotNull(items.getData(), "Guest login failed: cart content is null.");
        assertTrue(items.getData().isEmpty(), "Guest login failed: cart is not empty.");
    }

    @Test
    public void successfulGuestExit() {
        Response<String> guestToken = userService.enterToSystem();
        assertNotNull(cartService.checkCartContent(guestToken.getData()).getData(), "Guest login failed: cart content is null.");
        assertTrue(cartService.checkCartContent(guestToken.getData()).getData().isEmpty(), "Guest login failed: cart is not empty.");
        Response<Void> res = userService.exitAsGuest(guestToken.getData());
        assertTrue(res.isOk());
        assertNull(cartService.checkCartContent(guestToken.getData()).getData(), "checkCartContent should return null after exit");
    }

    @Test
    public void successfulGuestRegister() {
        
        Response<String> guestToken = userService.enterToSystem();
        Response<Void> res = userService.registerUser(guestToken.getData(), "user123", "password", LocalDate.now().minusYears(20));
        assertTrue(res.isOk());
        Response<String> userToken = userService.loginUser(guestToken.getData(), "user123", "password");

        assertTrue(userToken.isOk());
        assertNotNull(userToken.getData(), "loginUser should return a valid token after registration");

        // 4. verify cart transferred (empty)
        Response<List<ItemDTO>> cart = cartService.checkCartContent(userToken.getData());
        assertNotNull(cart.getData(), "Cart should not be null for registered user");
        assertTrue(cart.getData().isEmpty(), "Cart should remain empty after registration and login");
    }

    public void failedGuestRegister() {
        Response<String> guestToken = userService.enterToSystem();
        Response<Void> res = userService.registerUser(guestToken.getData(), "user123", "password", LocalDate.now().minusYears(20));
        assertTrue(res.isOk());

        Response<String> guestToken_2 = userService.enterToSystem();
        Response<Void> res_2 = userService.registerUser(guestToken_2.getData(), "user123", "password", LocalDate.now().minusYears(20));
        assertFalse(res_2.isOk());
    }

    @Test
    public void userRegisterUnauthorized() {
        Response<String> guestToken = userService.enterToSystem();
        
        Response<Void> res = userService.registerUser(guestToken.getData(), "user123", "password", LocalDate.now().minusYears(20));
        assertTrue(res.isOk());
        
        Response<String> userToken = userService.loginUser(guestToken.getData(), "user123", "password");
        assertTrue(userToken.isOk());
        assertNotNull(userToken.getData(), "loginUser should return a valid token after registration");

        // lavi

        // 2. attempt to register again using registered user's token
        Response<Void> res2 = userService.registerUser(userToken.getData(), "anotherUser", "pwd2", LocalDate.now().minusYears(30));
        assertFalse(res2.isOk());
        
        // 3a. ensure old user session still active
        Response<List<ItemDTO>> cart = cartService.checkCartContent(userToken.getData());
        assertNotNull(cart.getData(), "Existing user session should remain active after unauthorized register attempt");

        Response<String> newToken = userService.loginUser(userToken.getData(), "anotherUser", "pwd2");
        assertFalse(newToken.isOk());
    }

    @Test
    public void successfulUserLogin() {
        Response<String> guestToken = userService.enterToSystem();
        Response<Void> res = userService.registerUser(guestToken.getData(), "user123", "password", LocalDate.now().minusYears(20));

        assertTrue(res.isOk());
        Response<String> userToken = userService.loginUser(guestToken.getData(), "user123", "password");

        assertTrue(userToken.isOk());
        assertNotNull(userToken.getData(), "loginUser should return a valid token after registration");

    }

    @Test
    public void nameWrongUserLogin() {
        Response<String> guestToken = userService.enterToSystem();
        Response<Void> res = userService.registerUser(guestToken.getData(), "user123", "password", LocalDate.now().minusYears(20));
        
        assertTrue(res.isOk());

        // 2. attempt login with wrong username
        Response<String> userToken = userService.loginUser(guestToken.getData(), "wrongName", "password");
        assertNull(userToken.getData(), "Login should fail when username is incorrect");
    }

    @Test
    public void passwordWrongUserLogin() {
        Response<String> guestToken = userService.enterToSystem();
        Response<Void> res = userService.registerUser(guestToken.getData(), "user123", "password", LocalDate.now().minusYears(20));
        
        assertTrue(res.isOk());

        // 2. attempt login with wrong username
        Response<String> userToken = userService.loginUser(guestToken.getData(), "user123", "pwd");
        assertFalse(userToken.isOk());
        assertNull(userToken.getData(), "Login should fail when username is incorrect");
    }

    // @Test
    // public void getShopsAndItems() {
    //     // user enters system, no shops exist yet
    //     Response<String> guestToken = userService.enterToSystem();
    //     Response<List<ShopDTO>> shops = shopService.showAllShops();
    //     // assertNotNull(shops.getData(), "showAllShops should not return null");
    //     // assertTrue(shops.getData().isEmpty(), "Initially there should be no shops");
    // }

    // @Test
    // public void searchItemsWithFilters() {
    //     // user enters system, filtering on empty repository
    //     Response<String> guestToken = userService.enterToSystem();
    //     HashMap<String, String> filters = new HashMap<>();
    //     filters.put("name", "nothing");
    //     Response<List<ItemDTO>> filtered = shopService.filterItemsAllShops(filters);
    //     assertNotNull(filtered.getData(), "filterItemsAllShops should not return null");
    //     assertTrue(filtered.getData().isEmpty(), "Filtering on empty repo should yield no items");
    // }
    
    //@Test
    
    
    // @Test
    // public void searchItemsWithPopulatedShop() {
    //     // Setup: create an owner and a shop with items
    //     String guestToken = userService.enterToSystem();
    //     userService.registerUser(guestToken, "owner", "pwd", LocalDate.now().minusYears(30));
    //     String ownerToken = userService.loginUser(guestToken, "owner", "pwd");
    //     assertNotNull(ownerToken, "Owner login should succeed");
        
    //     ShopDTO shop = shopService.createShop(ownerToken, "MyShop", "desc");
    //     assertNotNull(shop, "Shop creation should succeed");
        
    //     // Add three items
    //     shopService.addItemToShop(ownerToken, shop.getId(), "Apple", Category.FOOD, 1.00, "fresh apple");
    //     shopService.addItemToShop(ownerToken, shop.getId(), "Banana", Category.FOOD, 0.50, "ripe banana");
    //     shopService.addItemToShop(ownerToken, shop.getId(), "Laptop", Category.ELECTRONICS, 999.99, "new laptop");

    //     // Filter by name exactly "Apple"
    //     HashMap<String, String> nameFilter = new HashMap<>();
    //     nameFilter.put("name", "Apple");
    //     List<ItemDTO> byName = shopService.filterItemsAllShops(nameFilter);
    //     assertNotNull(byName);
    //     assertEquals(1, byName.size(), "Only one item should match name 'Apple'");
    //     assertEquals("Apple", byName.get(0).getName());

    //     // Filter by category FOOD (should yield 2 items)
    //     HashMap<String, String> catFilter = new HashMap<>();
    //     catFilter.put("category", "FOOD");
    //     List<ItemDTO> byCategory = shopService.filterItemsAllShops(catFilter);
    //     assertNotNull(byCategory);
    //     assertEquals(2, byCategory.size(), "Two items should match category FOOD");
    // }

/*
    @Test
    public void userLogsInAsGuestGood() {
        // register and login
        String guestToken = userService.enterToSystem();
        userService.registerUser(guestToken, "user2", "pass2", LocalDate.now().minusYears(25));
        String userToken = userService.loginUser(guestToken, "user2", "pass2");
        // logout to guest
        String newGuestToken = userService.logoutRegistered(userToken);
        assertNotNull(newGuestToken, "Logout to guest should return a new token");
        // now guest login
        String guestAgain = userService.enterToSystem();
        assertNotNull(guestAgain, "Guest login after logout should succeed");
        List<ItemDTO> cart = userService.checkCartContent(guestAgain);
        assertTrue(cart.isEmpty(), "Guest cart should be empty after relogin");
    }


    @Test
    public void successfulGuestRegister() {
        String guestToken = userService.enterToSystem();
        userService.registerUser(guestToken, "newUser", "newPass", LocalDate.now().minusYears(22));
        // now login as new user
        String newToken = userService.loginUser(guestToken, "newUser", "newPass");
        assertNotNull(newToken, "Newly registered user should be able to login");
        List<ItemDTO> cart = userService.checkCartContent(newToken);
        assertTrue(cart.isEmpty(), "Transferred cart should remain empty");
    }

    @Test
    public void userRegisterUnauthorized() {
        // create and login registered
        String guestToken = userService.enterToSystem();
        userService.registerUser(guestToken, "user4", "pass4", LocalDate.now().minusYears(21));
        String userToken = userService.loginUser(guestToken, "user4", "pass4");
        assertThrows(RuntimeException.class,
                () -> userService.registerUser(userToken, "hacker", "hack", LocalDate.now()),
                "Registered user should not register again");
    }

    @Test
    public void nameWrongUserLogin() {
        String guestToken = userService.enterToSystem();
        userService.registerUser(guestToken, "u6", "p6", LocalDate.now().minusYears(24));
        assertThrows(RuntimeException.class,
                () -> userService.loginUser(guestToken, "wrongName", "p6"),
                "Wrong username should fail login");
    }

    @Test
    public void passwordWrongUserLogin() {
        String guestToken = userService.enterToSystem();
        userService.registerUser(guestToken, "u7", "p7", LocalDate.now().minusYears(24));
        assertThrows(RuntimeException.class,
                () -> userService.loginUser(guestToken, "u7", "wrongPass"),
                "Wrong password should fail login");
    }

    @Test
    public void addItemToAShopTest() {
        
        String guestToken = userService.enterToSystem();
        userService.registerUser(guestToken, "Benru", "181222", LocalDate.now().minusYears(20));
        String registeredToken = userService.loginUser(guestToken, "Benru", "181222");

        ShopDTO shop = shopService.createShop(registeredToken, "TSLA", "A place to buy stocks");
        List<ShopDTO> shopDTOs = shopService.showAllShops();

        assertEquals(1, shopDTOs.size());
        assertEquals("TSLA", shop.getName());
        assertEquals("A place to buy stocks", shop.getDescription());
        assertEquals(0, shop.getItems().size());

        // Add a way to get the managers and owners of the shop (to the DTO)
        // function should get a shopid and return the owners and managers

        List<Manager> managers = shopService.getManagersByShopId(shop.getId());
        List<Owner> owners = shopService.getOwnersByShopId(shop.getId());
        assertEquals(0, managers.size());
        assertEquals(1, owners.size());
        
        ItemDTO item = shopService.addItemToShop(registeredToken, shop.getId(), "Car", Category.ELECTRONICS, 125000, "Tesla Model S");
        shopService.changeItemQuantityInShop(registeredToken, shop.getId(), item.getItemID(), 10);
        assertEquals(1, shop.getItems().size());
        assertEquals("Car", item.getName());
        assertEquals(Category.ELECTRONICS, item.getCategory());
        assertEquals(125000, item.getPrice());
        assertEquals("Tesla Model S", item.getDescription());
        assertEquals(10, item.getQuantity());

        // Now guest wants to buy
        String guestToken_2 = userService.enterToSystem();
        userService.registerUser(guestToken_2, "lavi", "12345", LocalDate.now().minusYears(20));
        String registeredToken_2 = userService.loginUser(guestToken_2, "lavi", "12345");
        userService.loginUser(registeredToken_2, "lavi", "12345");
        List<ItemDTO> items = new ArrayList<>();
        items.add(shop.getItems().get(0));

        userService.addItemsToCart(guestToken_2, items);

        List<ItemDTO> cartItems = userService.checkCartContent(guestToken_2);
        assertEquals(1, cartItems.size());
        assertEquals("Car", cartItems.get(0).getName());
        assertEquals(Category.ELECTRONICS, cartItems.get(0).getCategory());
        assertEquals(125000, cartItems.get(0).getPrice());
        assertEquals("Tesla Model S", cartItems.get(0).getDescription());

        ShopDTO shopInfo = shopService.getShopInfo(guestToken_2, cartItems.get(0).getShopId());
        assertEquals(9, shopInfo.getItems().get(cartItems.get(0).getItemID()));
        
        Order order = userService.buyCartContent(guestToken_2);
        assertEquals(1, order.getItems().size());
        assertEquals("Car", order.getItems().get(0).getName());
        assertEquals(Category.ELECTRONICS, order.getItems().get(0).getCategory());
        assertEquals(125000, order.getItems().get(0).getPrice());
        assertEquals("Tesla Model S", order.getItems().get(0).getDescription());
        assertEquals(shopInfo.getId(), order.getItems().get(0).getShopId());
        
        List<Order> orders = userService.viewPersonalOrderHistory(registeredToken_2);
        assertEquals(1, orders.size());
        assertEquals(1, orders.get(0).getItems().size());
        assertEquals("Car", orders.get(0).getItems().get(0).getName());
        assertEquals(Category.ELECTRONICS, orders.get(0).getItems().get(0).getCategory());
        assertEquals(125000, orders.get(0).getItems().get(0).getPrice());
        assertEquals("Tesla Model S", orders.get(0).getItems().get(0).getDescription());
        assertEquals(shopInfo.getId(), orders.get(0).getItems().get(0).getShopId());

        userService.exitAsGuest(guestToken_2);
    }

    @Test
    public void shopManagmentTest()
    {

        String guestToken = userService.enterToSystem();
        userService.registerUser(guestToken, "Benru", "181222", null);
        String registeredToken = userService.loginUser(guestToken, "Benru", "181222");

        String guestToken_2 = userService.enterToSystem();
        userService.registerUser(guestToken_2, "lavi", "12345", null);
        String registeredToken_2 = userService.loginUser(guestToken_2, "lavi", "12345");

        String guestToken_3 = userService.enterToSystem();
        userService.registerUser(guestToken_3, "ori", "54321", null);
        String registeredToken_3 = userService.loginUser(guestToken_3, "ori", "54321");

        ShopDTO shop = shopService.createShop(registeredToken, "TSLA", "A place to buy stocks");
        Set<Permission> permissions = new HashSet<>();
        permissions.add(Permission.APPOINTMENT);
        
        
        
        List<Manager> managers = shopService.getManagersByShopId(shop.getId());
        List<Owner> owners = shopService.getOwnersByShopId(shop.getId());
        assertEquals(0, managers.size());
        assertEquals(1, owners.size());
        
        shopService.addShopManager(registeredToken, shop.getId(), "lavi", permissions);

        managers = shopService.getManagersByShopId(shop.getId());
        owners = shopService.getOwnersByShopId(shop.getId());
        assertEquals(1, managers.size());
        assertEquals(1, owners.size());

        shopService.addShopManager(registeredToken_2, shop.getId(), "ori", permissions);

        managers = shopService.getManagersByShopId(shop.getId());
        owners = shopService.getOwnersByShopId(shop.getId());
        assertEquals(2, managers.size());
        assertEquals(1, owners.size());

        shopService.removeAppointment(registeredToken, shop.getId(), "lavi");
        
        managers = shopService.getManagersByShopId(shop.getId());
        owners = shopService.getOwnersByShopId(shop.getId());
        assertEquals(0, managers.size());
        assertEquals(1, owners.size());
    }
}
    */
}