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
        guest.enterToSystem(userRepository.getTotalUsersCount()); 
        userRepository.incTotalUsersCount();
        return jwtAdapter.generateToken(guest.getCart().getCartID()+"");

    }

    public void exit(String sessionToken) {
        try {
            if (!jwtAdapter.validateToken(sessionToken)) {
                throw new Exception("User not logged in");
            }
            String username = jwtAdapter.getUsername(sessionToken);
            Guest guest = new Guest();
            registered.logout();
        } catch (Exception e) {
            logger.error(() -> "Error exiting the system: " + e.getMessage());
        }
        logger.info(() -> "User exited the system");
        userRepository.decGuestsCount();

    }
    
    public void registerUser(String username, String password, LocalDate dateOfBirth) {
        try {
            if (userRepository.getUserByName(username) != null) {
                throw new Exception("Username already exists");
            }
            Guest guest = new Guest();
            Registered registered = guest.register(username, password, dateOfBirth);
            userRepository.saveUser(registered);
        } catch (Exception e) {
            logger.error(() -> "Error registering user: " + e.getMessage());
        }

    }
    //return session token
    public String loginUser(String username, String password) {
        try {
            Registered registered = userRepository.getUserByName(username);
            //should throw exception if user not found in the repository
            if (!registered.getPassword().equals(password)) {
                throw new Exception("Username and password do not match");
            }
            String sessionToken = jwtAdapter.generateToken(username);
            userRepository.decGuestsCount();
            logger.info(() -> "User logged in successfully with session token: " + sessionToken);
            return sessionToken;
        } catch (Exception e) {
            logger.error(() -> "Error logging in user: " + e.getMessage());
        }
        return null;
    }
    
    public List<ItemDTO> viewCart(String sessionToken) {
        try {
            if (!jwtAdapter.validateToken(sessionToken)) {
                throw new Exception("User not logged in");
            }
            String username = jwtAdapter.getUsername(sessionToken);
            Registered registered = userRepository.getUserByName(username);
            return registered.getCart().getItems(); 
        } 
        catch (Exception e) {
            logger.error(() -> "Error viewing cart: " + e.getMessage());
        }

    }
    
    public void buyCartContent(String sessionToken, int cartID) {
        try {
            if (!jwtAdapter.validateToken(sessionToken)) {
                throw new Exception("User not logged in");
            }
            String username = jwtAdapter.getUsername(sessionToken);
            Registered registered = userRepository.getUserByName(username);
            //purchaseService.buyCartContent(registered.getCart(), cartID);
            logger.info(() -> "Purchase completed successfully for cart ID: " + cartID);
        } catch (Exception e) {
            logger.error(() -> "Error buying cart content: " + e.getMessage());
        }
        
    }

    public void logoutUser(String sessionToken) {
        try {
            if (!jwtAdapter.validateToken(sessionToken)) {
                throw new Exception("User not logged in");
            }
            String username = jwtAdapter.getUsername(sessionToken);
            Registered registered = userRepository.getUserByName(username);
            registered.logout();
            logger.info(() -> "User logged out successfully");
        } catch (Exception e) {
            logger.error(() -> "Error logging out user: " + e.getMessage());
        }
    }

    public void sendMessageToShop(String sessionToken, int shopID, String message) {
        // Check if the user is logged in
        // If not, prompt to log in or register
        // If logged in, send a message to the shop with the provided details
    }
    public void shopOwnerRespondsToMessage(String sessionToken, int shopID, String message) {
        //UserDTO appointer = userRepository.getUserById(appointerID
    }
    
    //     public void shopPurchaseHistory(String sessionToken, int shopID) {
    //     //UserDTO appointer = userRepository.getUserById(appointerID
    // }
}