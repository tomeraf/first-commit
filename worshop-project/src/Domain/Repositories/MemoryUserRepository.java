package Domain.Repositories;

import java.security.Permission;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Domain.Guest;
import Domain.Item;
import Domain.Registered;
import Domain.ShoppingCart;
import Domain.DTOs.CartShopItemDTO;
import Domain.DTOs.ItemDTO;
import Domain.DTOs.UserDTO;
import Domain.DTOs.UserShopPermissionDTO;

class Pair<K, V> {
    private K key;
    private V value;

    public Pair(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public K getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }
}

public class MemoryUserRepository implements IUserRepository {

    private Map<Integer, UserDTO> usersTable = new HashMap<>();
    private List<UserShopPermissionDTO> userRoleTable = new ArrayList<>();
    private List<CartShopItemDTO> cartTable = new ArrayList<>();
    private List<ItemDTO> itemTable = new ArrayList<>(); // Map to store users

    private int idCounter = 0; // Unique ID for each user
    private List<Integer> removedIds = new ArrayList<>(); // List of removed IDs

    @Override
    public void saveUser(Registered user) {
        UserDTO userDTO = usersTable.get(user.getUserID());
        if (userDTO != null) {
            throw new RuntimeException("User already exists");
        }
        userDTO = new UserDTO();
        userDTO.id = user.getUserID();
        userDTO.username = user.getUsername();
        userDTO.password = user.getPassword();
        userDTO.dateOfBirth = user.getDateOfBirth();

        usersTable.put(user.getUserID(), userDTO);
    }

    @Override
    public void updateUser(Registered user) {
        UserDTO userDTO = usersTable.get(user.getUserID());
        if (userDTO == null) {
            throw new RuntimeException("User doesn't exist");
        }
        userDTO.id = user.getUserID();
        userDTO.username = user.getUsername();
        userDTO.password = user.getPassword();
        userDTO.dateOfBirth = user.getDateOfBirth();

        usersTable.put(user.getUserID(), userDTO);
    }

    @Override
    // Get user by ID - which is not active and has session token = null
    public Registered getUserById(int id) {
        UserDTO userDTO = usersTable.get(id);
        if (userDTO == null) {
            throw new RuntimeException("User not found");
        }
        
        Registered user = new Registered(userDTO.username, userDTO.password, userDTO.dateOfBirth); 
        user.setCart(getShoppingCart(id));
        return user;
    }
    
    @Override
    public int getIdToAssign() {
        if (removedIds.isEmpty()) {
            return idCounter++;
        } else {
            return removedIds.remove(removedIds.size() - 1); // Reuse a removed ID
        }
    }

    @Override
    public boolean removeGuestId(int id) throws RuntimeException {
        if (usersTable.containsKey(id)) {
            usersTable.remove(id);
            cartTable.removeIf(cart -> cart.cartID == id); // Remove cart content for the user
            removedIds.add(id); // Add the ID to the removed list
            return true;
        }
        throw new RuntimeException("User ID not found");
    }

    @Override
    public Map<Integer, Registered> getAllUsers() {
        return usersTable.entrySet().stream()
                .collect(HashMap::new, (map, entry) -> {
                    UserDTO userDTO = entry.getValue();
                    Registered user = new Registered(userDTO.username, userDTO.password, userDTO.dateOfBirth);
                    user.setCart(getShoppingCart(userDTO.id));
                    map.put(entry.getKey(), user);
                }, HashMap::putAll);
    }

    @Override
    public void assignPermissionToUserInShop(int userId, int shopId, Permission perm) {

        // if (perm == Permission.OWNER)

        // UserRoleDTO userRole = new UserRoleDTO(userId, shopId, role);
        // userRoleTable.add(userRole); // Add the role to the list
        // Registered user = usersTable.get(userId);

        // if (user != null) {
        //     user.setRoleToShop(shopId, userRole); // Set the role for the user
        // } else {
        //     throw new RuntimeException("User not found");
        // }
    }

    @Override
    public String getRoleOfUserInShop(int userId, int shopId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getRoleOfUserInShop'");
    }

    @Override
    public void saveCartContent(int cartID, List<Item> items) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'saveCartContent'");
    }

    @Override
    public ShoppingCart getShoppingCart(int cartID) {
        List<Integer> shopsByCartID = cartTable.stream()
                .filter(cart -> cart.cartID == cartID)
                .map(cart -> cart.shopID)
                .toList(); // Get distinct shop IDs for the given cart ID
        List<Integer> itemsByCartID = cartTable.stream()
                .filter(cart -> cart.cartID == cartID)
                .map(cart -> cart.itemID)
                .toList(); // Get items for the given cart ID
        
        List<ItemDTO> items = new ArrayList<>();
        for (int i = 0; i < shopsByCartID.size(); i++) {
            int shopID = shopsByCartID.get(i);
            int itemID = itemsByCartID.get(i);
            List<ItemDTO> singleItem = itemTable.stream().filter(item -> item.getShopId() == shopID && item.getItemID() == itemID).toList(); // Filter items based on shop ID and item ID
            if (singleItem.size() > 1) {
                throw new RuntimeException("More than one item found for the same shop ID and item ID");
            }
            items.add(singleItem.get(0)); // Add the item to the list
        }

        ShoppingCart shoppingCart = new ShoppingCart(cartID); // Create a new ShoppingCart object with the items
        shoppingCart.addItems(items);
        return shoppingCart; // Return the ShoppingCart object with the items
    }

    @Override
    public void addItemsToCart(int cartID, List<Integer> itemIDs, List<Integer> shopIDs) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'addItemsToCart'");
    }

    @Override
    public void removeItemsFromCart(int cartID, List<Integer> itemIDs, List<Integer> shopIDs) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'removeItemsFromCart'");
    }

    @Override
    public Registered getUserByName(String username) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getUserByName'");
    }
    
}
