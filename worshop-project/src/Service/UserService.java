package Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.security.Permission;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale.Category;

import java.util.Map;

import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;

import Domain.Guest;
import Domain.Registered;
import Domain.ShoppingCart;
import Domain.Adapters_and_Interfaces.JWTAdapter;
import Domain.DTOs.ItemDTO;
import Domain.DTOs.UserDTO;
import Domain.DomainServices.ManagmentService;
import Domain.DomainServices.PurchaseService;
import Domain.Purchase.Purchase;
import Domain.Repositories.IShopRepository;
import Domain.Repositories.IUserRepository;

public class UserService {

    private JWTAdapter jwtAdapter = new JWTAdapter();
    private IUserRepository userRepository;
    private ManagmentService managmentService = new ManagmentService();
    private PurchaseService purchaseService = new PurchaseService();
    ObjectMapper objectMapper = new ObjectMapper();
    
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    public UserService(IUserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    public String enterToSystem() {
        logger.info(() -> "User entered the system");
        Guest guest = new Guest();
        // Shopping cart is created here, it seems redundant but then we need it to get the ID of the cart(UserID == CartID)
        guest.enterToSystem(userRepository.getIdToAssign()); 
        return jwtAdapter.generateToken(guest.getUserID()+"");
    }

    public void exitAsGuest(String sessionToken) {
        try {
            if (!jwtAdapter.validateToken(sessionToken)) {
                throw new Exception("User is not logged in");
            }
            int userID = Integer.parseInt(jwtAdapter.getUsername(sessionToken));
            userRepository.removedCartContent(userID); // User' ID and its cart's ID are same - it goes to the table <cartID, itemID> and removes
            userRepository.removedId(userID); // Adds to the "reuse" list
            logger.info(() -> "User exited the system");
        } catch (Exception e) {
            logger.error(() -> "Error exiting the system: " + e.getMessage());
        }
    }

    // After logout - the user remains in the system, as guest
    public String logout(String sessionToken) {
        try {
            if (!jwtAdapter.validateToken(sessionToken)) {
                throw new Exception("User is not logged in");
            }
            // Nothing to do, everything is saved in DB
            return enterToSystem();
        } catch (Exception e) {
            logger.error(() -> "Logout Error: " + e.getMessage());
            return "";
        }
    }

    public void exitAsRegistered(String sessionToken) {
        try {
            if (!jwtAdapter.validateToken(sessionToken)) {
                throw new Exception("User is not logged in");
            }
            String guestSessionToken = logout(sessionToken); // Makes the registered to be guest
            exitAsGuest(guestSessionToken); // and the exit as guest
            
            // I don't know if isInSession is going to be stored in the DB, currently, everything remains
            
            logger.info(() -> "User exited the system");
        } catch (Exception e) {
            logger.error(() -> "Error exiting the system: " + e.getMessage());
        }
    }

    public void registerUser(String sessionToken, String username, String password, LocalDate dateOfBirth) {
        try {
            if (!jwtAdapter.validateToken(sessionToken)) {
                throw new Exception("User is not logged in");
            }
            int idToAssign = userRepository.getIdToAssign();
            if (userRepository.getUserById(idToAssign) != null) {
                throw new Exception("Username already exists");
            }
            int currentGuestUserID = Integer.parseInt(jwtAdapter.getUsername(sessionToken));
            ShoppingCart cart = userRepository.getShoppingCart(currentGuestUserID);
            Guest guest = new Guest();
            
            // + MUST MODIFY GUEST TO HAVE SESSION_TOKEN INSTEAD OF "IS_IN_SESSION"
            guest.setSessionToken(sessionToken); // Set the session token for the guest
            guest.setCart(cart);
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

            int guestUserID = Integer.parseInt(jwtAdapter.getUsername(sessionToken));
            String newSessionToken = jwtAdapter.generateToken(guestUserID+"");
            
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
            int cartID = Integer.parseInt(jwtAdapter.getUsername(sessionToken));
            ShoppingCart cart = userRepository.getShoppingCart(cartID);
            List<ItemDTO> itemList = cart.getItems(); // Get all items in the cart
            
            logger.info(() -> "All items were listed successfully");
            return itemList;
        } 
        catch (Exception e) {
            logger.error(() -> "Error viewing cart: " + e.getMessage());
            return null;
        }
    }

    public void addItemsToCart(String sessionToken, List<ItemDTO> items) {
        try {
            if (!jwtAdapter.validateToken(sessionToken)) {
                throw new Exception("User not logged in");
            }
            int cartID = Integer.parseInt(jwtAdapter.getUsername(sessionToken));
            //ShoppingCart cart = userRepository.getShoppingCart(cartID);
            //boolean success = cart.addItems(items); // Add items to the cart

            List<Integer> itemIDs = items.stream().map(ItemDTO::getItemID).toList(); // Extract item IDs from the list of items
            List<Integer> shopIDs = items.stream().map(ItemDTO::getShopId).toList(); // Extract shop IDs from the list of items
            
            userRepository.addItemsToCart(cartID, itemIDs, shopIDs); // Save the updated cart to the repository
            
            logger.info(() -> "Items added to cart successfully");
        } catch (Exception e) {
            logger.error(() -> "Error adding items to cart: " + e.getMessage());
        }
    }

    public void removeItemsFromCart(String sessionToken, List<ItemDTO> items) {
        try {
            if (!jwtAdapter.validateToken(sessionToken)) {
                throw new Exception("User not logged in");
            }
            int cartID = Integer.parseInt(jwtAdapter.getUsername(sessionToken));
            //ShoppingCart cart = userRepository.getShoppingCart(cartID);
            //boolean success = cart.removeItems(items); // Remove items from the cart
            List<Integer> itemIDs = items.stream().map(ItemDTO::getItemID).toList(); // Extract item IDs from the list of items
            List<Integer> shopIDs = items.stream().map(ItemDTO::getShopId).toList(); // Extract shop IDs from the list of items
            
            userRepository.removeItemsFromCart(cartID, itemIDs, shopIDs); // Save the updated cart to the repository
            
            logger.info(() -> "Items removed from cart successfully");
        } catch (Exception e) {
            logger.error(() -> "Error removing items from cart: " + e.getMessage());
        }
    }

    public void buyCartContent(String sessionToken) {
        try {
            if (!jwtAdapter.validateToken(sessionToken)) {
                throw new Exception("User not logged in");
            }
            int cartID = Integer.parseInt(jwtAdapter.getUsername(sessionToken));
            ShoppingCart cart = userRepository.getShoppingCart(cartID);

            //purchaseService.buyCartContent(registered.getCart(), cartID);
            
            removeItemsFromCart(sessionToken, cart.getItems()); // Remove items from the cart after purchase

            logger.info(() -> "Purchase completed successfully for cart ID: " + cartID);
        } catch (Exception e) {
            logger.error(() -> "Error buying cart content: " + e.getMessage());
        }
    }
}