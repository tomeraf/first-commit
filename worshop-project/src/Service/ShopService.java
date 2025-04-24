package Service;

import java.security.Permission;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import Domain.Category;
import Domain.ItemDTO;
import Domain.Registered;
import Domain.Shop;
import Domain.Adapters_and_Interfaces.IAuthentication;
import Domain.DTOs.Order;
import Domain.DTOs.ShopDTO;
import Domain.DTOs.UserDTO;
import Domain.DomainServices.ManagementService;
import Domain.Repositories.IOrderRepository;
import Domain.Repositories.IShopRepository;
import Domain.Repositories.IUserRepository;
// import jdk.vm.ci.code.Register;

public class ShopService {
    
    private IUserRepository userRepository;
    private IShopRepository shopRepository;
    private IOrderRepository orderRepository; 
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
        if(!authenticationAdapter.validateToken(sessionToken)){
            System.out.println("Please log in or register to add items to the shop.");
            throw new RuntimeException("Please login.");
        }
        else {
            ShopDTO shopDto = this.shopRepository.getShopById(shopID);
            return shopDto;
        }
    }

    public void addItemToShop(String sessionToken, int shopID, String itemName, Category category, double itemPrice) {
        //need to add the Check if the user is logged in
        // If not, prompt to log in or register
        // If logged in, add the item to the shop with the provided details
        if(!authenticationAdapter.validateToken(sessionToken)){
            System.out.println("Please log in or register to add items to the shop.");
            return;
        }
        else {
            String userName = this.authenticationAdapter.getUsername(sessionToken);
            UserDTO userDto = this.userRepository.getUserByName(userName);
            Registered registeredUser = convertToObject(userDto);
            ShopDTO shopDto = this.shopRepository.getShopById(shopID);
            Shop shop = convertToObject(shopDto);
            this.managementService.addItemToShop(registeredUser, shop, itemName, category, itemPrice);
        }
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
        if(!authenticationAdapter.validateToken(sessionToken)){
            System.out.println("Please log in or register to add items to the shop.");
            return;
        }
        else {
            String userName = this.authenticationAdapter.getUsername(sessionToken);
            UserDTO userDto = this.userRepository.getUserByName(userName);
            Registered registeredUser = convertToObject(userDto);
            ShopDTO shopDto = this.shopRepository.getShopById(shopID);
            Shop shop = convertToObject(shopDto);
            managementService.updateItemQuantity(registeredUser, shop, itemID, newQuantity);
        }
    }
    public void rateShop(String sessionToken, int shopID, double rating) {
        // Check if the user is logged in
        // If not, prompt to log in or register
        // If logged in, change the item price in the shop with the provided details
        if(authenticationAdapter.validateToken(sessionToken)){
            String username=authenticationAdapter.getUsername(sessionToken);
            UserDTO user=userRepository.getUserByName(username);
            Registered registeredUser=convertToObject(user);
            ShopDTO shop=shopRepository.getShopById(shopID);
            Shop s=convertToObject(shop);
            managementService.updateItemPrice(registeredUser, s, itemID, newPrice);
        }
        // If logged in, rate the shop with the provided rating
        if(!authenticationAdapter.validateToken(sessionToken)){
            System.out.println("Please log in or register to add items to the shop.");
            return;
        }
        else {
            String userName = this.authenticationAdapter.getUsername(sessionToken);
            UserDTO userDto = this.userRepository.getUserByName(userName);
            ShopDTO shopDto = this.shopRepository.getShopById(shopID);
            Shop shop = convertToObject(shopDto);
            List<Order> orders = orderRepository.getOrdersByCustomerId(userDto.id);
            boolean canRate = false;
            for (Order order : orders) {
                List<ItemDTO> items = order.getItems();
                for(ItemDTO itemDto : items) {
                    if (itemDto.getShopId() == shopID) {
                        canRate = true;
                        break;
                    }
                }
            }
            if(canRate){
                shop.updateRating(rating);
            }   
        }
    }

    public void rateItem(String sessionToken, int itemID, double rating) {
        // Check if the user is logged in
        // If not, prompt to log in or register
        // If logged in, rate the item with the provided rating
        if(!authenticationAdapter.validateToken(sessionToken)){
            System.out.println("Please log in or register to add items to the shop.");
            return;
        }
        else {
            String userName = this.authenticationAdapter.getUsername(sessionToken);
            UserDTO userDto = this.userRepository.getUserByName(userName);
            Registered registeredUser = convertToObject(userDto);
            List<Order> orders = orderRepository.getOrdersByCustomerId(userDto.id);
            boolean canRate = false;
            int shopID = -1;
            for (Order order : orders) {
                List<ItemDTO> items = order.getItems();
                for(ItemDTO itemDto : items){
                    if (itemDto.getItemID() == itemID) {
                        canRate = true;
                        shopID = itemDto.getShopId();
                        break;
                    }
                }
            }
            if(canRate){
                ShopDTO shopDto = this.shopRepository.getShopById(shopID);
                Shop shop = convertToObject(shopDto);
                shop.getItems().get(itemID).updateRating(rating);
            }   
        }
    }
    public void submitBidOffer(String sessionToken, int itemID, double offerPrice) {
        // Check if the user is logged in
        // If not, prompt to log in or register
        // If logged in, submit a bid offer for the item with the provided details
    }
    public void updateDiscountType(String sessionToken, int shopID, String discountType) {
        // Check if the user is logged in
        // If not, prompt to log in or register
        // If logged in, update the discount type for the item in the shop with the provided details
        if(!authenticationAdapter.validateToken(sessionToken)){
            System.out.println("Please log in or register to add items to the shop.");
            return;
        }
        else {
            String userName = this.authenticationAdapter.getUsername(sessionToken);
            UserDTO userDto = this.userRepository.getUserByName(userName);
            Registered registeredUser = convertToObject(userDto);
            ShopDTO shopDto = this.shopRepository.getShopById(shopID);
            Shop shop = convertToObject(shopDto);
            this.managementService.updateDiscountType(registeredUser, shop, discountType);
        }
    }
    public void updatePurchaseType(String sessionToken, int shopID, String purchaseType) {
        // Check if the user is logged in
        // If not, prompt to log in or register
        // If logged in, update the purchase type for the item in the shop with the provided details
        if(!authenticationAdapter.validateToken(sessionToken)){
            System.out.println("Please log in or register to add items to the shop.");
            return;
        }
        else {
            String userName = this.authenticationAdapter.getUsername(sessionToken);
            UserDTO userDto = this.userRepository.getUserByName(userName);
            Registered registeredUser = convertToObject(userDto);
            ShopDTO shopDto = this.shopRepository.getShopById(shopID);
            Shop shop = convertToObject(shopDto);
            this.managementService.updatePurchaseType(registeredUser, shop, purchaseType);
        }
    }
    public void addShopOwner(String sessionToken, int shopID, String appointeeName) {
        if(!authenticationAdapter.validateToken(sessionToken)){
            System.out.println("Please log in or register to add items to the shop.");
            return;
        }
        else {
            String userName = this.authenticationAdapter.getUsername(sessionToken);
            UserDTO userDto = this.userRepository.getUserByName(userName);
            UserDTO appointee = this.userRepository.getUserByName(appointeeName);
            Registered registeredUser = convertToObject(userDto);
            Registered appointeeUser = convertToObject(appointee);
            ShopDTO shopDto = this.shopRepository.getShopById(shopID);
            Shop shop = convertToObject(shopDto);
            this.managementService.addOwner(registeredUser, shop, appointeeUser);
        }
    }
    public void addShopManager(String sessionToken, int shopID, String appointeeName) {
       if(authenticationAdapter.validateToken(sessionToken)){
            String username=authenticationAdapter.getUsername(sessionToken);
            UserDTO user=userRepository.getUserByName(username);
            Registered registeredUser=convertToObject(user);
            UserDTO appointee=userRepository.getUserByName(appointeeName);
            Registered appointeeUser=convertToObject(appointee);
            ShopDTO shop=shopRepository.getShopById(shopID);
            Shop s=convertToObject(shop);
            managementService.addManager(registeredUser, s, appointeeUser);
        }
    }
    public void removeShopOwner(String sessionToken, int shopID, String appointeeName) {
        if(authenticationAdapter.validateToken(sessionToken)){
            String username=authenticationAdapter.getUsername(sessionToken);
            UserDTO user=userRepository.getUserByName(username);
            Registered registeredUser=convertToObject(user);
            UserDTO appointee=userRepository.getUserByName(appointeeName);
            Registered appointeeUser=convertToObject(appointee);
            ShopDTO shop=shopRepository.getShopById(shopID);
            Shop s=convertToObject(shop);
            managementService.removeAppointment(registeredUser, s, appointeeUser);
        }
    }
    public void addShopManagerPermission(String sessionToken, int shopID,String appointeeName, Permission  permission) {
        if(authenticationAdapter.validateToken(sessionToken)){
            String username=authenticationAdapter.getUsername(sessionToken);
            UserDTO user=userRepository.getUserByName(username);
            Registered registeredUser=convertToObject(user);
            UserDTO appointee=userRepository.getUserByName(appointeeName);
            Registered appointeeUser=convertToObject(appointee);
            ShopDTO shop=shopRepository.getShopById(shopID);
            Shop s=convertToObject(shop);
            managementService.addPermission(registeredUser, s, appointeeUser, permission);
        }
    }
    
    public void removeShopManagerPermission(String sessionToken, int shopID,String appointeeName , Permission permission) {
        if(authenticationAdapter.validateToken(sessionToken)){
            String username=authenticationAdapter.getUsername(sessionToken);
            UserDTO user=userRepository.getUserByName(username);
            Registered registeredUser=convertToObject(user);
            UserDTO appointee=userRepository.getUserByName(appointeeName);
            Registered appointeeUser=convertToObject(appointee);
            ShopDTO shop=shopRepository.getShopById(shopID);
            Shop s=convertToObject(shop);
            managementService.removePermission(registeredUser, s, appointeeUser, permission);
        }
    }

    public void closeShopByFounder(String sessionToken, int shopID) {
        // Check if the user is logged in
        // If not, prompt to log in or register
        // If logged in, close the shop with the provided details
        if(authenticationAdapter.validateToken(sessionToken)){
            String username=authenticationAdapter.getUsername(sessionToken);
            UserDTO user=userRepository.getUserByName(username);
            Registered registeredUser=convertToObject(user);
            ShopDTO shop=shopRepository.getShopById(shopID);
            Shop s=convertToObject(shop);
            managementService.closeShop(registeredUser, s);
        }
    }
    public void getMembersPermissions(String sessionToken, int shopID) {
        if(authenticationAdapter.validateToken(sessionToken)){
            String username=authenticationAdapter.getUsername(sessionToken);
            UserDTO user=userRepository.getUserByName(username);
            Registered registeredUser=convertToObject(user);
            ShopDTO shop=shopRepository.getShopById(shopID);
            Shop s=convertToObject(shop);
            managementService.getMembersPermissions(registeredUser, s);
        }
    }
      
}
