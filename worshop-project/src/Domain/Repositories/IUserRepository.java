package Domain.Repositories;

import Domain.Item;
import Domain.Registered;
import Domain.ShoppingCart;
import Domain.DTOs.UserDTO;
import Domain.DTOs.UserShopPermissionDTO;

import java.security.Permission;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface IUserRepository {
    void saveUser(Registered user);
    void updateUser(Registered user);
    
    Registered getUserById(int id);

    int getIdToAssign(); // It will give a unique ID for the user
    boolean removeGuestId(int id);
    
    Map<Integer, Registered> getAllUsers();
    void assignPermissionToUserInShop(int userId, int shopId, Permission perm);
    String getRoleOfUserInShop(int userId, int shopId);
    void saveCartContent(int cartID, List<Item> items); // middle-table

    ShoppingCart getShoppingCart(int cartID);

    void addItemsToCart(int cartID, List<Integer> itemIDs, List<Integer> shopIDs); // save the cart in the DB
    void removeItemsFromCart(int cartID, List<Integer> itemIDs, List<Integer> shopIDs); // remove the items from the cart in the DB
    Registered getUserByName(String username);
}
