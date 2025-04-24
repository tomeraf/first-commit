package Service;

import java.security.Permission;
import java.time.LocalDate;
import java.util.Locale.Category;
import java.util.Map;

import Domain.Registered;
import Domain.DTOs.UserDTO;
import Domain.DomainServices.ManagmentService;
import Domain.Repositories.IShopRepository;
import Domain.Repositories.IUserRepository;

public class UserService {
    private IShopRepository shopRepository;
    private IUserRepository userRepository;
    private ManagmentService managmentService;
    
    public void loginAsGuest() {

    }

    public void exitAsGuest() {

    }
    
    public void registerUser(String username, String password, LocalDate dateOfBirth) {
        if (userRepository.isUsernameExists(username)) {
            System.out.println("Username already exists");
            return;
        }
        //UserDTO newUser = new UserDTO(username, password, dateOfBirth);
        //userRepository.addUser(newUser);
    }

    
    public void loginUser(String username, String password) {
        // UserDTO user = userRepository.getUserByUsername(username);
        // if (user == null) {
        //     System.out.println("User not found");
        //     return;
        // }
        // if (!user.password.equals(password)) {
        //     System.out.println("Incorrect password");
        //     return;
        // }
        // Set the user as logged in (this could be a session management logic)
    }

    public void getShopInfo() {

    }

    public void searchItem(String itemName, Category category, String keyword, double minPrice, double maxPrice, double itemRate, double shopRate) {
    }

    public void searchItemInStore(int shopID, String itemName, Category category, String keyword, double minPrice, double maxPrice, double itemRate) {
    }

    
    public void addItemToCart(int shopID, Map<Integer, Integer> itemsIDs, String sessionToken) {
        // Check if the user is logged in
        // If not, prompt to log in or register
        // If logged in, add the item to the user's cart
        // Use the shopRepository to get the shop and item details
    }
    
    public void viewCart(String sessionToken, int cartID) {
        // Check if the user is logged in
        // If not, prompt to log in or register
        // If logged in, retrieve and display the cart items
    }
    
    public void removeItemFromCart(String sessionToken, int cardID, int itemID) {
        // Check if the user is logged in
        // If not, prompt to log in or register
        // If logged in, remove the item from the user's cart) {
    }

    public void buyCartContent(String sessionToken, int cartID) {
        // Check if the user is logged in
        // If not, prompt to log in or register
        // If logged in, proceed with the purchase of the cart content
    }

    public void logoutUser(String sessionToken, int cartID) {
        // Check if the user is logged in
        // If not, prompt to log in or register
        // If logged in, log out the user and clear the session token
    }

    public void openShop(String sessionToken, String shopName, String shopAddress) {
        // Check if the user is logged in
        // If not, prompt to log in or register
        // If logged in, open a new shop with the provided details
    }

    public void rateShop(String sessionToken, int shopID, double rating) {
        // Check if the user is logged in
        // If not, prompt to log in or register
        // If logged in, rate the shop with the provided rating
    }
    public void rateItem(String sessionToken, int itemID, double rating) {
        // Check if the user is logged in
        // If not, prompt to log in or register
        // If logged in, rate the item with the provided rating
    }
    public void sendMessageToShop(String sessionToken, int shopID, String message) {
        // Check if the user is logged in
        // If not, prompt to log in or register
        // If logged in, send a message to the shop with the provided details
    }
    public void viewOrderHistory(String sessionToken) {
        // Check if the user is logged in
        // If not, prompt to log in or register
        // If logged in, retrieve and display the order history
    }
    public void submitBidOffer(String sessionToken, int itemID, double offerPrice) {
        // Check if the user is logged in
        // If not, prompt to log in or register
        // If logged in, submit a bid offer for the item with the provided details
    }
    public void directPurchase(String sessionToken, int itemID) {
        // Check if the user is logged in
        // If not, prompt to log in or register
        // If logged in, proceed with the direct purchase of the item with the provided details
    }
    public void addItemToShop(String sessionToken, int shopID, String itemName, String itemDescription, double itemPrice) {
        // Check if the user is logged in
        // If not, prompt to log in or register
        // If logged in, add the item to the shop with the provided details
    }
    public void removeItemFromShop(String sessionToken, int shopID, int itemID) {
        // Check if the user is logged in
        // If not, prompt to log in or register
        // If logged in, remove the item from the shop with the provided details
    }
    public void changeItemQuantityInShop(String sessionToken, int shopID, int itemID, int newQuantity) {
        // Check if the user is logged in
        // If not, prompt to log in or register
        // If logged in, change the item quantity in the shop with the provided details
    }
    public void changeItemPriceInShop(String sessionToken, int shopID, int itemID, double newPrice) {
        // Check if the user is logged in
        // If not, prompt to log in or register
        // If logged in, change the item price in the shop with the provided details
    }
    public void changeItemDescriptionInShop(String sessionToken, int shopID, int itemID, String newDescription) {
        // Check if the user is logged in
        // If not, prompt to log in or register
        // If logged in, change the item description in the shop with the provided details
    }
    public void updateDiscountType(String sessionToken, int shopID, String discountType) {
        // Check if the user is logged in
        // If not, prompt to log in or register
        // If logged in, update the discount type for the item in the shop with the provided details
    }
    public void updatePurchaseType(String sessionToken, int shopID, String purchaseType) {
        // Check if the user is logged in
        // If not, prompt to log in or register
        // If logged in, update the purchase type for the item in the shop with the provided details
    }
    
    public void addShopOwner(String sessionToken, int shopID, int appointeeID) {
        //UserDTO appointer = userRepository.getUserById(appointerID);
        
        //UserDTO appointee = userRepository.getUserById(appointeeID);


        //Shop shop = shopRepository.getShop(shopID);
        //managmentService.addOwner(userRepository.getUser(appointerID), shopRepository.getShop(shopID), userRepository.getUser(appointeeID));
    }
    public void addShopManager(String sessionToken, int shopID, int appointeeID) {
        //UserDTO appointer = userRepository.getUserById(appointerID
    }
    public void removeShopOwner(String sessionToken, int shopID, int appointeeID) {
        //UserDTO appointer = userRepository.getUserById(appointerID);
        
        //UserDTO appointee = userRepository.getUserById(appointeeID
    }
    public void setShopManagerPermission(String sessionToken, int shopID, int appointeeID, String permission) {
    
        //UserDTO appointer = userRepository.getUserById(appointerID  
    } 
    public void closeShopByFounder(String sessionToken, int shopID) {
        //UserDTO appointer = userRepository.getUserById(appointerID);
        
        //Shop shop = shopRepository.getShop(shopID);
        //managmentService.closeShop(userRepository.getUser(appointerID), shopRepository.getShop(shopID));
    }
    public void getMembersPermissions(String sessionToken, int shopID) {
        //UserDTO appointer = userRepository.getUserById(appointerID);
        
        //Shop shop = shopRepository.getShop(shopID);
        //managmentService.getMembersPermissions(userRepository.getUser(appointerID), shopRepository.getShop(shopID));
    }
    public void shopOwnerRespondsToMessage(String sessionToken, int shopID, String message) {
        //UserDTO appointer = userRepository.getUserById(appointerID
    }
    
    // The owner calls
    public void shopPurchaseHistory(String sessionToken, int shopID) {
        //UserDTO appointer = userRepository.getUserById(appointerID
    }
}