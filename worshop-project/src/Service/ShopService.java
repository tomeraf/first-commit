package Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import Domain.ItemDTO;
import Domain.Registered;
import Domain.Shop;
import Domain.Adapters_and_Interfaces.IAuthentication;
import Domain.DTOs.ShopDTO;
import Domain.DTOs.UserDTO;
import Domain.DomainServices.ManagementService;
import Domain.Repositories.IShopRepository;
import Domain.Repositories.IUserRepository;

public class ShopService {
    
    private IUserRepository userRepository;
    private IShopRepository shopRepository;
    private ManagementService managementService = ManagementService.getInstance();
    private IAuthentication authenticationAdapter;
    private int shopIdCounter = 1;
    

    public ShopService(IUserRepository userRepository, IShopRepository shopRepository, IAuthentication authenticationAdapter) {
        this.userRepository = userRepository;
        this.shopRepository = shopRepository;
        this.authenticationAdapter = authenticationAdapter;

    }
    
    public List<ShopDTO> showAllShops() {
        return new ArrayList<ShopDTO>(shopRepository.getAllShops().values());
    }

    public List<ItemDTO> showShopItems(int shopId) {
        return new ArrayList<ItemDTO>(shopRepository.getShopById(shopId).getItems().values());
    }

    public List<ItemDTO> filterItemsAllShops(HashMap<String, String> filters){
        String category = filters.get("category");
        String name = filters.get("name");
        int minPrice = filters.get("minPrice") != null ? Integer.parseInt(filters.get("minPrice")) : 0;
        int maxPrice = filters.get("maxPrice") != null ? Integer.parseInt(filters.get("maxPrice")) : 0;
        int minRating = filters.get("minRating") != null ? Integer.parseInt(filters.get("minRating")) : 0;
        int shopRating = filters.get("shopRating") != null ? Integer.parseInt(filters.get("shopRating")) : 0;
        List<ItemDTO> filteredItems = new ArrayList<>();
        for (ShopDTO shop : shopRepository.getAllShops().values()) {
            Shop s = convertToObject(shop);
            filteredItems.add(s.filter(category, name, minPrice, maxPrice, minRating, shopRating));
        }
        return filteredItems;
    }

    public List<ItemDTO> filterItemsInShop(int shopId, HashMap<String, String> filters){
        String category = filters.get("category");
        String name = filters.get("name");
        int minPrice = filters.get("minPrice") != null ? Integer.parseInt(filters.get("minPrice")) : 0;
        int maxPrice = filters.get("maxPrice") != null ? Integer.parseInt(filters.get("maxPrice")) : 0;
        int minRating = filters.get("minRating") != null ? Integer.parseInt(filters.get("minRating")) : 0;
        List<ItemDTO> filteredItems = new ArrayList<>();
        ShopDTO shop = shopRepository.getShopById(shopId);
        Shop s = convertToObject(shop);
        filteredItems.add(s.filter(category, name, minPrice, maxPrice, minRating,null));
        return filteredItems;
    }

    public ShopDTO createShop(int userID, String name, String description) {
        ShopDTO shop = managementService.createShop(userRepository.getUserById(userID), name, description);
        shopRepository.addShop(shop);
        return shop;
    }

    public ShopDTO getShopInfo(String sessionToken, int shopID) {


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
        if(authenticationAdapter.validateToken(sessionToken)){
            String username=authenticationAdapter.getUsername(sessionToken);
            UserDTO user=userRepository.getUserByName(username);
            Registered registeredUser=convertToObject(user);
            ShopDTO shop=shopRepository.getShopById(shopID);
            Shop s=convertToObject(shop);
            managementService.removeItemFromShop(registeredUser, s, itemID);
        }
        
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
        // If logged in, change the item description in the shop with the provided details
    }

    
}
