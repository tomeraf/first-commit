package Tests;
import Service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.Assume.assumeTrue;
import static org.junit.jupiter.api.Assertions.*;

import Service.ShopService;
import Service.UserService;
import Domain.DTOs.PaymentDetailsDTO;
import Domain.Guest;
import Domain.Manager;
import Domain.Owner;
import Domain.Registered;
import Domain.Response;
import Domain.Shop;
import Domain.DTOs.ItemDTO;
import Domain.DTOs.ShopDTO;
import Domain.Purchase.PurchaseType;
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
    private OrderService orderService;
    private ConcurrencyHandler concurrencyHandler;
    
    
    public endToEndTest_1() {
        shopRepository = new MemoryShopRepository();
        userRepository = new MemoryUserRepository();
        orderRepository = new MemoryOrderRepository();
        jwtAdapter = new JWTAdapter();
        concurrencyHandler = new ConcurrencyHandler();
        //shipment = new ProxyShipment();
        //payment = new ProxyPayment();



        userService = new UserService(userRepository, jwtAdapter, concurrencyHandler);
        shopService = new ShopService(userRepository, shopRepository, orderRepository, jwtAdapter, concurrencyHandler);
        orderService = new OrderService(userRepository, shopRepository, orderRepository, jwtAdapter, payment, shipment, concurrencyHandler);
    }

    public String generateloginAsRegistered() {
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
        Response<Void> chgQty = shopService.changeItemQuantityInShop(
            ownerToken, shopId, appleId, 10
        );
        assertTrue(chgQty.isOk(), "changeItemQuantityInShop should succeed");

        // 4) Retrieve final list and return it
        Response<List<ItemDTO>> finalResp = shopService.showShopItems(shopId);
        assertTrue(finalResp.isOk(), "showShopItems should succeed");
        List<ItemDTO> items = finalResp.getData();
        assertNotNull(items, "Returned item list must not be null");
        assertEquals(3, items.size(), "There should be 3 items in the shop");

        return shop;
    }
    public Order buyCartContent(String sessionToken) {
        PaymentDetailsDTO paymentDetails = new PaymentDetailsDTO(
            "1234567890123456", "John Doe", "123456789", "12/25", "123"
        );
        String shipmentInfo = "";
        Response<Order> purchaseResp = orderService.buyCartContent(
            sessionToken,
            paymentDetails,
            shipmentInfo
        );
        assertTrue(purchaseResp.isOk(), "buyCartContent should succeed");
        Order created = purchaseResp.getData();
        assertNotNull(created, "Returned Order must not be null");
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
        System.out.println(res.getError());

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
        Response<Void> logoutRespGuest = userService.exitAsGuest(guestToken.getData());
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
        String ownerToken = generateloginAsRegistered();
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
        filters.put("minPrice",  "0.60");
        filters.put("maxPrice",  "2.00");
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
        String ownerToken = generateloginAsRegistered();
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
        String ownerToken = generateloginAsRegistered();
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
        String ownerToken = generateloginAsRegistered();
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
        String ownerToken = generateloginAsRegistered();
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
        assertEquals(1, shopItems.size(), "Shop should contain exactly one item");

        // Buyer adds that item to cart
        Response<Void> addToCart = orderService.addItemsToCart(
            buyerGuestToken,
            shopItems
        );
        assertTrue(addToCart.isOk(), "addItemsToCart should succeed");

        List<ItemDTO> cartItems = orderService.checkCartContent(buyerGuestToken).getData();
        assertEquals(1, cartItems.size(), "Cart should contain exactly one item");
    }

    @Test
    public void checkCartContentTest() {
        String ownerToken = generateloginAsRegistered();
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
        Response<Void> addToCart = orderService.addItemsToCart(
            buyerGuestToken,
            itemsToAdd
        );
        assertTrue(addToCart.isOk(), "addItemsToCart should succeed");

        // verify cart contents
        List<ItemDTO> cartItems = orderService.checkCartContent(buyerGuestToken).getData();
        assertEquals(1, cartItems.size(), "Cart should contain exactly one item");

        // verify stock was decremented by 1
        Response<ShopDTO> updatedShopResp = shopService.getShopInfo(ownerToken, shop.getId());
        assertTrue(updatedShopResp.isOk(), "getShopInfo should succeed after cart operation");
        ShopDTO updatedShop = updatedShopResp.getData();
        int originalQty = shop.getItems().get(items.get(0).getItemID()).getQuantity();
        int newQty      = updatedShop.getItems().get(items.get(0).getItemID()).getQuantity();
        assertEquals(originalQty - 1, newQty, "Stock should decrease by 1 when added to cart");
    }

    @Test
    public void buyCartContentTest() {
        // 1) Owner setup
        String ownerToken = generateloginAsRegistered();
        ShopDTO shop = generateShopAndItems(ownerToken);
        List<ItemDTO> shopItems = shopService.showShopItems(shop.getId()).getData();
        ItemDTO toBuy = shopItems.get(0);

        // 2) Buyer setup
        String buyerToken = generateloginAsRegistered();

        // 3) Add to cart
        Response<Void> addResp = orderService.addItemsToCart(
            buyerToken,
            List.of(toBuy)
        );
        assertTrue(addResp.isOk(), "addItemsToCart should succeed");

        // 4) Purchase (pass dummy payment/shipment; replace with valid data if needed)
        Order created = buyCartContent(buyerToken);

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
    }

    @Test
    public void changeCartContentTest() {
        // 1) Owner setup
        String ownerToken = generateloginAsRegistered();
        ShopDTO shop = generateShopAndItems(ownerToken);
        List<ItemDTO> shopItems = shopService.showShopItems(shop.getId()).getData();

        // 2) Buyer setup: enter, register, login
        Response<String> guestResp = userService.enterToSystem();
        assertTrue(guestResp.isOk(), "Buyer enterToSystem should succeed");
        String guestToken = guestResp.getData();

        // 3) Add two different items to cart
        Response<Void> addResp = orderService.addItemsToCart(
            guestToken,
            List.of(shopItems.get(0), shopItems.get(1))
        );
        assertTrue(addResp.isOk(), "addItemsToCart should succeed");

        List<ItemDTO> cartAfterAdd = orderService.checkCartContent(guestToken).getData();
        assertEquals(2, cartAfterAdd.size(), "Cart should contain exactly two items");

        // 4) Remove the first item
        Response<Void> removeResp = orderService.removeItemsFromCart(
            guestToken,
            List.of(shopItems.get(0))
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
        // 1) Owner setup
        String ownerToken = generateloginAsRegistered();
        ShopDTO shop = generateShopAndItems(ownerToken);

        // 2) Buyer setup: enter -> register -> login
        String buyerToken = generateloginAsRegistered();

        // 3) Buyer purchases one item (necessary to enable rating)
        List<ItemDTO> shopItems = shopService.showShopItems(shop.getId()).getData();
        assertFalse(shopItems.isEmpty(), "Shop must have at least one item");
        ItemDTO toBuy = shopItems.get(0);

        Response<Void> addToCart = orderService.addItemsToCart(
            buyerToken,
            List.of(toBuy)
        );
        assertTrue(addToCart.isOk(), "addItemsToCart should succeed");

        // dummy payment & shipment (fill in real fields if needed)
        Order created = buyCartContent(buyerToken);

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


    // @Test
    // public void sendMessageToShopTest() {
    //     // 1) Owner creates a shop with items
    //     String ownerToken = generateloginAsRegistered();
    //     ShopDTO shopDto = generateShopAndItems(ownerToken);
    //     int shopId = shopDto.getId();

    //     // 2) A second user (sender) logs in
    //     String senderToken = generateloginAsRegistered();

    //     // 3) Send a message to the shop
    //     String title = "Problem with the name of the shop";
    //     String content = "Hello sir, I have a problem with the name of the shop. "
    //                 + "Can you please change it to something less racist?";
    //     Response<Void> res = shopService.sendMessage(senderToken, shopId, title, content);
    //     assertTrue(res.isOk(), "sendMessageToShop should succeed");   
    // }

    // @Test
    // public void viewPersonalOrderHistoryTest()
    // {
        
    // }

    @Test
    public void ItemUnavailableForPurchase()
    {
        String ownerToken = generateloginAsRegistered();
        ShopDTO shop = generateShopAndItems(ownerToken);

        ItemDTO item = shop.getItems().get(0);
        Response<Void> res = shopService.changeItemQuantityInShop(ownerToken, shop.getId(), item.getItemID(), 0);

        String buyerToken = generateloginAsRegistered();
        Response<List<ItemDTO>> viewResp = shopService.showShopItems(shop.getId());
        assertTrue(viewResp.isOk(), "showShopItems should succeed");
        List<ItemDTO> shopItems = viewResp.getData();
        assertNotNull(shopItems, "shopItems list must not be null");

        orderService.addItemsToCart(
            buyerToken,
            shopItems
        );

        PaymentDetailsDTO paymentDetails = new PaymentDetailsDTO(
            "1234567890123456", "John Doe", "123456789", "12/25", "123"
        );
        String shipmentInfo = "";
        Response<Order> purchaseResp = orderService.buyCartContent(
            buyerToken,
            paymentDetails,
            shipmentInfo
        );
        assertFalse(purchaseResp.isOk(), "buyCartContent should fail as one of the items is unavailable");
    }

    @Test
    public void duplicateShopNameTest()
    {
        String ownerToken = generateloginAsRegistered();
        ShopDTO shop = generateShopAndItems(ownerToken);
        
        String userToken = generateloginAsRegistered();
        Response<ShopDTO> shopResp = shopService.createShop(
            userToken, "MyShop", "A shop for tests"
        );

        assertFalse(shopResp.isOk(), "Shop creation should fail as the name is already taken");
        
    }

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
    public void RateShopNotLoggedInTest()
    {
        // 1) Owner creates a shop with items
        String ownerToken = generateloginAsRegistered();
        ShopDTO shopDto = generateShopAndItems(ownerToken);
        
        String guestToken = userService.enterToSystem().getData();
        List<ItemDTO> items = shopService.showShopItems(shopDto.getId()).getData();
        orderService.addItemsToCart(guestToken, items);

        buyCartContent(guestToken);
        
        Response<Void> res =shopService.rateShop(guestToken, shopDto.getId(), 5);
        assertFalse(res.isOk(), "Rate shop should fail when not logged in");
    }

    @Test
    public void addItemToShop() 
    {
        String ownerToken = generateloginAsRegistered();

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

        assertTrue(addA.isOk(), "Adding Apple should succeed");
        assertEquals("Apple", addA.getData().getName(), "Item name should be 'Apple'");
        assertEquals(Category.FOOD, addA.getData().getCategory(), "Item category should be FOOD");
        assertEquals(1.00, addA.getData().getPrice(), "Item price should be 1.00");
        assertEquals("fresh apple", addA.getData().getDescription());
        assertEquals(1, shopResp.getData().getItems().size(), "Shop should contain exactly one item after adding Apple");
        
    }

    @Test
    public void addItemToANonExistentShop() 
    {
        String ownerToken = generateloginAsRegistered();

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
        String ownerToken = generateloginAsRegistered();
        ShopDTO shopDto = generateShopAndItems(ownerToken);

        String userToken = generateloginAsRegistered();
        Response<ItemDTO> addA = shopService.addItemToShop(
            userToken, shopDto.getId(),
            "Apple", Category.FOOD, 1.00, "fresh apple"
        );
        assertFalse(addA.isOk(), "Adding Apple should fail as the user is not the owner");
    }

    @Test
    public void addDuplicateItemToShop()
    {
        String ownerToken = generateloginAsRegistered();
        ShopDTO shopDto = generateShopAndItems(ownerToken);

        Response<ItemDTO> addA = shopService.addItemToShop(
            ownerToken, shopDto.getId(),
            "Apple", Category.FOOD, 1.00, "fresh apple"
        );
        assertFalse(addA.isOk(), "Adding duplicate item should fail");
    }

    public void removeItemsFromShop()
    {
        String ownerToken = generateloginAsRegistered();
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
        String ownerToken = generateloginAsRegistered();
        ShopDTO shopDto = generateShopAndItems(ownerToken);

        String userToken = generateloginAsRegistered();
        Response<Void> removeResp = shopService.removeItemFromShop(
            userToken, shopDto.getId(), shopDto.getItems().get(0).getItemID()
        );
        assertFalse(removeResp.isOk(), "removeItemFromShop should fail as the user is not the owner");
    }

    @Test
    public void removeNonExistentItemFromShop()
    {
        String ownerToken = generateloginAsRegistered();
        ShopDTO shopDto = generateShopAndItems(ownerToken);

        Response<Void> removeResp = shopService.removeItemFromShop(
            ownerToken, shopDto.getId(), 456 // Non-existent item ID
        );
        assertFalse(removeResp.isOk(), "removeItemFromShop should fail as the item does not exist");
    }

    @Test
    public void editItemInShop()
    {
        String ownerToken = generateloginAsRegistered();
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
        String ownerToken = generateloginAsRegistered();
        ShopDTO shopDto = generateShopAndItems(ownerToken);

        String userToken = generateloginAsRegistered();

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
        String ownerToken = generateloginAsRegistered();
        ShopDTO shopDto = generateShopAndItems(ownerToken);

        // 2) Owner edits an item
        ItemDTO itemToEdit = shopDto.getItems().get(0);
        Response<Void> editResp = shopService.changeItemPriceInShop(
            ownerToken, shopDto.getId(), itemToEdit.getItemID(), -100.00
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
    public void addPurchaseDiscountTypeSuccessTest() {
        // 1) Owner setup
        String ownerToken = generateloginAsRegistered();
        ShopDTO shop = generateShopAndItems(ownerToken);

        // 2) Update purchase type
        Response<Void> updateResp = shopService.updatePurchaseType(ownerToken, shop.getId(), PurchaseType.AUCTION.name());
        assertTrue(updateResp.isOk(), "Adding purchase type should succeed");
    }

    @Test
    public void addPurchaseDiscountTypeShopNotFoundTest() {
        // 1) User setup
        String userToken = generateloginAsRegistered();

        // 2) Attempt to add purchase type to non-existing shop
        int nonExistingShopId = 9999;
        Response<Void> updateResp = shopService.updatePurchaseType(userToken, nonExistingShopId, PurchaseType.AUCTION.name());
        assertFalse(updateResp.isOk(), "Adding purchase type to non-existing shop should fail");
    }

    @Test
    public void removePurchaseDiscountTypeSuccessTest() {
        // 1) Owner setup
        String ownerToken = generateloginAsRegistered();
        ShopDTO shop = generateShopAndItems(ownerToken);

        // 2) Owner first adds purchase type
        Response<Void> addResp = shopService.updatePurchaseType(ownerToken, shop.getId(), PurchaseType.AUCTION.name());
        assertTrue(addResp.isOk(), "Adding purchase type should succeed");

        // 3) Then owner removes it by setting default or blank type (depends how you remove)
        Response<Void> removeResp = shopService.updatePurchaseType(ownerToken, shop.getId(), PurchaseType.BID.name());
        assertTrue(removeResp.isOk(), "Removing purchase type should succeed");
    }

    @Test
    public void addPurchaseDiscountTypeUnauthorizedTest() {
        // 1) Setup: Owner1 creates shop
        String owner1Token = generateloginAsRegistered();
        ShopDTO shop = generateShopAndItems(owner1Token);

        // 2) Buyer (other user)
        String buyerToken = generateloginAsRegistered();

        // 3) Buyer tries to update purchase type
        Response<Void> updateResp = shopService.updatePurchaseType(buyerToken, shop.getId(), "NEW_PURCHASE_TYPE");
        assertFalse(updateResp.isOk(), "Unauthorized user should not be able to add purchase type");
    }

    @Test
    public void removePurchaseDiscountTypeNotFoundTest() {
        // 1) Owner setup
        String ownerToken = generateloginAsRegistered();
        ShopDTO shop = generateShopAndItems(ownerToken);

        // 2) Owner tries to remove a type when none exists
        Response<Void> removeResp = shopService.updatePurchaseType(ownerToken, shop.getId(), "");
        assertFalse(removeResp.isOk(), "Removing non-existing purchase type should fail");
    }

    @Test
    public void updateDiscountTypeSuccessTest() {
        // 1) Owner setup
        String ownerToken = generateloginAsRegistered();
        ShopDTO shop = generateShopAndItems(ownerToken);

        // 2) Owner updates discount type
        Response<Void> updateResp = shopService.updateDiscountType(ownerToken, shop.getId(), "NEW_DISCOUNT_TYPE");
        assertTrue(updateResp.isOk(), "Updating discount type should succeed");
    }

    @Test
    public void addShopManagerTest() {
        // 1) Owner setup
        String ownerToken = generateloginAsRegistered();
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
        String ownerToken = generateloginAsRegistered();
        ShopDTO shop = generateShopAndItems(ownerToken);

        String managerToken = userService.enterToSystem().getData();
        Response<Void> managerRegResp = userService.registerUser(
            managerToken, "manager", "pwdM", LocalDate.now().minusYears(30)
        );
        assertTrue(managerRegResp.isOk(), "Manager registration should succeed");
        // 2) manager logs in
        Response<String> ownerLoginResp = userService.loginUser(
            ownerToken, "manager", "pwdM"
        );

        Set<Permission> permissions = new HashSet<>();

        // 2) Owner adds a manager
        Response<Void> addManagerResp = shopService.addShopManager(
            ownerToken, shop.getId(), "manager", permissions
        );

        Response<String> res = shopService.getMembersPermissions(ownerToken, shop.getId());
        
        Response<Void> setPermissionsResp = shopService.addShopManagerPermission
        (
            ownerToken, shop.getId(), "manager", Permission.APPOINTMENT
        );

        assertNotEquals(res.getData(), shopService.getMembersPermissions(ownerToken, shop.getId()).getData());
        
    }

    @Test
    public void searchItemsWithPopulatedShop() {
        // 1) Enter as guest
        Response<String> guestResp = userService.enterToSystem();
        assertTrue(guestResp.isOk(), "enterToSystem should succeed");
        String guestToken = guestResp.getData();
        assertNotNull(guestToken, "Guest token must not be null");

        // 2) Register owner
        Response<Void> regResp = userService.registerUser(guestToken, "owner", "pwd", LocalDate.now().minusYears(30));
        assertTrue(regResp.isOk(), "Registration should succeed");

        // 3) Log in as owner
        Response<String> loginResp = userService.loginUser(guestToken, "owner", "pwd");
        assertTrue(loginResp.isOk(), "Owner login should succeed");
        String ownerToken = loginResp.getData();
        assertNotNull(ownerToken, "Owner token must not be null");

        // 4) Create the shop
        Response<ShopDTO> createShopResp = shopService.createShop(ownerToken, "MyShop", "desc");
        assertTrue(createShopResp.isOk(), "Shop creation should succeed");
        ShopDTO shop = createShopResp.getData();
        assertNotNull(shop, "ShopDTO must not be null");

        // 5) Add three items
        assertTrue(shopService.addItemToShop(ownerToken, shop.getId(), "Apple", Category.FOOD, 1.00, "fresh apple")
                .isOk(), "Adding Apple should succeed");
        assertTrue(shopService.addItemToShop(ownerToken, shop.getId(), "Banana", Category.FOOD, 0.50, "ripe banana")
                .isOk(), "Adding Banana should succeed");
        assertTrue(shopService.addItemToShop(ownerToken, shop.getId(), "Laptop", Category.ELECTRONICS, 999.99, "new laptop")
                .isOk(), "Adding Laptop should succeed");

        // 6) Filter by exact name "Apple"
        HashMap<String, String> nameFilter = new HashMap<>();
        nameFilter.put("name", "Apple");
        Response<List<ItemDTO>> nameFilterResp = shopService.filterItemsAllShops(nameFilter);
        assertTrue(nameFilterResp.isOk(), "Name filter call should succeed");
        List<ItemDTO> byName = nameFilterResp.getData();
        assertNotNull(byName, "Result list must not be null");
        assertEquals(1, byName.size(), "Only one item should match name 'Apple'");
        assertEquals("Apple", byName.get(0).getName());

        // 7) Filter by category FOOD (should yield 2 items)
        HashMap<String, String> catFilter = new HashMap<>();
        catFilter.put("category", "FOOD");
        Response<List<ItemDTO>> catFilterResp = shopService.filterItemsAllShops(catFilter);
        assertTrue(catFilterResp.isOk(), "Category filter call should succeed");
        List<ItemDTO> byCategory = catFilterResp.getData();
        assertNotNull(byCategory, "Result list must not be null");
        assertEquals(2, byCategory.size(), "Two items should match category FOOD");
    }
}