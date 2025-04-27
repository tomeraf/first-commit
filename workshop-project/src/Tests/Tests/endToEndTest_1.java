package Tests;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import Service.ShopService;
import Service.UserService;
import Domain.Guest;
import Domain.Manager;
import Domain.Owner;
import Domain.Registered;
import Domain.Shop;
import Domain.DTOs.ItemDTO;
import Domain.DTOs.ShopDTO;
import Domain.Category;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
    
    
    public endToEndTest_1() {
        shopRepository = new MemoryShopRepository();
        userRepository = new MemoryUserRepository();
        orderRepository = new MemoryOrderRepository();
        jwtAdapter = new JWTAdapter();
        shipment = new ProxyShipment();
        payment = new ProxyPayment();

        userService = new UserService(userRepository, shopRepository, orderRepository, jwtAdapter, payment, shipment);
        shopService = new ShopService(userRepository, shopRepository, orderRepository, jwtAdapter);
    }

    @Test
    public void successfullGuestLogin() {
        String guestToken = userService.enterToSystem();
        assertNotNull(guestToken, "Guest login failed: token is null.");
        List<ItemDTO> items = userService.checkCartContent(guestToken);
        assertNotNull(items, "Guest login failed: cart content is null.");
        assertTrue(items.isEmpty(), "Guest login failed: cart is not empty.");
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