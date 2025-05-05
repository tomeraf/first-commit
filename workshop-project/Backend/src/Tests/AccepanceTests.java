package Tests;

import main.Service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import main.Service.ShopService;
import main.Service.UserService;
import main.Domain.Response;
import main.Domain.DTOs.ItemDTO;
import main.Domain.DTOs.ShopDTO;
import main.Domain.Category;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.ConsoleHandler;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.*;

import main.Domain.DTOs.Order;
import main.Domain.Permission;
import main.Domain.Adapters_and_Interfaces.*;
import main.Domain.Repositories.*;
import main.Infrastructure.MemoryOrderRepository;
import main.Infrastructure.MemoryShopRepository;
import main.Infrastructure.MemoryUserRepository;

public class AccepanceTests {

    private IShopRepository shopRepository;
    private IUserRepository userRepository;
    private IOrderRepository orderRepository;
    private IAuthentication jwtAdapter;
    private IShipment shipment;
    private IPayment payment;
    private UserService userService;
    private ShopService shopService;
    private OrderService orderService;
    private ConcurrencyHandler concurrencyHandler;
    static {
        // 1) Reconfigure JUL so INFO logs don’t print timestamps
        java.util.logging.Logger root = java.util.logging.Logger.getLogger("");
        LogManager.getLogManager().reset();
        ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(new SimpleFormatter() {
            @Override
            public synchronized String format(LogRecord record) {
                return record.getMessage() + System.lineSeparator();
            }
        });
        root.addHandler(handler);

        // 2) Wrap System.err in a PrintStream that drops Byte-Buddy warnings
        PrintStream originalErr = System.err;
        System.setErr(new PrintStream(new FilterOutputStream(originalErr) {
            private final String[] blacklist = new String[] {
              "WARNING: A Java agent has been loaded dynamically",
              "WARNING: Dynamic loading of agents will be disallowed"
            };

            @Override
            public void write(byte[] b, int off, int len) throws IOException, IndexOutOfBoundsException   {
                String s = new String(b, off, len);
                for (String block : blacklist) {
                    if (s.startsWith(block) || s.contains(block)) {
                        return; // swallow
                    }
                }
                super.write(b, off, len);
            }
        }));
    }
    @BeforeEach
    public void setUp() {
        shopRepository = new MemoryShopRepository();
        userRepository = new MemoryUserRepository();
        orderRepository = new MemoryOrderRepository();
        jwtAdapter = new JWTAdapter();
        concurrencyHandler = new ConcurrencyHandler();
        shipment = mock(IShipment.class);
        payment = mock(IPayment.class);

        userService = new UserService(userRepository, jwtAdapter, concurrencyHandler);
        shopService = new ShopService(userRepository, shopRepository, orderRepository, jwtAdapter, concurrencyHandler);
        orderService = new OrderService(userRepository, shopRepository, orderRepository, jwtAdapter, payment, shipment, concurrencyHandler);
    }
    

    public String generateloginAsRegistered(String name, String password) {
        Response<String> ownerGuestResp = userService.enterToSystem();
        assertTrue(ownerGuestResp.isOk(), "Owner enterToSystem should succeed");
        String ownerGuestToken = ownerGuestResp.getData();
        assertNotNull(ownerGuestToken, "Owner guest token must not be null");

        // Owner registers
        Response<Void> ownerReg = userService.registerUser(
            ownerGuestToken, name, password, LocalDate.now().minusYears(30)
        );
        assertTrue(ownerReg.isOk(), "Owner registration should succeed");

        // Owner logs in
        Response<String> ownerLogin = userService.loginUser(
            ownerGuestToken, name, password
        );
        assertTrue(ownerLogin.isOk(), "Owner login should succeed");
        String ownerToken = ownerLogin.getData();
        assertNotNull(ownerToken, "Owner token must not be null");
        return ownerToken;
    }

    public ShopDTO generateShopAndItems(String ownerToken) {
        // 1) Owner creates the shop
        Response<ShopDTO> shopResp = shopService.createShop(
            ownerToken, 
            "MyShop", 
            "A shop for tests"
        );
        assertTrue(shopResp.isOk(), "Shop creation should succeed");
        ShopDTO shop = shopResp.getData();
        assertNotNull(shop, "Returned ShopDTO must not be null");
        int shopId = shop.getId();

        // 2) Owner adds three items
        Response<ItemDTO> addA = shopService.addItemToShop(
            ownerToken, shopId,
            "Apple", Category.FOOD, 1.00, "fresh apple"
        );
        Response<ItemDTO> addB = shopService.addItemToShop(
            ownerToken, shopId,
            "Banana", Category.FOOD, 0.50, "ripe banana"
        );
        Response<ItemDTO> addL = shopService.addItemToShop(
            ownerToken, shopId,
            "Laptop", Category.ELECTRONICS, 999.99, "new laptop"
        );
        assertTrue(addA.isOk(), "Adding Apple should succeed");
        assertTrue(addB.isOk(), "Adding Banana should succeed");
        assertTrue(addL.isOk(), "Adding Laptop should succeed");

        // 3) (Optional) bump quantities or prices if you like:
        //    here we just set Apple's stock to 10 as an example
        //    first fetch its ID
        Response<ShopDTO> infoResp = shopService.getShopInfo(ownerToken, shopId);
        assertTrue(infoResp.isOk(), "getShopInfo should succeed");
        Map<Integer,ItemDTO> map = infoResp.getData().getItems();
        assertEquals(3, map.size(), "Shop should contain exactly 3 items");
        int appleId = map.values().stream()
                        .filter(i -> i.getName().equals("Apple"))
                        .findFirst()
                        .get()
                        .getItemID();
        int bananaId = map.values().stream()
                        .filter(i -> i.getName().equals("Banana"))
                        .findFirst()
                        .get()
                        .getItemID();
        int laptopId = map.values().stream()
                        .filter(i -> i.getName().equals("Laptop"))
                        .findFirst()
                        .get()
                        .getItemID();
        Response<Void> chgQty1 = shopService.changeItemQuantityInShop(
            ownerToken, shopId, appleId, 10
        );
        Response<Void> chgQty2 = shopService.changeItemQuantityInShop(
            ownerToken, shopId, bananaId, 1
        );
        Response<Void> chgQty3 = shopService.changeItemQuantityInShop(
            ownerToken, shopId, laptopId, 0
        );
        assertTrue(chgQty1.isOk(), "changeItemQuantityInShop should succeed");
        assertTrue(chgQty2.isOk(), "changeItemQuantityInShop should succeed");
        assertTrue(chgQty3.isOk(), "changeItemQuantityInShop should succeed");

        // 4) Retrieve final list and return it
        Response<List<ItemDTO>> finalResp = shopService.showShopItems(shopId);
        assertTrue(finalResp.isOk(), "showShopItems should succeed");
        List<ItemDTO> items = finalResp.getData();
        assertNotNull(items, "Returned item list must not be null");
        assertEquals(3, items.size(), "There should be 3 items in the shop");

        return shopService.getShopInfo(ownerToken, shopId).getData();
    }

    public Order successfulBuyCartContent(String sessionToken) {
        
        when(payment.validatePaymentDetails()).thenReturn(true);
        when(payment.processPayment(1.0)).thenReturn(true);
        when(shipment.validateShipmentDetails()).thenReturn(true);
        when(shipment.processShipment(0.1)).thenReturn(true);
        Response<Order> purchaseResp = orderService.buyCartContent(
            sessionToken
        );
        assertTrue(purchaseResp.isOk(), "buyCartContent should succeed");
        Order created = purchaseResp.getData();
        assertNotNull(created, "Returned Order must not be null");
        List<ItemDTO> cartItems = orderService.checkCartContent(sessionToken).getData();
        assertEquals(0, cartItems.size(), "Cart should be empty after purchase");
        return created;
    }
    

    @Test
    public void successfulGuestLogin() {
        Response<String> guestToken = userService.enterToSystem();
        assertNotNull(guestToken.getData(), "Guest login failed: token is null.");
        Response<List<ItemDTO>> items = orderService.checkCartContent(guestToken.getData());
        assertNotNull(items.getData(), "Guest login failed: cart content is null.");
        assertTrue(items.getData().isEmpty(), "Guest login failed: cart is not empty.");
    }

    @Test
    public void successfulGuestExit() {
        Response<String> guestToken = userService.enterToSystem();
        assertNotNull(orderService.checkCartContent(guestToken.getData()).getData(), "Guest login failed: cart content is null.");
        assertTrue(orderService.checkCartContent(guestToken.getData()).getData().isEmpty(), "Guest login failed: cart is not empty.");
        Response<Void> res = userService.exitAsGuest(guestToken.getData());
        assertTrue(res.isOk());
        assertNull(orderService.checkCartContent(guestToken.getData()).getData(), "checkCartContent should return null after exit");
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
        Response<List<ItemDTO>> cart = orderService.checkCartContent(userToken.getData());
        assertNotNull(cart.getData(), "Cart should not be null for registered user");
        assertTrue(cart.getData().isEmpty(), "Cart should remain empty after registration and login");
    }

    @Test
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
        Response<List<ItemDTO>> cart = orderService.checkCartContent(userToken.getData());
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
    public void LogoutTest()
    {
        Response<String> guestToken = userService.enterToSystem();
        Response<Void> res = userService.registerUser(guestToken.getData(), "user123", "password", LocalDate.now().minusYears(20));
        assertTrue(res.isOk());
        Response<String> userToken = userService.loginUser(guestToken.getData(), "user123", "password");

        assertTrue(userToken.isOk());
        assertNotNull(userToken.getData(), "loginUser should return a valid token after registration");
        Response<String> logoutRespUser = userService.logoutRegistered(userToken.getData());
        assertTrue(logoutRespUser.isOk(), "Logout should succeed");
        Response<Void> logoutRespGuest = userService.exitAsGuest(logoutRespUser.getData());
        assertTrue(logoutRespGuest.isOk(), "Logout should succeed");
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

    @Test
    public void getShopsAndItems() {
        //  1) Owner setup 
        // Owner enters as guest
        String ownerToken = generateloginAsRegistered("Owner", "Pwd0");
        ShopDTO shop = generateShopAndItems(ownerToken);
        
        Response<List<ShopDTO>> shops = shopService.showAllShops();
        assertNotNull(shops.getData(), "showAllShops should not return null");
        assertEquals(1, shops.getData().size());
        assertEquals(3, shops.getData().get(0).getItems().size());
    }

    @Test
    public void searchItemsWithFilters() {
        // 1) Owner enters & registers
        Response<String> guestResp = userService.enterToSystem();
        assertTrue(guestResp.isOk(), "enterToSystem should succeed");
        String guestToken = guestResp.getData();

        Response<Void> regResp = userService.registerUser(
            guestToken, "owner", "pwdO", LocalDate.now().minusYears(30)
        );
        assertTrue(regResp.isOk(), "Owner registration should succeed");

        // 2) Owner logs in
        Response<String> loginResp = userService.loginUser(
            guestToken, "owner", "pwdO"
        );
        assertTrue(loginResp.isOk(), "Owner login should succeed");
        String ownerToken = loginResp.getData();

        // 3) Owner creates a shop
        Response<ShopDTO> createResp = shopService.createShop(
            ownerToken, "MyShop", "A shop for tests"
        );
        assertTrue(createResp.isOk(), "createShop should succeed");
        ShopDTO shop = createResp.getData();

        // 4) Owner adds three items
        Response<ItemDTO> addA = shopService.addItemToShop(
            ownerToken, shop.getId(), "Apple", Category.FOOD,  1.00, "fresh apple"
        );
        Response<ItemDTO> addB = shopService.addItemToShop(
            ownerToken, shop.getId(), "Banana", Category.FOOD, 0.50, "ripe banana"
        );
        Response<ItemDTO> addL = shopService.addItemToShop(
            ownerToken, shop.getId(), "Laptop", Category.ELECTRONICS, 999.99, "new laptop"
        );
        assertTrue(addA.isOk(), "Adding Apple should succeed");
        assertTrue(addB.isOk(), "Adding Banana should succeed");
        assertTrue(addL.isOk(), "Adding Laptop should succeed");

        // 5) (Optional) Retrieve them if you need IDs or to verify all three exist
        Response<List<ItemDTO>> allResp = shopService.showShopItems(shop.getId());
        assertTrue(allResp.isOk(), "showShopItems should succeed");
        List<ItemDTO> allItems = allResp.getData();
        assertEquals(3, allItems.size(), "Shop should now contain 3 items");

        // 6) Build a composite filter:
        //    - name contains "a" (so Apple, Banana, Laptop)
        //    - category = FOOD     (so Apple, Banana)
        //    - price between 0.6 and 2.0 (so only Apple)
        HashMap<String,String> filters = new HashMap<>();
        filters.put("name",      "a");
        filters.put("category",  "FOOD");
        filters.put("minPrice",  "0.6");
        filters.put("maxPrice",  "2");
        // we'll leave minRating and shopRating at zero,
        // so they don't filter anything extra

        // 7) Call the service
        Response<List<ItemDTO>> filteredResp = shopService.filterItemsAllShops(filters);
        assertTrue(filteredResp.isOk(), "filterItemsAllShops should succeed");

        List<ItemDTO> result = filteredResp.getData();
        assertNotNull(result, "Filtered list must not be null");

        // 8) Verify that only "Apple" remains
        assertEquals(1, result.size(), "Exactly one item should survive all filters");
        assertEquals("Apple", result.get(0).getName(), "That one item should be Apple");
    }

    @Test
    public void searchItemsWithoutFilters() {
        // Owner creates a shop with 3 items
        String ownerToken = generateloginAsRegistered("Owner", "Pwd0");
        generateShopAndItems(ownerToken);

        // Guest enters the system
        Response<String> guestResp = userService.enterToSystem();
        assertTrue(guestResp.isOk(), "Guest enterToSystem should succeed");
        String guestToken = guestResp.getData();

        // 1) Search without any filters
        HashMap<String,String> emptyFilters = new HashMap<>();
        Response<List<ItemDTO>> searchResp = shopService.filterItemsAllShops(emptyFilters);
        assertTrue(searchResp.isOk(), "filterItemsAllShops should succeed");
        List<ItemDTO> items = searchResp.getData();
        assertEquals(3, items.size(), "Should return all 3 available items");
    }

    @Test
    public void emptySearchResults() {
        // Owner creates a shop with 3 items
        String ownerToken = generateloginAsRegistered("Owner", "Pwd0");
        generateShopAndItems(ownerToken);

        // Guest enters
        userService.enterToSystem();

        // 2) Search with a name that matches nothing
        HashMap<String,String> filters = new HashMap<>();
        filters.put("name", "NoSuchItem");
        Response<List<ItemDTO>> searchResp = shopService.filterItemsAllShops(filters);
        assertTrue(searchResp.isOk(), "filterItemsAllShops should succeed even if empty");
        assertTrue(searchResp.getData().isEmpty(), "No items should be found");
    }


    @Test
    public void searchItemsInSpecificShop() {
        // Owner creates a shop with 3 items
        String ownerToken = generateloginAsRegistered("Owner", "Pwd0");
        ShopDTO shop = generateShopAndItems(ownerToken);

        // Guest enters
        userService.enterToSystem();

        // 4) Search in that shop for bananas priced <=0.50
        HashMap<String,String> filters = new HashMap<>();
        filters.put("name",     "Banana");
        filters.put("category", Category.FOOD.name());
        filters.put("minPrice","0");
        filters.put("maxPrice","0.50");

        Response<List<ItemDTO>> resp = shopService.filterItemsInShop(shop.getId(), filters);
        assertTrue(resp.isOk(), "filterItemsInShop should succeed");
        List<ItemDTO> results = resp.getData();
        assertEquals(1, results.size(), "Exactly one banana at price <= 0.50 should match");
        assertEquals("Banana", results.get(0).getName());
    }

    @Test
    public void shopNotFound() {
        // Guest enters
        userService.enterToSystem();

        // 5) Use a non-existent shop ID
        int missingShopId = 9999;
        HashMap<String,String> filters = new HashMap<>();
        Response<List<ItemDTO>> resp = shopService.filterItemsInShop(missingShopId, filters);

        // Right now this blows up with a NullPointerException. You need to catch that
        // inside filterItemsInShop and return Response.error("Shop not found");
        assertFalse(resp.isOk());
    }

    @Test
    public void addItemToBasketTest() {
        //  1) Owner setup 
        // Owner enters as guest
        String ownerToken = generateloginAsRegistered("Owner", "Pwd0");
        ShopDTO shop = generateShopAndItems(ownerToken);
        
        //  2) Buyer setup 
        // Buyer enters as guest
        Response<String> buyerGuestResp = userService.enterToSystem();
        assertTrue(buyerGuestResp.isOk(), "Buyer enterToSystem should succeed");
        String buyerGuestToken = buyerGuestResp.getData();
        assertNotNull(buyerGuestToken, "Buyer guest token must not be null");

        //  3) Buyer shopping & checkout 
        // Buyer views the shop's items
        Response<List<ItemDTO>> viewResp = shopService.showShopItems(shop.getId());
        assertTrue(viewResp.isOk(), "showShopItems should succeed");
        List<ItemDTO> shopItems = viewResp.getData();
        assertNotNull(shopItems, "shopItems list must not be null");
        assertEquals(3, shopItems.size(), "Shop should contain exactly one item");

        HashMap<Integer, HashMap<Integer, Integer>> itemsMap = new HashMap<>();
        HashMap<Integer, Integer> itemMap = new HashMap<>();
        itemMap.put(shopItems.get(0).getItemID(), 1);
        itemsMap.put(shop.getId(), itemMap);
        // Buyer adds that item to cart
        Response<Void> addToCart = orderService.addItemsToCart(
            buyerGuestToken,
            itemsMap
        );
        assertTrue(addToCart.isOk(), "addItemsToCart should succeed");

        List<ItemDTO> cartItems = orderService.checkCartContent(buyerGuestToken).getData();
        assertEquals(1, cartItems.size(), "Cart should contain exactly one item");
    }

    @Test
    public void checkCartContentTest() {
        String ownerToken = generateloginAsRegistered("Owner", "Pwd0");
        ShopDTO shop = generateShopAndItems(ownerToken);
        // grab the first item from the shop
        List<ItemDTO> items = shopService.showShopItems(shop.getId()).getData();
        assertEquals(3, items.size(), "Shop should have 3 items");

        //  2) Buyer setup 
        // Buyer enters as guest
        Response<String> buyerGuestResp = userService.enterToSystem();
        assertTrue(buyerGuestResp.isOk(), "Buyer enterToSystem should succeed");
        String buyerGuestToken = buyerGuestResp.getData();
        assertNotNull(buyerGuestToken, "Buyer guest token must not be null");

        // Buyer adds one item to cart
        List<ItemDTO> itemsToAdd = new ArrayList<>();
        itemsToAdd.add(items.get(0));
        
        HashMap<Integer, HashMap<Integer, Integer>> itemsMap = new HashMap<>();
        HashMap<Integer, Integer> itemMap = new HashMap<>();
        itemMap.put(itemsToAdd.get(0).getItemID(), 1);
        itemsMap.put(shop.getId(), itemMap);
        Response<Void> addToCart = orderService.addItemsToCart(
            buyerGuestToken,
            itemsMap
        );
        assertTrue(addToCart.isOk(), "addItemsToCart should succeed");

        // verify cart contents
        List<ItemDTO> cartItems = orderService.checkCartContent(buyerGuestToken).getData();
        assertEquals(1, cartItems.size(), "Cart should contain exactly one item");
    }

    @Test
    public void successfulBuyCartContentTest() {
        when(payment.validatePaymentDetails()).thenReturn(true);
        when(payment.processPayment(1.0)).thenReturn(true);
        when(shipment.validateShipmentDetails()).thenReturn(true);
        when(shipment.processShipment(0.1)).thenReturn(true);

        // 1) Owner setup
        String ownerToken = generateloginAsRegistered("Owner", "Pwd0");
        ShopDTO shop = generateShopAndItems(ownerToken);
        List<ItemDTO> shopItems = shopService.showShopItems(shop.getId()).getData();
        ItemDTO toBuy = shopItems.get(0);

        // 2) Buyer setup
        String buyerToken = generateloginAsRegistered("buyer", "Pwd0");

        HashMap<Integer, HashMap<Integer, Integer>> itemsMap = new HashMap<>();
        HashMap<Integer, Integer> itemMap = new HashMap<>();
        itemMap.put(List.of(toBuy).get(0).getItemID(), 1);
        itemsMap.put(shop.getId(), itemMap);

        // 3) Add to cart
        Response<Void> addResp = orderService.addItemsToCart(
            buyerToken,
            itemsMap
        );
        assertTrue(addResp.isOk(), "addItemsToCart should succeed");

        // 4) Purchase (pass dummy payment/shipment; replace with valid data if needed)
        Order created = successfulBuyCartContent(buyerToken);

        // verify stock was decremented by 1
        Response<ShopDTO> updatedShopResp = shopService.getShopInfo(ownerToken, shop.getId());
        assertTrue(updatedShopResp.isOk(), "getShopInfo should succeed after cart operation");
        ShopDTO updatedShop = updatedShopResp.getData();
        int originalQty = shop.getItems().get(shopItems.get(0).getItemID()).getQuantity();
        int newQty      = updatedShop.getItems().get(shopItems.get(0).getItemID()).getQuantity();
        assertEquals(originalQty - 1, newQty, "Stock should decrease by 1 when added to cart");

        // 5) Verify via service (no direct repo access)
        Response<List<Order>> historyResp = orderService.viewPersonalOrderHistory(buyerToken);
        assertTrue(historyResp.isOk(), "viewPersonalOrderHistory should succeed");
        List<Order> history = historyResp.getData();
        assertEquals(1, history.size(), "Exactly one order should exist for this buyer");

        Order recorded = history.get(0);
        assertEquals(created.getId(), recorded.getId(), "Order IDs should match");
        assertEquals(1, recorded.getItems().size(), "Order must contain exactly one item");
        assertEquals(
            toBuy.getName(),
            recorded.getItems().get(0).getName(),
            "Purchased item's name must match what was added"
        );

        verify(payment, atLeastOnce()).validatePaymentDetails(); // validatePaymentDetails should be called at least once
        verify(payment).processPayment(1.0);
        verify(shipment, atLeastOnce()).validateShipmentDetails(); // validateShipmentDetails should be called at least once
        verify(shipment).processShipment(0.1);
    }

    @Test
    public void BuyCartContentTest_paymentFails() {
        when(payment.validatePaymentDetails()).thenReturn(false);
        when(payment.processPayment(1.0)).thenReturn(false);

        // 1) Owner setup
        String ownerToken = generateloginAsRegistered("Owner", "Pwd0");
        ShopDTO shop = generateShopAndItems(ownerToken);
        List<ItemDTO> shopItems = shopService.showShopItems(shop.getId()).getData();
        ItemDTO toBuy = shopItems.get(0);

        // 2) Buyer setup
        String buyerToken = generateloginAsRegistered("buyer", "Pwd0");

        HashMap<Integer, HashMap<Integer, Integer>> itemsMap = new HashMap<>();
        HashMap<Integer, Integer> itemMap = new HashMap<>();
        itemMap.put(List.of(toBuy).get(0).getItemID(), 1);
        itemsMap.put(shop.getId(), itemMap);

        // 3) Add to cart
        Response<Void> addResp = orderService.addItemsToCart(
            buyerToken,
            itemsMap
        );
        assertTrue(addResp.isOk(), "addItemsToCart should succeed");

        // 4) Purchase (pass dummy payment/shipment; replace with valid data if needed)
        Response<Order> orderResp = orderService.buyCartContent(buyerToken);
        assertFalse(orderResp.isOk(), "buyCartContent should fail due to payment validation failure");

        verify(payment).validatePaymentDetails();
    }

    @Test
    public void BuyCartContentTest_shipmentFails() {
        when(payment.validatePaymentDetails()).thenReturn(true);
        when(payment.processPayment(1.0)).thenReturn(true);
        when(shipment.validateShipmentDetails()).thenReturn(false);
        when(shipment.processShipment(0.1)).thenReturn(false);

        // 1) Owner setup
        String ownerToken = generateloginAsRegistered("Owner", "Pwd0");
        ShopDTO shop = generateShopAndItems(ownerToken);
        List<ItemDTO> shopItems = shopService.showShopItems(shop.getId()).getData();
        ItemDTO toBuy = shopItems.get(0);

        // 2) Buyer setup
        String buyerToken = generateloginAsRegistered("buyer", "Pwd0");

        HashMap<Integer, HashMap<Integer, Integer>> itemsMap = new HashMap<>();
        HashMap<Integer, Integer> itemMap = new HashMap<>();
        itemMap.put(List.of(toBuy).get(0).getItemID(), 1);
        itemsMap.put(shop.getId(), itemMap);

        // 3) Add to cart
        Response<Void> addResp = orderService.addItemsToCart(
            buyerToken,
            itemsMap
        );
        assertTrue(addResp.isOk(), "addItemsToCart should succeed");

        // 4) Purchase (pass dummy payment/shipment; replace with valid data if needed)
        Response<Order> orderResp = orderService.buyCartContent(buyerToken);
        assertFalse(orderResp.isOk(), "buyCartContent should fail due to shipment validation failure");

        verify(shipment).validateShipmentDetails();
    }

    @Test
    public void changeCartContentTest() {
        // 1) Owner setup
        String ownerToken = generateloginAsRegistered("Owner", "Pwd0");
        ShopDTO shop = generateShopAndItems(ownerToken);
        List<ItemDTO> shopItems = shopService.showShopItems(shop.getId()).getData();

        // 2) Buyer setup: enter, register, login
        Response<String> guestResp = userService.enterToSystem();
        assertTrue(guestResp.isOk(), "Buyer enterToSystem should succeed");
        String guestToken = guestResp.getData();

        HashMap<Integer, HashMap<Integer, Integer>> itemsMap = new HashMap<>();
        HashMap<Integer, Integer> itemMap = new HashMap<>();
        itemMap.put(shopItems.get(0).getItemID(), 1);
        itemMap.put(shopItems.get(1).getItemID(), 1);
        itemsMap.put(shop.getId(), itemMap);
        // 3) Add two different items to cart
        Response<Void> addResp = orderService.addItemsToCart(
            guestToken,
            itemsMap
        );
        assertTrue(addResp.isOk(), "addItemsToCart should succeed");

        List<ItemDTO> cartAfterAdd = orderService.checkCartContent(guestToken).getData();
        assertEquals(2, cartAfterAdd.size(), "Cart should contain exactly two items");

        HashMap<Integer, List<Integer>> itemMap1 = new HashMap<>();
        itemMap1.put(shop.getId(), List.of(shopItems.get(0).getItemID()));

        // 4) Remove the first item
        Response<Void> removeResp = orderService.removeItemsFromCart(
            guestToken,
            itemMap1
        );
        assertTrue(removeResp.isOk(), "removeItemsFromCart should succeed");

        // 5) Re-fetch and verify
        List<ItemDTO> cartAfterRemove = orderService.checkCartContent(guestToken).getData();
        assertEquals(1, cartAfterRemove.size(), "Cart should contain exactly one item after removal");
        assertEquals(
            shopItems.get(1).getItemID(),
            cartAfterRemove.get(0).getItemID(),
            "Remaining item should be the second one originally added"
        );
    }

    @Test
    public void openShopTest()
    {
        Response<String> ownerGuestResp = userService.enterToSystem();
        assertTrue(ownerGuestResp.isOk(), "Owner enterToSystem should succeed");
        String ownerGuestToken = ownerGuestResp.getData();
        assertNotNull(ownerGuestToken, "Owner guest token must not be null");

        // Owner registers
        Response<Void> ownerReg = userService.registerUser(
            ownerGuestToken, "owner", "pwdO", LocalDate.now().minusYears(30)
        );
        assertTrue(ownerReg.isOk(), "Owner registration should succeed");

        // Owner logs in
        Response<String> ownerLogin = userService.loginUser(
            ownerGuestToken, "owner", "pwdO"
        );
        assertTrue(ownerLogin.isOk(), "Owner login should succeed");
        String ownerToken = ownerLogin.getData();
        assertNotNull(ownerToken, "Owner token must not be null");

        // Owner creates shop
        Response<ShopDTO> shopResp = shopService.createShop(
            ownerToken, "MyShop", "A shop for tests"
        );
        assertTrue(shopResp.isOk(), "Shop creation should succeed");
        ShopDTO shop = shopResp.getData();
        assertNotNull(shop, "Returned ShopDTO must not be null");
    }


    @Test
    public void rateShopTest() {
        when(payment.validatePaymentDetails()).thenReturn(true);
        when(payment.processPayment(1.0)).thenReturn(true);
        when(shipment.validateShipmentDetails()).thenReturn(true);
        when(shipment.processShipment(0.1)).thenReturn(true);
        // 1) Owner setup
        String ownerToken = generateloginAsRegistered("Owner", "Pwd0");
        ShopDTO shop = generateShopAndItems(ownerToken);

        // 2) Buyer setup: enter -> register -> login
        String buyerToken = generateloginAsRegistered("buyer", "Pwd0");

        // 3) Buyer purchases one item (necessary to enable rating)
        List<ItemDTO> shopItems = shopService.showShopItems(shop.getId()).getData();
        assertFalse(shopItems.isEmpty(), "Shop must have at least one item");
        ItemDTO toBuy = shopItems.get(0);

        HashMap<Integer, HashMap<Integer, Integer>> itemsMap = new HashMap<>();
        HashMap<Integer, Integer> itemMap = new HashMap<>();
        itemMap.put(toBuy.getItemID(), 1);
        itemsMap.put(shop.getId(), itemMap);
        Response<Void> addToCart = orderService.addItemsToCart(
            buyerToken,
            itemsMap
        );
        assertTrue(addToCart.isOk(), "addItemsToCart should succeed");

        // dummy payment & shipment (fill in real fields if needed)
        Order created = successfulBuyCartContent(buyerToken);

        // 4) Rate the shop
        int ratingScore = 5;
        Response<Void> rateResp = shopService.rateShop(buyerToken, shop.getId(), ratingScore);
        assertTrue(rateResp.isOk(), "rateShop should succeed");

        // 5) Fetch shop info and verify its rating
        Response<ShopDTO> infoResp = shopService.getShopInfo(buyerToken, shop.getId());
        assertTrue(infoResp.isOk(), "getShopInfo should succeed after rating");
        ShopDTO ratedShop = infoResp.getData();
        assertEquals(ratingScore, ratedShop.getRating(), "Shop rating should match the score given");
    }


    @Test
    public void sendMessageToShopTest() {
        // 1) Owner creates a shop with items
        String ownerToken = generateloginAsRegistered("Owner", "Pwd");
        ShopDTO shopDto = generateShopAndItems(ownerToken);
        int shopId = shopDto.getId();

        // 2) A second user (sender) logs in
        String senderToken = generateloginAsRegistered("Sender", "Pwd");

        // 3) Send a message to the shop
        String title = "Problem with the name of the shop";
        String content = "Hello sir, I have a problem with the name of the shop. "
                    + "Can you please change it to something less racist?";

        Response<Void> res = shopService.sendMessage(senderToken, shopId, title, content);
        assertTrue(res.isOk(), "sendMessageToShop should succeed");   

        HashMap<Integer, IMessage> messages = shopService.getInbox(shopId).getData();
        boolean contains = false;
        for (IMessage msg : messages.values()) {
            if (msg.getTitle().equals(title) && msg.getContent().equals(content))
                contains = true;    
        }
        assertTrue(contains);
    }

    @Test
    public void itemUnavailableForPurchaseTest() {
        // --- Setup -------------------------------------------------------------
        // 1) Prepare payment/shipment mocks
        when(payment.validatePaymentDetails()).thenReturn(true);
        when(payment.processPayment(anyDouble())).thenReturn(true);
        when(shipment.validateShipmentDetails()).thenReturn(true);
        when(shipment.processShipment(anyDouble())).thenReturn(true);

        // 2) Owner creates shop with 3 items
        String ownerToken = generateloginAsRegistered("Owner", "Pwd0");
        ShopDTO shop = generateShopAndItems(ownerToken);
        int shopId = shop.getId();

        // 3) Set the quantity of the first item to 0 (unavailable)
        ItemDTO firstItem = shop.getItems().get(0);
        Response<Void> setQtyResp = shopService.changeItemQuantityInShop(
            ownerToken, shopId, firstItem.getItemID(), 0
        );
        assertTrue(setQtyResp.isOk(), "Setting quantity to 0 should succeed");

        // Capture the stock before any buyer interactions
        int stockBefore = shopService
            .getShopInfo(ownerToken, shopId)
            .getData()
            .getItems()
            .get(firstItem.getItemID())
            .getQuantity();
        assertEquals(0, stockBefore, "Stock should now be zero");

        // Buyer enters the system
        String buyerToken = generateloginAsRegistered("Buyer", "Pwd1");

        // --- Action ------------------------------------------------------------
        // Buyer attempts to add all three items (including the unavailable one) to cart
        List<ItemDTO> availableItems = shopService.showShopItems(shopId).getData();
        HashMap<Integer, HashMap<Integer, Integer>> itemsMap = new HashMap<>();
        HashMap<Integer, Integer> itemMap = new HashMap<>();
        for (ItemDTO it : availableItems) {
            itemMap.put(it.getItemID(), 1);
        }
        itemsMap.put(shopId, itemMap);

        Response<Void> addToCartResp = orderService.addItemsToCart(buyerToken, itemsMap);

        // Buyer then attempts to checkout
        Response<Order> checkoutResp = orderService.buyCartContent(buyerToken);

        // --- Verification -------------------------------------------------------
        // 1) Adding to cart should fail because one item has zero stock
        assertFalse(addToCartResp.isOk(),
            "addItemsToCart must fail when any requested item is out of stock");

        // 2) Checkout should also fail
        assertFalse(checkoutResp.isOk(),
            "buyCartContent must fail when cart contains unavailable items");

        // 3) Cart should remain empty after the failed attempts
        List<ItemDTO> cartAfter = orderService.checkCartContent(buyerToken).getData();
        assertNotNull(cartAfter, "checkCartContent must return a (possibly empty) list");
        assertTrue(cartAfter.isEmpty(), "Cart must be empty after a failed addition/checkout");

        // 4) Stock must be unchanged (still zero for the first item)
        int stockAfter = shopService
            .getShopInfo(ownerToken, shopId)
            .getData()
            .getItems()
            .get(firstItem.getItemID())
            .getQuantity();
        assertEquals(stockBefore, stockAfter,
            "Stock of the unavailable item must remain unchanged");
    }

    @Test
    public void concurrentSingleStockCartPurchaseTest() {
        // --- Arrange mocks for payments and shipments ---
        when(payment.validatePaymentDetails()).thenReturn(true);
        when(payment.processPayment(anyDouble())).thenReturn(true);
        when(shipment.validateShipmentDetails()).thenReturn(true);
        when(shipment.processShipment(anyDouble())).thenReturn(true);

        // --- 1) Owner creates shop with a single‐unit item ---
        String ownerToken = generateloginAsRegistered("owner", "pwdO");
        ShopDTO shop = generateShopAndItems(ownerToken);
        int shopId = shop.getId();

        // Pick the first item and force its stock to exactly 1
        ItemDTO item = shop.getItems().get(0);
        assertTrue(
            shopService.changeItemQuantityInShop(ownerToken, shopId, item.getItemID(), 1).isOk(),
            "Setting item stock to 1 should succeed"
        );

        // --- 2) Two buyers each add that same single unit to their carts ---
        String buyer1 = generateloginAsRegistered("buyer1", "pwd1");
        String buyer2 = generateloginAsRegistered("buyer2", "pwd2");

        HashMap<Integer,HashMap<Integer,Integer>> itemsMap = new HashMap<>();
        itemsMap.put(shopId, new HashMap<>(Map.of(item.getItemID(), 1)));

        // Buyer1 adds to cart
        Response<Void> add1 = orderService.addItemsToCart(buyer1, itemsMap);
        assertTrue(add1.isOk(), "Buyer1 should add the item to cart");

        // Buyer2 also adds to cart
        Response<Void> add2 = orderService.addItemsToCart(buyer2, itemsMap);
        assertTrue(add2.isOk(), "Buyer2 should add the item to cart");

        // --- 3) Buyer1 completes purchase successfully ---
        Response<Order> buy1 = orderService.buyCartContent(buyer1);
        assertTrue(buy1.isOk(), "Buyer1 purchase should succeed");

        // --- 4) Buyer2 attempt to purchase must fail (out of stock) ---
        Response<Order> buy2 = orderService.buyCartContent(buyer2);
        assertFalse(buy2.isOk(), "Buyer2 purchase should fail due to no stock");

        // --- 5) Verify final stock is zero and buyer2’s cart is empty ---
        int finalStock = shopService
            .getShopInfo(ownerToken, shopId)
            .getData()
            .getItems()
            .get(item.getItemID())
            .getQuantity();
        assertEquals(0, finalStock, "Final stock should be zero after Buyer1 purchase");

        List<ItemDTO> cart1 = orderService.checkCartContent(buyer1).getData();
        assertNotNull(cart1, "Buyer2 cart query should return a list");
        assertTrue(cart1.isEmpty(), "Buyer2 cart should be empty after failed purchase");

        List<ItemDTO> cart2 = orderService.checkCartContent(buyer2).getData();
        assertNotNull(cart2, "Buyer2 cart query should return a list");
        // Here buyer2's cart should not be empty even if the purchase failed becasue we don't want to remove all the cart (and therefore not the only item there even though the quantity is 0)
        assertFalse(cart2.isEmpty(), "Buyer2 cart should be empty after failed purchase");
    }


    // @Test
    // public void duplicateShopNameTest()
    // {
    //     String ownerToken = generateloginAsRegistered("Owner", "Pwd0");
    //     ShopDTO shop = generateShopAndItems(ownerToken);
        
    //     String userToken = generateloginAsRegistered("Buyer", "Pwd0");
    //     Response<ShopDTO> shopResp = shopService.createShop(
    //         userToken, "MyShop", "A shop for tests"
    //     );

    //     assertFalse(shopResp.isOk(), "Shop creation should fail as the name is already taken");
        
    // }

    @Test
    public void invalidShopCreationTest()
    {
        Response<String> guestResp = userService.enterToSystem();
        assertTrue(guestResp.isOk(), "enterToSystem should succeed");
        String guestToken = guestResp.getData();
        assertNotNull(guestToken, "Guest token must not be null");
        
        Response<ShopDTO> createShopResp = shopService.createShop(guestResp.getData(), "MyShop", "desc");
        assertFalse(createShopResp.isOk(), "Shop creation should succeed");
    }

    @Test
    public void rateShopNotLoggedInTest()
    {
        when(payment.validatePaymentDetails()).thenReturn(true);
        when(payment.processPayment(1.0)).thenReturn(true);
        when(shipment.validateShipmentDetails()).thenReturn(true);
        when(shipment.processShipment(0.1)).thenReturn(true);
        // 1) Owner creates a shop with items
        String ownerToken = generateloginAsRegistered("Owner", "Pwd0");
        ShopDTO shopDto = generateShopAndItems(ownerToken);
        
        String guestToken = userService.enterToSystem().getData();
        List<ItemDTO> items = shopService.showShopItems(shopDto.getId()).getData();
        HashMap<Integer, HashMap<Integer, Integer>> itemsMap = new HashMap<>();
        HashMap<Integer, Integer> itemMap = new HashMap<>();
        itemMap.put(items.get(0).getItemID(), 1);
        itemMap.put(items.get(1).getItemID(), 1);
        itemsMap.put(shopDto.getId(), itemMap);

        orderService.addItemsToCart(guestToken, itemsMap);

        successfulBuyCartContent(guestToken);
        
        Response<Void> res =shopService.rateShop(guestToken, shopDto.getId(), 5);
        assertFalse(res.isOk(), "Rate shop should fail when not logged in");
    }

    @Test
    public void addItemToShop() 
    {
        String ownerToken = generateloginAsRegistered("Owner", "Pwd0");

        Response<ShopDTO> shopResp = shopService.createShop(
            ownerToken, 
            "MyShop", 
            "A shop for tests"
        );
        assertTrue(shopResp.isOk(), "Shop creation should succeed");
        ShopDTO shop = shopResp.getData();
        assertNotNull(shop, "Returned ShopDTO must not be null");
        int shopId = shop.getId();

        // 2) Owner adds three items
        Response<ItemDTO> addA = shopService.addItemToShop(
            ownerToken, shopId,
            "Apple", Category.FOOD, 1.00, "fresh apple"
        );

        Response<ShopDTO> infoResp = shopService.getShopInfo(ownerToken, shopId);
        assertTrue(infoResp.isOk(), "getShopInfo should succeed");

        assertTrue(addA.isOk(), "Adding Apple should succeed");
        assertEquals("Apple", addA.getData().getName(), "Item name should be 'Apple'");
        assertEquals(Category.FOOD, addA.getData().getCategory(), "Item category should be FOOD");
        assertEquals(1.00, addA.getData().getPrice(), "Item price should be 1.00");
        assertEquals("fresh apple", addA.getData().getDescription());
        assertEquals(1, infoResp.getData().getItems().size(), "Shop should contain exactly one item after adding Apple");
        
    }

    @Test
    public void addItemToANonExistentShop() 
    {
        String ownerToken = generateloginAsRegistered("Owner", "Pwd0");

        // 2) Owner adds three items
        Response<ItemDTO> addA = shopService.addItemToShop(
            ownerToken, 0,
            "Apple", Category.FOOD, 1.00, "fresh apple"
        );

        assertFalse(addA.isOk(), "Adding Apple should fail");
    }

    @Test
    public void addItemToShopAsNonOwner()
    {
        String ownerToken = generateloginAsRegistered("Owner", "Pwd0");
        ShopDTO shopDto = generateShopAndItems(ownerToken);

        String userToken = generateloginAsRegistered("Buyer", "Pwd0");
        Response<ItemDTO> addA = shopService.addItemToShop(
            userToken, shopDto.getId(),
            "Apple", Category.FOOD, 1.00, "fresh apple"
        );
        assertFalse(addA.isOk(), "Adding Apple should fail as the user is not the owner");
    }

    @Test
    public void addDuplicateItemToShop()
    {
        String ownerToken = generateloginAsRegistered("Owner", "Pwd0");
        ShopDTO shopDto = generateShopAndItems(ownerToken);

        Response<ItemDTO> addA = shopService.addItemToShop(
            ownerToken, shopDto.getId(),
            "Apple", Category.FOOD, 1.00, "fresh apple"
        );
        assertFalse(addA.isOk(), "Adding duplicate item should fail");
    }

    @Test
    public void removeItemsFromShop()
    {
        String ownerToken = generateloginAsRegistered("Owner", "Pwd0");
        ShopDTO shopDto = generateShopAndItems(ownerToken);
        assertEquals(3, shopDto.getItems().size(), "Shop should contain exactly three items after removal");

        // 2) Owner removes an item
        Response<Void> removeResp = shopService.removeItemFromShop(
            ownerToken, shopDto.getId(), shopDto.getItems().get(0).getItemID()
        );
        assertTrue(removeResp.isOk(), "removeItemFromShop should succeed");

        // 3) Verify the item is removed
        Response<List<ItemDTO>> itemsResp = shopService.showShopItems(shopDto.getId());
        assertTrue(itemsResp.isOk(), "showShopItems should succeed");
        List<ItemDTO> items = itemsResp.getData();
        assertEquals(2, items.size(), "Shop should contain exactly two items after removal");
    }

    @Test
    public void removeItemFromShopAsNonOwner()
    {
        String ownerToken = generateloginAsRegistered("Owner", "Pwd0");
        ShopDTO shopDto = generateShopAndItems(ownerToken);

        String userToken = generateloginAsRegistered("Buyer", "Pwd0");
        Response<Void> removeResp = shopService.removeItemFromShop(
            userToken, shopDto.getId(), shopDto.getItems().get(0).getItemID()
        );
        assertFalse(removeResp.isOk(), "removeItemFromShop should fail as the user is not the owner");
    }

    @Test
    public void removeNonExistentItemFromShop()
    {
        String ownerToken = generateloginAsRegistered("Owner", "Pwd0");
        ShopDTO shopDto = generateShopAndItems(ownerToken);

        Response<Void> removeResp = shopService.removeItemFromShop(
            ownerToken, shopDto.getId(), 456 // Non-existent item ID
        );
        assertFalse(removeResp.isOk(), "removeItemFromShop should fail as the item does not exist");
    }

    @Test
    public void editItemInShop()
    {
        String ownerToken = generateloginAsRegistered("Owner", "Pwd0");
        ShopDTO shopDto = generateShopAndItems(ownerToken);

        // 2) Owner edits an item
        ItemDTO itemToEdit = shopDto.getItems().get(0);
        Response<Void> editResp = shopService.changeItemDescriptionInShop(
            ownerToken, shopDto.getId(), itemToEdit.getItemID(), "New description"
        );
        assertTrue(editResp.isOk(), "editItemInShop should succeed");

        // 3) Verify the item is edited
        Response<List<ItemDTO>> itemsResp = shopService.showShopItems(shopDto.getId());
        assertTrue(itemsResp.isOk(), "showShopItems should succeed");
        List<ItemDTO> items = itemsResp.getData();
        assertEquals(3, items.size(), "Shop should contain exactly three items after editing");
        assertEquals("New description", items.get(0).getDescription(), "Item description should be updated");
    }

    @Test
    public void editItemInShopAsNonOwner()
    {
        String ownerToken = generateloginAsRegistered("Owner", "Pwd0");
        ShopDTO shopDto = generateShopAndItems(ownerToken);

        String userToken = generateloginAsRegistered("Buyer", "Pwd0");

        // 2) Owner edits an item
        ItemDTO itemToEdit = shopDto.getItems().get(0);
        Response<Void> editResp = shopService.changeItemDescriptionInShop(
            userToken, shopDto.getId(), itemToEdit.getItemID(), "New description"
        );
        assertFalse(editResp.isOk(), "editItemInShop should fail as the user is not the owner");
    }

    @Test
    public void invalidEditItemInShop()
    {
        String ownerToken = generateloginAsRegistered("Owner", "Pwd0");
        ShopDTO shopDto = generateShopAndItems(ownerToken);

        // 2) Owner edits an item
        ItemDTO itemToEdit = shopDto.getItems().get(0);
        Response<Void> editResp = shopService.changeItemPriceInShop(
            ownerToken, shopDto.getId(), itemToEdit.getItemID(), -100.00
        );
        assertFalse(editResp.isOk(), "editItemInShop should succeed");

        // 3) Verify the item is edited
        Response<List<ItemDTO>> itemsResp = shopService.showShopItems(shopDto.getId());
        assertTrue(itemsResp.isOk(), "showShopItems should succeed");
        List<ItemDTO> items = itemsResp.getData();
        assertEquals(3, items.size(), "Shop should contain exactly three items after editing");
    }

    // @Test
    // public void addPurchaseDiscountTypeSuccessTest() {
    //     // 1) Owner setup
    //     String ownerToken = generateloginAsRegistered("Owner", "Pwd0");
    //     ShopDTO shop = generateShopAndItems(ownerToken);

    //     // 2) Update purchase type
    //     Response<Void> updateResp = shopService.updatePurchaseType(ownerToken, shop.getId(), PurchaseType.AUCTION.name());
    //     assertTrue(updateResp.isOk(), "Adding purchase type should succeed");
    // }

    // @Test
    // public void addPurchaseDiscountTypeShopNotFoundTest() {
    //     // 1) User setup
    //     String userToken = generateloginAsRegistered("Owner", "Pwd0");

    //     // 2) Attempt to add purchase type to non-existing shop
    //     int nonExistingShopId = 9999;
    //     Response<Void> updateResp = shopService.updatePurchaseType(userToken, nonExistingShopId, PurchaseType.AUCTION.name());
    //     assertFalse(updateResp.isOk(), "Adding purchase type to non-existing shop should fail");
    // }

    // @Test
    // public void removePurchaseDiscountTypeSuccessTest() {
    //     // 1) Owner setup
    //     String ownerToken = generateloginAsRegistered("Owner", "Pwd0");
    //     ShopDTO shop = generateShopAndItems(ownerToken);

    //     // 2) Owner first adds purchase type
    //     Response<Void> addResp = shopService.updatePurchaseType(ownerToken, shop.getId(), PurchaseType.AUCTION.name());
    //     assertTrue(addResp.isOk(), "Adding purchase type should succeed");

    //     // 3) Then owner removes it by setting default or blank type (depends how you remove)
    //     Response<Void> removeResp = shopService.updatePurchaseType(ownerToken, shop.getId(), PurchaseType.BID.name());
    //     assertTrue(removeResp.isOk(), "Removing purchase type should succeed");
    // }

    // @Test
    // public void addPurchaseDiscountTypeUnauthorizedTest() {
    //     // 1) Setup: Owner1 creates shop
    //     String owner1Token = generateloginAsRegistered("Owner", "Pwd0");
    //     ShopDTO shop = generateShopAndItems(owner1Token);

    //     // 2) Buyer (other user)
    //     String buyerToken = generateloginAsRegistered("Buyer", "Pwd0");

    //     // 3) Buyer tries to update purchase type
    //     Response<Void> updateResp = shopService.updatePurchaseType(buyerToken, shop.getId(), "NEW_PURCHASE_TYPE");
    //     assertFalse(updateResp.isOk(), "Unauthorized user should not be able to add purchase type");
    // }

    // @Test
    // public void removePurchaseDiscountTypeNotFoundTest() {
    //     // 1) Owner setup
    //     String ownerToken = generateloginAsRegistered("Owner", "Pwd0");
    //     ShopDTO shop = generateShopAndItems(ownerToken);

    //     // 2) Owner tries to remove a type when none exists
    //     Response<Void> removeResp = shopService.updatePurchaseType(ownerToken, shop.getId(), "");
    //     assertFalse(removeResp.isOk(), "Removing non-existing purchase type should fail");
    // }

    // @Test
    // public void updateDiscountTypeSuccessTest() {
    //     // 1) Owner setup
    //     String ownerToken = generateloginAsRegistered("Owner", "Pwd0");
    //     ShopDTO shop = generateShopAndItems(ownerToken);

    //     // 2) Owner updates discount type
    //     Response<Void> updateResp = shopService.updateDiscountType(ownerToken, shop.getId(), "NEW_DISCOUNT_TYPE");
    //     assertTrue(updateResp.isOk(), "Updating discount type should succeed");
    // }

    @Test
    public void addShopManagerTest() {
        // 1) Owner setup
        String ownerToken = generateloginAsRegistered("Owner1", "Pwd0");
        ShopDTO shop = generateShopAndItems(ownerToken);

        // 2) Manager setup
        String managerGuestToken = userService.enterToSystem().getData();
        Response<Void> managerRegResp = userService.registerUser(
            managerGuestToken, "manager", "pwdM", LocalDate.now().minusYears(30)
        );
        assertTrue(managerRegResp.isOk(), "Manager registration should succeed");

        Response<String> managerLoginResp = userService.loginUser(
            managerGuestToken, "manager", "pwdM"
        );
        assertTrue(managerLoginResp.isOk(), "Manager login should succeed");
        String managerToken = managerLoginResp.getData(); // after login
        // 3) Owner adds the manager
        Set<Permission> permissions = new HashSet<>();
        
        permissions.add(Permission.APPOINTMENT);
        Response<Void> addManagerResp = shopService.addShopManager(
            ownerToken, shop.getId(), "manager", permissions
        );
        assertTrue(addManagerResp.isOk(), "addShopManager should succeed");
        

        // 4) Now verify that manager was actually added to the shop
        Response<String> permsResp = shopService.getMembersPermissions(ownerToken, shop.getId());
        assertTrue(permsResp.isOk(), "getMembersPermissions should succeed");
        String permsData = permsResp.getData();

        assertTrue(permsData.contains(Permission.APPOINTMENT.name()));
        
    }

    @Test
    public void setManagerPermissionsTest() {
        // 1) Owner setup
        String ownerToken = generateloginAsRegistered("Owner", "Pwd0");
        ShopDTO shop = generateShopAndItems(ownerToken);

        // 2) Manager setup
        String managerGuestToken = userService.enterToSystem().getData();
        Response<Void> managerRegResp = userService.registerUser(
            managerGuestToken, "manager", "pwdM", LocalDate.now().minusYears(30)
        );
        assertTrue(managerRegResp.isOk(), "Manager registration should succeed");

        Response<String> managerLoginResp = userService.loginUser(
            managerGuestToken, "manager", "pwdM"
        );
        assertTrue(managerLoginResp.isOk(), "Manager login should succeed");
        String managerToken = managerLoginResp.getData(); // after login

        // 3) Owner adds the manager
        Set<Permission> permissions = new HashSet<>();
        permissions.add(Permission.APPOINTMENT);
        Response<Void> addManagerResp = shopService.addShopManager(
            ownerToken, shop.getId(), "manager", permissions
        );
        assertTrue(addManagerResp.isOk(), "addShopManager should succeed");

        Response<String> res = shopService.getMembersPermissions(ownerToken, shop.getId());
        
        Response<Void> setPermissionsResp = shopService.addShopManagerPermission
        (
            ownerToken, shop.getId(), "manager", Permission.UPDATE_ITEM_PRICE
        );
        assertTrue(setPermissionsResp.isOk(), "setPermissions should succeed");
        assertNotEquals(res.getData(), shopService.getMembersPermissions(ownerToken, shop.getId()).getData());
        
    }

    @Test
    public void removeManagerTest() {
        // 1) Owner setup
        String ownerToken = generateloginAsRegistered("Owner", "Pwd0");
        ShopDTO shop = generateShopAndItems(ownerToken);

        // 2) Manager setup
        String managerGuestToken = userService.enterToSystem().getData();
        Response<Void> managerRegResp = userService.registerUser(
            managerGuestToken, "manager", "pwdM", LocalDate.now().minusYears(30)
        );
        assertTrue(managerRegResp.isOk(), "Manager registration should succeed");

        Response<String> managerLoginResp = userService.loginUser(
            managerGuestToken, "manager", "pwdM"
        );
        assertTrue(managerLoginResp.isOk(), "Manager login should succeed");
        String managerToken = managerLoginResp.getData(); // after login

        // 3) Owner adds the manager
        Set<Permission> permissions = new HashSet<>();
        
        permissions.add(Permission.APPOINTMENT);
        Response<Void> addManagerResp = shopService.addShopManager(
            ownerToken, shop.getId(), "manager", permissions
        );
        assertTrue(addManagerResp.isOk(), "addShopManager should succeed");

        // 4) Owner removes the manager
        Response<Void> removeManagerResp = shopService.removeAppointment(
            ownerToken, shop.getId(), "manager"
        );
        assertTrue(removeManagerResp.isOk(), "removeShopManager should succeed");
        
        String userToken = generateloginAsRegistered("User", "Pwd0");
        Response<Void> res = shopService.addShopManager(managerToken, shop.getId(), "User", permissions);
        assertFalse(res.isOk(), "addShopManager should fail as the manager was removed");
    }

    @Test
    public void removeAllApointeesTest() {
        // 1) Owner setup
        String ownerToken = generateloginAsRegistered("Owner", "Pwd0");
        ShopDTO shop = generateShopAndItems(ownerToken);

        // 2) Manager setup
        String managerGuestToken = userService.enterToSystem().getData();
        Response<Void> managerRegResp = userService.registerUser(
            managerGuestToken, "manager", "pwdM", LocalDate.now().minusYears(30)
        );
        assertTrue(managerRegResp.isOk(), "Manager registration should succeed");

        Response<String> managerLoginResp = userService.loginUser(
            managerGuestToken, "manager", "pwdM"
        );
        assertTrue(managerLoginResp.isOk(), "Manager login should succeed");
        String managerToken = managerLoginResp.getData(); // after login

        // 3) Owner adds the manager
        Set<Permission> permissions = new HashSet<>();

        permissions.add(Permission.APPOINTMENT);
        Response<Void> addManagerResp = shopService.addShopManager(
            ownerToken, shop.getId(), "manager", permissions
        );
        assertTrue(addManagerResp.isOk(), "addShopManager should succeed");

        String userToken = generateloginAsRegistered("User", "Pwd0");

        // 3) Owner adds the manager
        Set<Permission> userpermissions = new HashSet<>();

        userpermissions.add(Permission.APPOINTMENT);
        Response<Void> addManager2Resp = shopService.addShopManager(
            managerToken, shop.getId(), "User", userpermissions
        );
        assertTrue(addManager2Resp.isOk(), "addShopManager should succeed");
        
        String user2Token = generateloginAsRegistered("User2", "Pwd0");
        Response<Void> res = shopService.addShopManager(userToken, shop.getId(), "User2", permissions);
        assertTrue(res.isOk(), "addShopManager should fail as the manager was removed");

        // 4) Owner removes the manager
        Response<Void> removeManager2Resp = shopService.removeAppointment(
            ownerToken, shop.getId(), "manager"
        );
        assertTrue(removeManager2Resp.isOk(), "removeShopManager should succeed");

        String user3Token = generateloginAsRegistered("User3", "Pwd0");
        Response<Void> res3 = shopService.addShopManager(userToken, shop.getId(), "User3", permissions);
        assertFalse(res3.isOk(), "addShopManager should fail as the manager was removed");
    }

    @Test
    public void appointTwiceFail() {
        // 1) Owner setup
        String ownerToken = generateloginAsRegistered("Owner", "Pwd0");
        ShopDTO shop = generateShopAndItems(ownerToken);

        // 2) Owner adds the manager
        String managerToken = generateloginAsRegistered("Manager", "PwdM");
        
        Set<Permission> permissions = new HashSet<>();

        permissions.add(Permission.APPOINTMENT);
        Response<Void> addManagerResp = shopService.addShopManager(
            ownerToken, shop.getId(), "Manager", permissions
        );
        assertTrue(addManagerResp.isOk(), "addShopManager should succeed");

        // 3) Owner adds the manager
        String manager2Token = generateloginAsRegistered("Manager2", "PwdM");
        
        Set<Permission> permissions2 = new HashSet<>();

        permissions2.add(Permission.APPOINTMENT);
        Response<Void> addManagerResp2 = shopService.addShopManager(
            ownerToken, shop.getId(), "Manager2", permissions2
        );
        assertTrue(addManagerResp2.isOk(), "addShopManager should succeed");

        // 4) Manager tries adding manager2        
        Set<Permission> permissions3 = new HashSet<>();

        permissions3.add(Permission.APPOINTMENT);
        Response<Void> addManagerResp3 = shopService.addShopManager(
            managerToken, shop.getId(), "Manager2", permissions3
        );
        assertFalse(addManagerResp3.isOk(), "addShopManager should succeed");
    }

    @Test
    public void successfulViewShopContent() {
        // 1) Owner creates shop
        String ownerToken = generateloginAsRegistered("Owner", "Pwd0");
        ShopDTO shop = generateShopAndItems(ownerToken);

        // 2) Manager setup
        String mgrGuest = userService.enterToSystem().getData();
        userService.registerUser(mgrGuest, "mgr", "pwdM", LocalDate.now().minusYears(30));
        String mgrToken = userService.loginUser(mgrGuest, "mgr", "pwdM").getData();

        // 3) Owner assigns manager WITH VIEW permission
        Set<Permission> perms = new HashSet<>();
        perms.add(Permission.VIEW);
        Response<Void> addMgr = shopService.addShopManager(ownerToken, shop.getId(), "mgr", perms);
        assertTrue(addMgr.isOk(), "addShopManager should succeed");

        // 4) Manager views dashboard
        Response<ShopDTO> viewResp = shopService.getShopInfo(mgrToken, shop.getId());
        assertTrue(viewResp.isOk(), "viewShopContent should succeed");
        ShopDTO seen = viewResp.getData();

        assertEquals(shop.getId(),   seen.getId(),   "Shop ID must match");
        assertEquals(shop.getName(), seen.getName(), "Shop name must match");
    }

    @Test
    public void viewShopNotLoggedIn() {
        // 1) Owner creates shop
        String ownerToken = generateloginAsRegistered("Owner", "Pwd0");
        ShopDTO shop = generateShopAndItems(ownerToken);

        // 2) User never logged in → use an invalid token
        String badToken = "not-a-valid-token";

        // 3) Attempt to view
        Response<ShopDTO> resp = shopService.getShopInfo(badToken, shop.getId());
        assertFalse(resp.isOk(), "Should fail when not logged in");
    }

    // @Test
    // public void userNotManagerOfShop() {
    //     // 1) Owner creates shop
    //     String ownerToken = generateloginAsRegistered("Owner", "Pwd0");
    //     ShopDTO shop = generateShopAndItems(ownerToken);

    //     // 2) Some other user logs in
    //     String guest = userService.enterToSystem().getData();
    //     userService.registerUser(guest, "other", "pwdO", LocalDate.now().minusYears(28));
    //     String otherToken = userService.loginUser(guest, "other", "pwdO").getData();

    //     // 3) That user attempts to view
    //     Response<ShopDTO> resp = shopService.getShopInfo(otherToken, shop.getId());
    //     assertFalse(resp.isOk(), "Should fail when not a manager");
    // }
    @Test
    public void submitBidOfferTest() {
        // 1) Owner + shop setup
        String ownerToken = generateloginAsRegistered("Owner", "Pwd0");
        ShopDTO shop = generateShopAndItems(ownerToken);
        int shopId = shop.getId();
        int appleId = shop.getItems().get(0).getItemID();

        // 2) Buyer setup
        String buyerToken = generateloginAsRegistered("Buyer", "Pwd1");

        // 3) Buyer submits a bid
        Response<Void> bidResp = orderService.submitBidOffer(
            buyerToken,
            shopId,
            appleId,
            10.0  // offer price
        );
        assertTrue(bidResp.isOk(), "submitBidOffer should succeed");
    }

    @Test
    public void answerBidAndPurchaseBidItemTest() {
        
        when(payment.validatePaymentDetails()).thenReturn(true);
        when(payment.processPayment(1.0)).thenReturn(true);
        when(shipment.validateShipmentDetails()).thenReturn(true);
        when(shipment.processShipment(0.1)).thenReturn(true);
        // 1) Owner + shop + buyer setup as before
        String ownerToken = generateloginAsRegistered("Owner", "Pwd0");
        ShopDTO shop = generateShopAndItems(ownerToken);
        int shopId = shop.getId();
        int appleId = shop.getItems().get(0).getItemID();

        String buyerToken = generateloginAsRegistered("Buyer", "Pwd1");

        // 2) Buyer submits the bid (first-ever bid, ID == 0)
        assertTrue(orderService.submitBidOffer(buyerToken, shopId, appleId, 15.0).isOk());

        // 3) Owner accepts the bid
        int bidId = 1;
        Response<Void> answerResp = shopService.answerBid(
            ownerToken,
            shopId,
            bidId,
            true   // accept
        );
        assertTrue(answerResp.isOk(), "answerBid should succeed");

        // 4) Buyer purchases the accepted bid item
        Response<Void> purchaseResp = orderService.purchaseBidItem(
            buyerToken,
            shopId,
            bidId
        );
        assertTrue(purchaseResp.isOk(), "purchaseBidItem should succeed");

        // 5) Verify it ended up in their order history
        List<Order> history = orderService.viewPersonalOrderHistory(buyerToken).getData();
        assertEquals(1, history.size(), "There should be exactly one bid-based order");
    }

    @Test
    public void submitCounterBidTest() {
        // 1) Owner + shop + buyer setup
        String ownerToken = generateloginAsRegistered("Owner", "Pwd0");
        ShopDTO shop = generateShopAndItems(ownerToken);
        int shopId = shop.getId();
        int appleId = shop.getItems().get(0).getItemID();

        String buyerToken = generateloginAsRegistered("Buyer", "Pwd1");

        // 2) Buyer places the original bid
        assertTrue(orderService.submitBidOffer(buyerToken, shopId, appleId, 20.0).isOk());

        // 3) Owner counters that bid
        int bidId = 1;
        Response<Void> counterResp = shopService.submitCounterBid(
            ownerToken,
            shopId,
            bidId,
            25.0   // new counter-offer price
        );
        assertTrue(counterResp.isOk(), "submitCounterBid should succeed");
    }

    @Test
    public void userLacksPermission() {
        // 1) Owner creates shop
        String ownerToken = generateloginAsRegistered("Owner", "Pwd0");
        ShopDTO shop = generateShopAndItems(ownerToken);

        // 2) Manager setup
        String mgrGuest = userService.enterToSystem().getData();
        userService.registerUser(mgrGuest, "mgr2", "pwdM2", LocalDate.now().minusYears(30));
        String mgrToken = userService.loginUser(mgrGuest, "mgr2", "pwdM2").getData();

        // 3) Owner assigns manager WITHOUT VIEW permission
        Set<Permission> perms = new HashSet<>();  // empty set
        Response<Void> addMgr = shopService.addShopManager(ownerToken, shop.getId(), "mgr2", perms);
        assertTrue(addMgr.isOk(), "addShopManager should succeed");

        // 4) Manager tries to view
        Response<String> resp = shopService.getMembersPermissions(mgrToken, shop.getId());
        assertFalse(resp.isOk(), "Should fail when lacking VIEW permission");
    }

    @Test
    public void successfulEditProduct() {
        // 1) Owner creates shop + items
        String ownerToken = generateloginAsRegistered("Owner", "Pwd0");
        ShopDTO shop = generateShopAndItems(ownerToken);

        // 2) Manager setup
        String mgrGuestToken = userService.enterToSystem().getData();
        userService.registerUser(mgrGuestToken, "mgr", "pwdM", LocalDate.now().minusYears(30));
        String mgrToken = userService.loginUser(mgrGuestToken, "mgr", "pwdM").getData();

        // 3) Owner assigns manager + gives INVENTORY permission
        Response<Void> addMgr = shopService.addShopManager(ownerToken, shop.getId(), "mgr", new HashSet<>());
        assertTrue(addMgr.isOk(), "addShopManager should succeed");
        Response<Void> givePerm = shopService.addShopManagerPermission(
            ownerToken, shop.getId(), "mgr", Permission.UPDATE_ITEM_QUANTITY
        );
        assertTrue(givePerm.isOk(), "addShopManagerPermission should succeed");

        // 4) Manager edits the quantity of an existing item
        List<ItemDTO> items = shopService.showShopItems(shop.getId()).getData();
        ItemDTO first = items.get(0);
        int newQty = first.getQuantity() + 5;
        Response<Void> editResp = shopService.changeItemQuantityInShop(
            mgrToken, shop.getId(), first.getItemID(), newQty
        );
        assertTrue(editResp.isOk(), "changeItemQuantityInShop should succeed");

        // 5) Verify via service
        List<ItemDTO> updated = shopService.showShopItems(shop.getId()).getData();
        ItemDTO updatedFirst = updated.stream()
            .filter(i -> i.getItemID() == first.getItemID())
            .findFirst()
            .orElseThrow();
        assertEquals(newQty, updatedFirst.getQuantity(), "Quantity should be updated");
    }

    @Test
    public void editProductNotLoggedIn() {
        // 1) Attempt to edit without logging in
        String invalidToken = "not-a-token";
        Response<Void> resp = shopService.changeItemQuantityInShop(
            invalidToken, 1, 42, 10
        );
        assertFalse(resp.isOk(), "Should fail if not logged in");
    }

    @Test
    public void productNotFound() {
        // 1) Owner setup
        String ownerToken = generateloginAsRegistered("Owner", "Pwd0");
        ShopDTO shop = generateShopAndItems(ownerToken);

        // 2) Owner tries to edit a non-existent product
        int missingId = 9999;
        Response<Void> resp = shopService.changeItemQuantityInShop(
            ownerToken, shop.getId(), missingId, 10
        );
        assertFalse(resp.isOk(), "Should fail when product does not exist");
    }

    @Test
    public void editProductUserLacksPermission() {
        // 1) Owner + shop + items
        String ownerToken = generateloginAsRegistered("Owner", "Pwd0");
        ShopDTO shop = generateShopAndItems(ownerToken);

        // 2) Manager setup (no inventory permission)
        String mgrGuest = userService.enterToSystem().getData();
        userService.registerUser(mgrGuest, "mgrNoInv", "pwdI", LocalDate.now().minusYears(30));
        String mgrToken = userService.loginUser(mgrGuest, "mgrNoInv", "pwdI").getData();

        // 3) Owner assigns the user as manager—but with an empty permission set
        Response<Void> addMgr = shopService.addShopManager(ownerToken, shop.getId(), "mgrNoInv", new HashSet<>());
        assertTrue(addMgr.isOk(), "addShopManager should succeed");

        // 4) Manager attempts to edit the first product’s quantity
        List<ItemDTO> items = shopService.showShopItems(shop.getId()).getData();
        int itemId = items.get(0).getItemID();
        int newQty = items.get(0).getQuantity() + 5;

        Response<Void> resp = shopService.changeItemQuantityInShop(
            mgrToken, shop.getId(), itemId, newQty
        );

        // 5) Expect a permission-denied error
        assertFalse(resp.isOk(), "Should fail when lacking inventory permission");
    }


    // @Test
    // public void successfulEditPurchasePolicy() {
    //     // 1) Owner + shop + items
    //     String ownerToken = generateloginAsRegistered("Owner", "Pwd0");
    //     ShopDTO shop = generateShopAndItems(ownerToken);

    //     // 2) Manager setup
    //     String mgrGuest = userService.enterToSystem().getData();
    //     userService.registerUser(mgrGuest, "mgrP", "pwdP", LocalDate.now().minusYears(30));
    //     String mgrToken = userService.loginUser(mgrGuest, "mgrP", "pwdP").getData();

    //     // 3) Owner assigns manager WITH purchase‐policy permission
    //     Set<Permission> perms = new HashSet<>();
    //     perms.add(Permission.UPDATE_PURCHASE_POLICY);
    //     Response<Void> addMgr = shopService.addShopManager(ownerToken, shop.getId(), "mgrP", perms);
    //     assertTrue(addMgr.isOk(), "addShopManager should succeed");

    //     // 4) Manager submits valid purchase policy
    //     String newPolicy = "AT_LEAST_ONE_FROM_SHOP";  // your domain’s valid format
    //     Response<Void> editResp = shopService.updatePurchaseType(mgrToken, shop.getId(), newPolicy);
    //     assertTrue(editResp.isOk(), "updatePurchaseType should succeed");
    // }

    @Test
    public void editPurchasePolicyNotLoggedIn() {
        // use an invalid token
        String badToken = "invalid";
        Response<Void> resp = shopService.updatePurchaseType(badToken, 1, "ANY_POLICY");
        assertFalse(resp.isOk(), "Should fail when not logged in");
    }

    @Test
    public void userLacksPermissionOnPurchasePolicy() {
        // 1) Owner + shop
        String ownerToken = generateloginAsRegistered("Owner", "Pwd0");
        ShopDTO shop = generateShopAndItems(ownerToken);

        // 2) Manager without that permission
        String mgrGuest = userService.enterToSystem().getData();
        userService.registerUser(mgrGuest, "mgrNoP", "pwdX", LocalDate.now().minusYears(30));
        String mgrToken = userService.loginUser(mgrGuest, "mgrNoP", "pwdX").getData();

        Response<Void> addMgr = shopService.addShopManager(ownerToken, shop.getId(), "mgrNoP", new HashSet<>());
        assertTrue(addMgr.isOk(), "addShopManager should succeed");

        // 3) Attempt to edit
        Response<Void> resp = shopService.updatePurchaseType(mgrToken, shop.getId(), "ANY_POLICY");
        assertFalse(resp.isOk(), "Should fail when lacking permission");
    }

    @Test
    public void invalidPurchasePolicyFormat() {
        // 1) Owner + shop
        String ownerToken = generateloginAsRegistered("Owner", "Pwd0");
        ShopDTO shop = generateShopAndItems(ownerToken);

        // 2) Manager with permission
        String mgrGuest = userService.enterToSystem().getData();
        userService.registerUser(mgrGuest, "mgrBadP", "pwdB", LocalDate.now().minusYears(30));
        String mgrToken = userService.loginUser(mgrGuest, "mgrBadP", "pwdB").getData();

        Set<Permission> perms = new HashSet<>();
        perms.add(Permission.UPDATE_PURCHASE_POLICY);
        shopService.addShopManager(ownerToken, shop.getId(), "mgrBadP", perms);

        // 3) Submit malformed policy
        Response<Void> resp = shopService.updatePurchaseType(mgrToken, shop.getId(), "not a valid format");
        assertFalse(resp.isOk(), "Should fail on invalid format");
    }

    @Test
    public void successfulShopClosing() {
        // 1) System (owner) creates and opens a shop
        String ownerToken = generateloginAsRegistered("Owner", "Pwd0");
        ShopDTO shop = generateShopAndItems(ownerToken);

        // 2) Owner closes it
        Response<Void> closeResp = shopService.closeShop(ownerToken, shop.getId());
        assertTrue(closeResp.isOk(), "closeShopByFounder should succeed");

        // 3) After closing, it must no longer show up among open shops
        List<ShopDTO> openShops = shopService.showAllShops().getData();
        assertFalse(openShops.stream().anyMatch(s -> s.getId() == shop.getId()),
                    "Closed shop must not appear in showAllShops()");
    }
    @Test
    public void closingClosedShop() {
        // 1) Owner creates and closes a shop
        String ownerToken = generateloginAsRegistered("Owner", "Pwd0");
        ShopDTO shop = generateShopAndItems(ownerToken);
        assertTrue(shopService.closeShop(ownerToken, shop.getId()).isOk(),
                   "Initial close should succeed");

        // 2) Owner attempts to close it again
        Response<Void> secondClose = shopService.closeShop(ownerToken, shop.getId());
        assertFalse(secondClose.isOk(), "Closing an already closed shop should fail");
    }

    @Test
    public void concurrentRegisterSameUsername2() throws InterruptedException {
        when(payment.validatePaymentDetails()).thenReturn(true);
        when(shipment.validateShipmentDetails()).thenReturn(true);

        for (int i = 0; i < 10; i++) {
            // 1) Owner creates shop with exactly 1 unit of “Apple”
            generateloginAsRegistered("owner"+i, "pwdO"+i);
            String guest1 = userService.enterToSystem().getData();
            String guest2 = userService.enterToSystem().getData();
            String wanted = "dupUser"+i;

            // Build two callables
            List<Callable<Response<Void>>> tasks = List.of(
            () -> userService.registerUser(guest1, wanted, "pw", LocalDate.now().minusYears(20)),
            () -> userService.registerUser(guest2, wanted, "pw", LocalDate.now().minusYears(20))
            );

            ExecutorService ex = Executors.newFixedThreadPool(2);
            // This will block until *both* tasks have completed
            List<Future<Response<Void>>> futures = ex.invokeAll(tasks);
            ex.shutdown();

            long successCount = futures.stream()
            .map(f -> {
                try { return f.get().isOk(); }
                catch (Exception e) { return false; }
            })
            .filter(ok -> ok)
            .count();

            // Now exactly one registration should succeed
            assertEquals(1, successCount, "Only one registration may succeed");
        }
    }

    @Test
    public void concurrentPurchaseSameItem() throws InterruptedException {
        when(payment.validatePaymentDetails()).thenReturn(true);
        when(shipment.validateShipmentDetails()).thenReturn(true);
        for (int i = 0; i < 10; i++) {
            // 1) Owner creates shop with exactly 1 unit of “Apple”
            String ownerToken = generateloginAsRegistered("owner"+i, "pwdO");
            ShopDTO shop = generateShopAndItems(ownerToken);
            // find the apple’s ID and set its stock to 1
            int shopId = shop.getId();
            int appleId = shop.getItems().values().stream()
                                .filter(j -> j.getName().equals("Apple"))
                                .findFirst().get().getItemID();
            Response<Void> setOne = shopService.changeItemQuantityInShop(
                ownerToken, shopId, appleId, 1
            );
            assertTrue(setOne.isOk(), "Should be able to set stock to 1");

            // 2) Two separate buyers each enter & add that one apple to their cart
            String buyer1 = userService.enterToSystem().getData();
            String buyer2 = userService.enterToSystem().getData();
            assertNotNull(buyer1);
            assertNotNull(buyer2);

            // build the same itemsMap for both
            var itemsMap = new HashMap<Integer, HashMap<Integer,Integer>>();
            var m = new HashMap<Integer,Integer>();
            m.put(appleId, 1);
            itemsMap.put(shopId, m);

            Response<Void> add1 = orderService.addItemsToCart(buyer1, itemsMap);
            Response<Void> add2 = orderService.addItemsToCart(buyer2, itemsMap);
            assertTrue(add1.isOk(), "Buyer1 should add Apple");
            assertTrue(add2.isOk(), "Buyer2 should add Apple");

            // 3) Concurrently attempt to buy
            List<Callable<Response<Order>>> tasks = List.of(
                () -> orderService.buyCartContent(buyer1),
                () -> orderService.buyCartContent(buyer2)
            );

            ExecutorService ex = Executors.newFixedThreadPool(2);
            List<Future<Response<Order>>> futures = ex.invokeAll(tasks);
            ex.shutdown();

            long successCount = futures.stream()
            .map(f -> {
                try { return f.get().isOk(); }
                catch(Exception e) { return false; }
            })
            .filter(ok -> ok)
            .count();

            long failureCount = futures.size() - successCount;

            assertEquals(1, successCount, "Exactly one purchase may succeed");
            assertEquals(1, failureCount, "Exactly one purchase must fail due to out-of-stock");
        }
    }

    @Test
    public void concurrentRemoveAndPurchase() throws InterruptedException {
        for (int i = 0; i < 10; i++) {
            // 1) Owner creates shop with exactly 1 unit of “Apple”
            String ownerToken = generateloginAsRegistered("owner"+i, "pwdO");
            ShopDTO shop = generateShopAndItems(ownerToken);
            int shopId = shop.getId();
            int appleId = shop.getItems().values().stream()
                                .filter(j -> j.getName().equals("Apple"))
                                .findFirst().get().getItemID();
            // set stock = 1
            assertTrue(shopService.changeItemQuantityInShop(ownerToken, shopId, appleId, 1)
                    .isOk(), "Should be able to set stock to 1");

            // 2) Buyer enters & adds that one apple to cart
            String buyer = userService.enterToSystem().getData();
            var itemsMap = new HashMap<Integer, HashMap<Integer,Integer>>();
            itemsMap.put(shopId, new HashMap<>(Map.of(appleId, 1)));
            assertTrue(orderService.addItemsToCart(buyer, itemsMap)
                    .isOk(), "Buyer should add the only Apple");

            // 3) Concurrently: owner removes item, buyer attempts checkout
            List<Callable<Boolean>> tasks = List.of(
                () -> shopService.removeItemFromShop(ownerToken, shopId, appleId).isOk(),
                () -> orderService.buyCartContent(buyer).isOk()
            );

            ExecutorService ex = Executors.newFixedThreadPool(2);
            List<Future<Boolean>> results = ex.invokeAll(tasks);
            ex.shutdown();

            long succeeded = results.stream()
                .map(f -> {
                    try { return f.get(); }
                    catch (Exception e) { return false; }
                })
                .filter(ok -> ok)
                .count();

            // exactly one action may succeed
            assertEquals(1, succeeded,
                "Exactly one of removeItem or buyCartContent must succeed; succeeded=" + succeeded);
        }
    }

    @Test
    public void concurrentManagerAppointment() throws InterruptedException {
        for (int i = 0; i < 10; i++) {
            // 1) Owner creates shop
            String ownerToken = generateloginAsRegistered("owner"+i, "pwdO");
            ShopDTO shop = generateShopAndItems(ownerToken);
            int shopId = shop.getId();

            // 2) Prepare candidate user
            String candGuest = userService.enterToSystem().getData();
            assertTrue(userService.registerUser(candGuest, "candidate"+i, "pwdC", LocalDate.now().minusYears(25))
                    .isOk(), "Candidate registration should succeed");

            // 3) Two concurrent attempts to appoint the same candidate
            int iteration = i;  // effectively final
            Set<Permission> perms = Set.of(Permission.VIEW);
            List<Callable<Boolean>> tasks = List.of(
                () -> shopService.addShopManager(ownerToken, shopId, "candidate"+iteration, perms).isOk(),
                () -> shopService.addShopManager(ownerToken, shopId, "candidate"+iteration, perms).isOk()
            );

            ExecutorService ex = Executors.newFixedThreadPool(2);
            List<Future<Boolean>> results = ex.invokeAll(tasks);
            ex.shutdown();

            long successCount = results.stream()
                .map(f -> {
                    try { return f.get(); }
                    catch (Exception e) { return false; }
                })
                .filter(ok -> ok)
                .count();

            assertEquals(1, successCount,
                "Exactly one of the two concurrent addShopManager calls should succeed, but got " + successCount);
        }
    }
}