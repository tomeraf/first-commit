package Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;

import Domain.DomainServices.ManagementService;

import Domain.Guest;
import Domain.Item;
import Domain.Registered;
import Domain.Shop;
import Domain.ShoppingCart;
import Domain.Adapters_and_Interfaces.IAuthentication;
import Domain.Adapters_and_Interfaces.IPayment;
import Domain.Adapters_and_Interfaces.IShipment;
import Domain.Adapters_and_Interfaces.JWTAdapter;
import Domain.DTOs.ItemDTO;
import Domain.DTOs.Order;
import Domain.DomainServices.PurchaseService;
import Domain.Repositories.IOrderRepository;
import Domain.Repositories.IShopRepository;
import Domain.Repositories.IUserRepository;

public class UserService {

    //private JWTAdapter jwtAdapter = new JWTAdapter();
    private IUserRepository userRepository;
    private IShopRepository shopRepository;
    private IOrderRepository orderRepository;
    private IAuthentication jwtAdapter;
    private IPayment payment;
    private IShipment shipment;

    private PurchaseService purchaseService = new PurchaseService();
    ObjectMapper objectMapper = new ObjectMapper();
    
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    public UserService(IUserRepository userRepository, IShopRepository shopRepository, IOrderRepository orderRepository, IAuthentication jwtAdapter, IPayment payment, IShipment shipment) {
        this.userRepository = userRepository;
        this.shopRepository = shopRepository;
        this.orderRepository = orderRepository;
        this.payment = payment;
        this.shipment = shipment;
        this.jwtAdapter = jwtAdapter;
    }
    
    /**
     * Enters the system as a guest, generates a session token, and persists the user.
     *
     * @return the newly generated session token for the guest
     */
    public String enterToSystem() {
        logger.info(() -> "User entered the system");
        int guestUserID = userRepository.getIdToAssign(); // Get a unique ID for the guest user
        Guest guest = new Guest();

        String sessionToken = jwtAdapter.generateToken(guestUserID+"");
        
        guest.enterToSystem(sessionToken, guestUserID);
        userRepository.saveUser(guest); // Save the guest user in the repository
        return sessionToken;
    }

    /**
     * Exits a guest session by validating and removing the guest from the repository.
     *
     * @param sessionToken the token of the guest session to terminate
     */
    public void exitAsGuest(String sessionToken) {
        try {
            if (!jwtAdapter.validateToken(sessionToken)) {
                throw new Exception("User is not logged in");
            }
            int userID = Integer.parseInt(jwtAdapter.getUsername(sessionToken));
            userRepository.removeGuestById(userID); // Adds to the "reuse" list
            logger.info(() -> "User exited the system");
        } catch (Exception e) {
            logger.error(() -> "Error exiting the system: " + e.getMessage());
        }
    }

    /**
     * Logs out a registered user, converts back to a guest session, and returns a new token.
     *
     * @param sessionToken the current token of the registered user
     * @return a new session token as a guest, or empty string on failure
     */
    public String logoutRegistered(String sessionToken) {
        try {
            if (!jwtAdapter.validateToken(sessionToken)) {
                throw new Exception("User is not logged in");
            }
            // THIS IS REGISTERED USER - NOT GUEST
            Guest user = userRepository.getUserById(Integer.parseInt(jwtAdapter.getUsername(sessionToken)));
            user.logout();
            // Nothing to do, everything is saved in DB
            return enterToSystem();
        } catch (Exception e) {
            logger.error(() -> "Logout Error: " + e.getMessage());
            return "";
        }
    }

    /**
     * Registers a new user using the provided credentials and date of birth.
     * The guest keeps the same session token and is upgraded to Registered.
     *
     * @param sessionToken the current guest session token
     * @param username desired username
     * @param password desired password
     * @param dateOfBirth user's date of birth
     */
    public void registerUser(String sessionToken, String username, String password, LocalDate dateOfBirth) {
        try {
            if (!jwtAdapter.validateToken(sessionToken)) {
                throw new Exception("User is not logged in");
            }
            int idToAssign = userRepository.getIdToAssign();
            Guest guest = new Guest();
            
            guest.setSessionToken(sessionToken); // Set the session token for the guest
            guest.setCart(new ShoppingCart(idToAssign));
            // Stays with same token
            Registered registered = guest.register(username, password, dateOfBirth);
            userRepository.saveUser(registered);
        } catch (Exception e) {
            logger.error(() -> "Error registering user: " + e.getMessage());
        }
    }

    /**
     * Authenticates and logs in a registered user, issuing a new session token.
     *
     * @param sessionToken current guest session token
     * @param username registered user's username
     * @param password registered user's password
     * @return the new session token if login succeeds, or null on failure
     */
    public String loginUser(String sessionToken, String username, String password) {
        try {
            if (!jwtAdapter.validateToken(sessionToken)) {
                throw new Exception("User is not logged in");
            }

            Registered registered = userRepository.getUserByName(username);
            
            //should throw exception if user not found in the repository
            if (!registered.getPassword().equals(password)) {
                throw new Exception("Username and password do not match");
            }
            registered.setSessionToken(sessionToken); // Set the session token for the registered user
            int guestUserID = Integer.parseInt(jwtAdapter.getUsername(sessionToken));
            String newSessionToken = jwtAdapter.generateToken(guestUserID+"");
            userRepository.removeGuestById(guestUserID);
            
            logger.info(() -> "User logged in successfully with session token: " + sessionToken);
            return newSessionToken;
        } catch (Exception e) {
            logger.error(() -> "Error logging in user: " + e.getMessage());
            return null;
        }
    }

    /**
     * Retrieves the contents of the user's shopping cart.
     *
     * @param sessionToken current session token
     * @return list of ItemDTOs in the cart, or null on error
     */
    public List<ItemDTO> checkCartContent(String sessionToken) {
        try {
            if (!jwtAdapter.validateToken(sessionToken)) {
                throw new Exception("User not logged in");
            }
            int userID = Integer.parseInt(jwtAdapter.getUsername(sessionToken));
            Guest guest = userRepository.getUserById(userID);
            List<Shop> shops = new ArrayList<>();
            for (int i = 0; i < guest.getCart().getBaskets().size(); i++) {
                int shopID = guest.getCart().getBaskets().get(i).getShopID();
                Shop shop = shopRepository.getShopById(shopID);
                shops.add(shop);
            }
            List<ItemDTO> itemDTOs = purchaseService.checkCartContent(guest);

            logger.info(() -> "All items were listed successfully");
            return itemDTOs;
        } 
        catch (Exception e) {
            logger.error(() -> "Error viewing cart: " + e.getMessage());
            return null;
        }
    }

    /**
     * Adds items to the user's shopping cart.
     *
     * @param sessionToken current session token
     * @param itemDTOs list of items to add
     */
    public void addItemsToCart(String sessionToken, List<ItemDTO> itemDTOs) {
        try {
            if (!jwtAdapter.validateToken(sessionToken)) {
                throw new Exception("User not logged in");
            }
            int cartID = Integer.parseInt(jwtAdapter.getUsername(sessionToken));
            Guest guest = userRepository.getUserById(cartID);
            List<Item> items = itemDTOs.stream()
                    .map(itemDTO -> new Item(itemDTO.getName(), itemDTO.getCategory(), itemDTO.getPrice(), itemDTO.getShopId(), itemDTO.getItemID(), itemDTO.getDescription()))
                    .toList();

            purchaseService.addItemsToCart(guest, items);

            logger.info(() -> "Items added to cart successfully");
        } catch (Exception e) {
            logger.error(() -> "Error adding items to cart: " + e.getMessage());
        }
    }

    /**
     * Removes items from the user's shopping cart.
     *
     * @param sessionToken current session token
     * @param itemDTOs list of items to remove
     */
    public void removeItemsFromCart(String sessionToken, List<ItemDTO> itemDTOs) {
        try {
            if (!jwtAdapter.validateToken(sessionToken)) {
                throw new Exception("User not logged in");
            }
            int cartID = Integer.parseInt(jwtAdapter.getUsername(sessionToken));
            Guest guest = userRepository.getUserById(cartID); 
            List<Item> items = itemDTOs.stream()
                    .map(itemDTO -> new Item(itemDTO.getName(), itemDTO.getCategory(), itemDTO.getPrice(), itemDTO.getShopId(), itemDTO.getItemID(), itemDTO.getDescription()))
                    .toList(); 

            purchaseService.removeItemsFromCart(guest, items);
            
            logger.info(() -> "Items removed from cart successfully");
        } catch (Exception e) {
            logger.error(() -> "Error removing items from cart: " + e.getMessage());
        }
    }

    /**
     * Executes purchase of all items in the cart, creates and records an Order.
     *
     * @param sessionToken current session token
     * @return the created Order, or null on failure
     */
    public Order buyCartContent(String sessionToken) {
        try {
            if (!jwtAdapter.validateToken(sessionToken)) {
                throw new Exception("User not logged in");
            }
            int cartID = Integer.parseInt(jwtAdapter.getUsername(sessionToken));
            Guest guest = userRepository.getUserById(cartID); 
            List<Shop> shops = new ArrayList<>();
            for (int i = 0; i < guest.getCart().getBaskets().size(); i++) {
                int shopID = guest.getCart().getBaskets().get(i).getShopID();
                Shop shop = shopRepository.getShopById(shopID); 
                shops.add(shop); // Add the shop to the list of shops
            }
            
            Order order = purchaseService.buyCartContent(guest, shops, shipment, payment);
            orderRepository.addOrder(order); 
            logger.info(() -> "Purchase completed successfully for cart ID: " + cartID);
            return order; 
        } catch (Exception e) {
            logger.error(() -> "Error buying cart content: " + e.getMessage());
        }
        return null;
    }

    /**
     * Submits a bid offer for a specific item.
     *
     * @param sessionToken current session token
     * @param itemID the item to bid on
     * @param offerPrice the bid amount
     */
    public void submitBidOffer(String sessionToken, int itemID, double offerPrice) {
        try {
            if (!jwtAdapter.validateToken(sessionToken)) {
                throw new Exception("User not logged in");
            }
            int cartID = Integer.parseInt(jwtAdapter.getUsername(sessionToken));
            Guest guest = userRepository.getUserById(cartID); // Get the guest user by I
            purchaseService.submitBidOffer(guest, itemID, offerPrice);

            logger.info(() -> "Bid offer submitted successfully for item ID: " + itemID);
        } catch (Exception e) {
            logger.error(() -> "Error submitting bid offer: " + e.getMessage());
        }
    }

    /**
     * Performs a direct purchase of a single item.
     *
     * @param sessionToken current session token
     * @param itemID the item to purchase
     */
    public void directPurchase(String sessionToken, int itemID) {
        try {
            if (!jwtAdapter.validateToken(sessionToken)) {
                throw new Exception("User not logged in");
            }
            int cartID = Integer.parseInt(jwtAdapter.getUsername(sessionToken));
            Guest guest = userRepository.getUserById(cartID); // Get the guest user by I
            purchaseService.directPurchase(guest, itemID);

            logger.info(() -> "Direct purchase completed successfully for item ID: " + itemID);
        } catch (Exception e) {
            logger.error(() -> "Error completing direct purchase: " + e.getMessage());
        }
    }

    /**
     * Retrieves the personal order history for the user.
     *
     * @param sessionToken current session token
     * @return list of past Orders, or null on error
     */
    public List<Order> viewPersonalOrderHistory(String sessionToken) {
        try {
            if (!jwtAdapter.validateToken(sessionToken)) {
                throw new Exception("User not logged in");
            }
            int userId = Integer.parseInt(jwtAdapter.getUsername(sessionToken));
            List<Order> orders = orderRepository.getOrdersByCustomerId(userId);
            logger.info(() -> "Personal search history viewed successfully for user ID: " + userId);
            return orders;
        } catch (Exception e) {
            logger.error(() -> "Error viewing personal search history: " + e.getMessage());
            return null;
        }
    }
}