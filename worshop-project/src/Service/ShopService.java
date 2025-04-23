package Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import Domain.ItemDTO;
import Domain.DTOs.ShopDTO;
import Domain.DomainServices.ManagementService;
import Domain.Repositories.IShopRepository;
import Domain.Repositories.IUserRepository;

public class ShopService {
    
    private IUserRepository userRepository;
    private IShopRepository shopRepository;
    private ManagementService managementService = ManagementService.getInstance();
    private int shopIdCounter = 1;
    

    public ShopService(IUserRepository userRepository, IShopRepository shopRepository) {
        this.userRepository = userRepository;
        this.shopRepository = shopRepository;
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

    
}
