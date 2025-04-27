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
import Domain.Adapters_and_Interfaces.JWTAdapter;
import Domain.DTOs.ItemDTO;
import Domain.DTOs.Order;
import Domain.DomainServices.PurchaseService;
import Domain.Repositories.IOrderRepository;
import Domain.Repositories.IShopRepository;
import Domain.Repositories.IUserRepository;

public class UserService {

    private JWTAdapter jwtAdapter = new JWTAdapter();
    private IUserRepository userRepository;
    private IShopRepository shopRepository;
    private IOrderRepository orderRepository;
    
    private PurchaseService purchaseService = new PurchaseService();
    ObjectMapper objectMapper = new ObjectMapper();
    
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    public UserService(IUserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    public String enterToSystem() {
        logger.info(() -> "User entered the system");
        int guestUserID = userRepository.getIdToAssign(); // Get a unique ID for the guest user
        Guest guest = new Guest();

        String sessionToken = jwtAdapter.generateToken(guestUserID+"");
        
        guest.enterToSystem(sessionToken, guestUserID);
        userRepository.saveUser(guest); // Save the guest user in the repository
        return sessionToken;
    }

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

    // After logout - the user remains in the system, as guest
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

    // Returns session token
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
            return sessionToken;
        } catch (Exception e) {
            logger.error(() -> "Error logging in user: " + e.getMessage());
            return null;
        }
    }

    public List<ItemDTO> checkCartContent(String sessionToken) {
        try {
            if (!jwtAdapter.validateToken(sessionToken)) {
                throw new Exception("User not logged in");
            }
            int userID = Integer.parseInt(jwtAdapter.getUsername(sessionToken));
            Guest guest = userRepository.getUserById(userID); // Get the guest user by I
            List<Shop> shops = new ArrayList<>();
            for (int i = 0; i < guest.getCart().getBaskets().size(); i++) {
                int shopID = guest.getCart().getBaskets().get(i).getShopID();
                Shop shop = shopRepository.getShopById(shopID); // Get the shop by ID
                shops.add(shop); // Add the shop to the list of shops
            }
            List<ItemDTO> itemDTOs = purchaseService.checkCartContent(guest);
            // List<ItemDTO> itemDTOs = items.stream()
            //         .map(item -> new ItemDTO(item.getName(), item.getCategory(), item.getPrice(), item.getShopId(), item.getId(), item.getQuantity(), item.getRating()))
            //         .toList(); // Convert Item to ItemDTO

            logger.info(() -> "All items were listed successfully");
            return itemDTOs; // Check the cart content
        } 
        catch (Exception e) {
            logger.error(() -> "Error viewing cart: " + e.getMessage());
            return null;
        }
    }

    public void addItemsToCart(String sessionToken, List<ItemDTO> itemDTOs) {
        try {
            if (!jwtAdapter.validateToken(sessionToken)) {
                throw new Exception("User not logged in");
            }
            int cartID = Integer.parseInt(jwtAdapter.getUsername(sessionToken));
            Guest guest = userRepository.getUserById(cartID); // Get the guest user by I
            List<Item> items = itemDTOs.stream()
                    .map(itemDTO -> new Item(itemDTO.getName(), itemDTO.getCategory(), itemDTO.getPrice(), itemDTO.getShopId(), itemDTO.getItemID(), itemDTO.getDescription()))
                    .toList(); // Convert ItemDTO to Item

            purchaseService.addItemsToCart(guest, items); // Add items to the cart

            logger.info(() -> "Items added to cart successfully");
        } catch (Exception e) {
            logger.error(() -> "Error adding items to cart: " + e.getMessage());
        }
    }

    public void removeItemsFromCart(String sessionToken, List<ItemDTO> itemDTOs) {
        try {
            if (!jwtAdapter.validateToken(sessionToken)) {
                throw new Exception("User not logged in");
            }
            int cartID = Integer.parseInt(jwtAdapter.getUsername(sessionToken));
            Guest guest = userRepository.getUserById(cartID); // Get the guest user by I
            List<Item> items = itemDTOs.stream()
                    .map(itemDTO -> new Item(itemDTO.getName(), itemDTO.getCategory(), itemDTO.getPrice(), itemDTO.getShopId(), itemDTO.getItemID(), itemDTO.getDescription()))
                    .toList(); // Convert ItemDTO to Item

            purchaseService.removeItemsFromCart(guest, items); // Save the updated cart to the repository
            
            logger.info(() -> "Items removed from cart successfully");
        } catch (Exception e) {
            logger.error(() -> "Error removing items from cart: " + e.getMessage());
        }
    }

    public Order buyCartContent(String sessionToken) {
        try {
            if (!jwtAdapter.validateToken(sessionToken)) {
                throw new Exception("User not logged in");
            }
            int cartID = Integer.parseInt(jwtAdapter.getUsername(sessionToken));
            Guest guest = userRepository.getUserById(cartID); // Get the guest user by I
            List<Shop> shops = new ArrayList<>();
            for (int i = 0; i < guest.getCart().getBaskets().size(); i++) {
                int shopID = guest.getCart().getBaskets().get(i).getShopID();
                Shop shop = shopRepository.getShopById(shopID); // Get the shop by ID
                shops.add(shop); // Add the shop to the list of shops
            }
            
            Order order = purchaseService.buyCartContent(guest, shops); // Buy the cart content
            orderRepository.addOrder(order); // Save the order to the repository
            logger.info(() -> "Purchase completed successfully for cart ID: " + cartID);
            return order; // Return the order details
        } catch (Exception e) {
            logger.error(() -> "Error buying cart content: " + e.getMessage());
        }
        return null;
    }

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

    public List<Order> viewPersonalSearchHistory(String sessionToken) {
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