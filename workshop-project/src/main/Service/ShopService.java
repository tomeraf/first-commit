package Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import Domain.Category;
import Domain.Item;
import Domain.DTOs.ItemDTO;
import Domain.Registered;
import Domain.Shop;
import Domain.Adapters_and_Interfaces.IAuthentication;
import Domain.Adapters_and_Interfaces.IMessage;
import Domain.DTOs.Order;
import Domain.DTOs.ShopDTO;

import Domain.DTOs.UserDTO;
import Domain.DomainServices.InteractionService;

import Domain.DomainServices.ManagementService;
import Domain.DomainServices.ShoppingService;
import Domain.Repositories.IOrderRepository;
import Domain.Repositories.IShopRepository;
import Domain.Repositories.IUserRepository;
import Domain.Permission;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ShopService {
    
    private IUserRepository userRepository;
    private IShopRepository shopRepository;
    private IOrderRepository orderRepository; 
    private ManagementService managementService = ManagementService.getInstance();
    private ShoppingService shoppingService;
    private IAuthentication authenticationAdapter;
    private int shopIdCounter = 1;
    private ObjectMapper objectMapper;
    private InteractionService interactionService = InteractionService.getInstance();
    private int messageIdCounter = 1;
    

    public ShopService(IUserRepository userRepository, IShopRepository shopRepository,IOrderRepository orderRepository, IAuthentication authenticationAdapter) {
        this.userRepository = userRepository;
        this.shopRepository = shopRepository;
        this.orderRepository = orderRepository;
        this.authenticationAdapter = authenticationAdapter;
        this.objectMapper = new ObjectMapper();
        this.shoppingService = new ShoppingService();
    }
    
    public List<ShopDTO> showAllShops() {
        ArrayList<Shop> s = new ArrayList<Shop>(shopRepository.getAllShops().values());
        List<ShopDTO> shopDTOs = new ArrayList<>();
        
        for (Shop shop : s) {
            ShopDTO shopDTO = objectMapper.convertValue(shop, ShopDTO.class);
            shopDTOs.add(shopDTO);
        }
        return shopDTOs;
    }

    public List<ItemDTO> showShopItems(int shopId) {
        ArrayList<Item> items=new ArrayList<Item>(shopRepository.getShopById(shopId).getItems().values());
        List<ItemDTO> itemDTOs = new ArrayList<>();
        for (Item item : items) {
            ItemDTO itemDTO = objectMapper.convertValue(item, ItemDTO.class);
            itemDTOs.add(itemDTO);
        }
        return itemDTOs;
    }

    public List<ItemDTO> filterItemsAllShops(HashMap<String, String> filters){
        String category = filters.get("category");
        String name = filters.get("name");
        double minPrice = filters.get("minPrice") != null ? Integer.parseInt(filters.get("minPrice")) : 0;
        double maxPrice = filters.get("maxPrice") != null ? Integer.parseInt(filters.get("maxPrice")) : 0;
        int minRating = filters.get("minRating") != null ? Integer.parseInt(filters.get("minRating")) : 0;
        int shopRating = filters.get("shopRating") != null ? Integer.parseInt(filters.get("shopRating")) : 0;
        List<Item> filteredItems = new ArrayList<>();
        for (Shop shop : shopRepository.getAllShops().values()) {
            filteredItems.addAll(shop.filter(name, category, minPrice, maxPrice, minRating, shopRating));
        }
        List<ItemDTO> itemDTOs = new ArrayList<>();
        for (Item item : filteredItems) {
            ItemDTO itemDTO = objectMapper.convertValue(item, ItemDTO.class);
            itemDTOs.add(itemDTO);
        }
        return itemDTOs;
    }

    public List<ItemDTO> filterItemsInShop(int shopId, HashMap<String, String> filters){
        String category = filters.get("category");
        String name = filters.get("name");
        double minPrice = filters.get("minPrice") != null ? Integer.parseInt(filters.get("minPrice")) : 0;
        double maxPrice = filters.get("maxPrice") != null ? Integer.parseInt(filters.get("maxPrice")) : 0;
        int minRating = filters.get("minRating") != null ? Integer.parseInt(filters.get("minRating")) : 0;
        List<Item> filteredItems = new ArrayList<>();
        Shop shop = shopRepository.getShopById(shopId);
        filteredItems.addAll(shop.filter(name, category, minPrice, maxPrice, minRating,0));
        List<ItemDTO> itemDTOs = new ArrayList<>();
        for (Item item : filteredItems) {
            ItemDTO itemDTO = objectMapper.convertValue(item, ItemDTO.class);
            itemDTOs.add(itemDTO);
        }
        return itemDTOs;
    }

    public Shop createShop(String sessionToken, String name, String description) {
        if(!authenticationAdapter.validateToken(sessionToken)){
            System.out.println("Please log in or register to add items to the shop.");
            throw new RuntimeException("Please login.");
        }
        int userID = Integer.parseInt(authenticationAdapter.getUsername(sessionToken));
        Shop shop = managementService.createShop(shopRepository.getAllShops().size(),(Registered)userRepository.getUserById(userID), name, description);
        shopRepository.addShop(shop);
        return shop;
    }

    public ShopDTO getShopInfo(String sessionToken, int shopID) {
        if(!authenticationAdapter.validateToken(sessionToken)){
            System.out.println("Please log in or register to add items to the shop.");
            throw new RuntimeException("Please login.");
        }
        else {
            Shop shop = this.shopRepository.getShopById(shopID);
            ShopDTO shopDto = objectMapper.convertValue(shop, ShopDTO.class);
            return shopDto;
        }
    }

    public void addItemToShop(String sessionToken, int shopID, String itemName, Category category, double itemPrice, String description) {
        //need to add the Check if the user is logged in
        // If not, prompt to log in or register
        // If logged in, add the item to the shop with the provided details
        if(!authenticationAdapter.validateToken(sessionToken)){
            System.out.println("Please log in or register to add items to the shop.");
            return;
        }
        else {
            int userID = Integer.parseInt(authenticationAdapter.getUsername(sessionToken));
            Registered user = (Registered)this.userRepository.getUserById(userID);
            Shop shop = this.shopRepository.getShopById(shopID);
            this.managementService.addItemToShop(user, shop, itemName, category, itemPrice, description);
        }
    }
    public void removeItemFromShop(String sessionToken, int shopID, int itemID) {
        // Check if the user is logged in
        // If not, prompt to log in or register
        // If logged in, remove the item from the shop with the provided details
        if(authenticationAdapter.validateToken(sessionToken)){
            int userID = Integer.parseInt(authenticationAdapter.getUsername(sessionToken));
            Registered user = (Registered)this.userRepository.getUserById(userID);
            Shop shop=shopRepository.getShopById(shopID);
            managementService.removeItemFromShop(user, shop, itemID);
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
            int userID = Integer.parseInt(authenticationAdapter.getUsername(sessionToken));
            Registered user = (Registered)this.userRepository.getUserById(userID);
            Shop shop = this.shopRepository.getShopById(shopID);

            managementService.updateItemQuantity(user, shop, itemID, newQuantity);
        }
    }
    public void changeItemPriceInShop(String sessionToken, int shopID, int itemID, double newPrice) {
        // Check if the user is logged in
        // If not, prompt to log in or register
        // If logged in, change the item price in the shop with the provided details
        if(authenticationAdapter.validateToken(sessionToken)){
            int userID = Integer.parseInt(authenticationAdapter.getUsername(sessionToken));
            Registered user = (Registered)this.userRepository.getUserById(userID);
            Shop shop=shopRepository.getShopById(shopID);
            managementService.updateItemPrice(user, shop, itemID, newPrice);
        }
    }
    public void changeItemDescriptionInShop(String sessionToken, int shopID, int itemID, String newDescription) {
        // Check if the user is logged in
        // If not, prompt to log in or register
        // If logged in, change the item name in the shop with the provided details
        if(authenticationAdapter.validateToken(sessionToken)){
            int userID = Integer.parseInt(authenticationAdapter.getUsername(sessionToken));
            Registered user = (Registered)this.userRepository.getUserById(userID);
            Shop shop=shopRepository.getShopById(shopID);
            managementService.updateItemDescription(user, shop, itemID, newDescription);
        }
    }
    public void rateShop(String sessionToken, int shopID, int rating) {
        // If logged in, rate the shop with the provided rating
        if(!authenticationAdapter.validateToken(sessionToken)){
            System.out.println("Please log in or register to add items to the shop.");
            return;
        }
        else {
            int userID = Integer.parseInt(authenticationAdapter.getUsername(sessionToken));
            Registered user = (Registered)this.userRepository.getUserById(userID);
            Shop shop = this.shopRepository.getShopById(shopID);
            List<Order> orders = orderRepository.getOrdersByUserName(user.getUsername());
            shoppingService.RateShop(shop,orders ,rating);
        }
    }

    public void rateItem(String sessionToken,int shopID, int itemID, int rating) {
        // Check if the user is logged in
        // If not, prompt to log in or register
        // If logged in, rate the item with the provided rating
        if(!authenticationAdapter.validateToken(sessionToken)){
            System.out.println("Please log in or register to add items to the shop.");
            return;
        }
        else {
            int userID = Integer.parseInt(authenticationAdapter.getUsername(sessionToken));
            Registered user = (Registered)this.userRepository.getUserById(userID);
            Shop shop = this.shopRepository.getShopById(shopID);
            List<Order> orders = orderRepository.getOrdersByUserName(user.getUsername());
            shoppingService.RateItem(shop,itemID,orders, rating);
            
        }
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
            int userID = Integer.parseInt(authenticationAdapter.getUsername(sessionToken));
            Registered user = (Registered)this.userRepository.getUserById(userID);
            Shop shop = this.shopRepository.getShopById(shopID);
            this.managementService.updateDiscountType(user, shop, discountType);
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
            int userID = Integer.parseInt(authenticationAdapter.getUsername(sessionToken));
            Registered user = (Registered)this.userRepository.getUserById(userID);
            Shop shop = this.shopRepository.getShopById(shopID);
            this.managementService.updatePurchaseType(user, shop, purchaseType);
        }
    }
    public void addShopOwner(String sessionToken, int shopID, String appointeeName) {
        if(!authenticationAdapter.validateToken(sessionToken)){
            System.out.println("Please log in or register to add items to the shop.");
            return;
        }
        else {
            int userID = Integer.parseInt(authenticationAdapter.getUsername(sessionToken));
            Registered user = (Registered)this.userRepository.getUserById(userID);
            Registered appointee = this.userRepository.getUserByName(appointeeName);
            Shop shop = this.shopRepository.getShopById(shopID);
            this.managementService.addOwner(user, shop, appointee);
        }
    }
    public void addShopManager(String sessionToken, int shopID, String appointeeName, Set<Permission> permission) {
       if(authenticationAdapter.validateToken(sessionToken)){
            int userID = Integer.parseInt(authenticationAdapter.getUsername(sessionToken));
            Registered user = (Registered)this.userRepository.getUserById(userID);
            Registered appointee=userRepository.getUserByName(appointeeName);
            Shop shop=shopRepository.getShopById(shopID);
            managementService.addManager(user, shop, appointee, permission);
        }
    }
    public void removeShopOwner(String sessionToken, int shopID, String appointeeName) {
        if(authenticationAdapter.validateToken(sessionToken)){
            int userID = Integer.parseInt(authenticationAdapter.getUsername(sessionToken));
            Registered user = (Registered)this.userRepository.getUserById(userID);
            Registered appointee=userRepository.getUserByName(appointeeName);
            Shop shop=shopRepository.getShopById(shopID);
            managementService.removeAppointment(user, shop, appointee);
        }
    }
    public void addShopManagerPermission(String sessionToken, int shopID,String appointeeName, Permission  permission) {
        if(authenticationAdapter.validateToken(sessionToken)){
            int userID = Integer.parseInt(authenticationAdapter.getUsername(sessionToken));
            Registered user = (Registered)this.userRepository.getUserById(userID);
            Registered appointee=userRepository.getUserByName(appointeeName);
            Shop shop=shopRepository.getShopById(shopID);
            managementService.addPermission(user, shop, appointee, permission);
        }
    }
    
    public void removeShopManagerPermission(String sessionToken, int shopID,String appointeeName , Permission permission) {
        if(authenticationAdapter.validateToken(sessionToken)){
            int userID = Integer.parseInt(authenticationAdapter.getUsername(sessionToken));
            Registered user = (Registered)this.userRepository.getUserById(userID);
            Registered appointee=userRepository.getUserByName(appointeeName);
            Shop shop=shopRepository.getShopById(shopID);
            managementService.removePermission(user, shop, appointee, permission);
        }
    }

    public void closeShopByFounder(String sessionToken, int shopID) {
        // Check if the user is logged in
        // If not, prompt to log in or register
        // If logged in, close the shop with the provided details
        if(authenticationAdapter.validateToken(sessionToken)){
            int userID = Integer.parseInt(authenticationAdapter.getUsername(sessionToken));
            Registered user = (Registered)this.userRepository.getUserById(userID);
            Shop shop=shopRepository.getShopById(shopID);
            managementService.closeShop(user, shop);
        }
    }
    public String getMembersPermissions(String sessionToken, int shopID) {
        if(authenticationAdapter.validateToken(sessionToken)){
            int userID = Integer.parseInt(authenticationAdapter.getUsername(sessionToken));
            Registered user = (Registered)this.userRepository.getUserById(userID);
            Shop shop=shopRepository.getShopById(shopID);
            List<Integer> membersUserIds=managementService.getMembersPermissions(user, shop);
            StringBuilder permissions = new StringBuilder();
            for(int memberId : membersUserIds){
                Registered member = (Registered)userRepository.getUserById(memberId);
                permissions.append(member.getUsername()).append(": ");
                permissions.append(member.getPermissions(shopID)).append("\n");
            }

            return permissions.toString();
        }
        return null;
    }

    public void sendMessage(String sessionToken, int shopId, String title, String content) {
        if(authenticationAdapter.validateToken(sessionToken)){
            String username=authenticationAdapter.getUsername(sessionToken);
            Registered user=userRepository.getUserByName(username);
            Shop shop=shopRepository.getShopById(shopId);
            if(user.getRoleInShop(shopId).equals("Owner")){
                IMessage message = interactionService.createMessage(messageIdCounter, shop.getId(), shop.getName(), shop.getId(), title, content);
                interactionService.sendMessage(user, message);
                messageIdCounter++;
            } 
            else {
                System.out.println("You don't have permission to send messages in this shop.");
            }
        }
    }

    public void respondToMessage(String sessionToken, int shopId, int messageId, String title, String content) {
        if(authenticationAdapter.validateToken(sessionToken)){
            String username=authenticationAdapter.getUsername(sessionToken);
            Registered user=userRepository.getUserByName(username);
            Shop shop=shopRepository.getShopById(shopId);
            IMessage parentMessage = shop.getAllMessages().get(messageId);
            if(user.getRoleInShop(shopId).equals("Owner")){
                IMessage responseMessage = interactionService.createMessage(this.messageIdCounter, shop.getId(), shop.getName(), shop.getId(), title, content);
                interactionService.respondToMessage(parentMessage, responseMessage);
                messageIdCounter++;
            } 
            else {
                System.out.println("You don't have permission to respond to messages in this shop.");
            }

    public void answerBid(String sessionToken,int shopID, int bidID,boolean accept) {
        if(authenticationAdapter.validateToken(sessionToken)){
            int userID = Integer.parseInt(authenticationAdapter.getUsername(sessionToken));
            Registered user = (Registered)this.userRepository.getUserById(userID);
            Shop shop=shopRepository.getShopById(shopID);
            managementService.answerBid(user, shop, bidID, accept);
        }
    }
    public void submitCounterBid(String sessionToken,int shopID, int bidID,double offerAmount) {
        if(authenticationAdapter.validateToken(sessionToken)){
            int userID = Integer.parseInt(authenticationAdapter.getUsername(sessionToken));
            Registered user = (Registered)this.userRepository.getUserById(userID);
            Shop shop=shopRepository.getShopById(shopID);
            managementService.submitCounterBid(user, shop, bidID, offerAmount);
        }
    }

}
