package Domain.Repositories;

import Domain.Item;
import Domain.Registered;
import Domain.ShoppingCart;
import Domain.DTOs.UserDTO;
import Domain.DTOs.UserRoleDTO;

import java.util.HashMap;
import java.util.List;

public interface IUserRepository {
    void saveUser(Registered user);
    void updateUser(Registered user);
    void deleteUser(int id);
    
    Registered getUserById(int id);

    int getIdToAssign(); // It will give a unique ID for the user
    boolean removedId(int id);
    
    boolean isUsernameExists(String username);
    HashMap<Integer,Registered> getAllUsers();
    void assignRoleToUserInShop(int userId, int shopId, String role);
    String getRoleOfUserInShop(int userId, int shopId);
    void saveCartContent(int cartID, List<Item> items); // middle-table

    ShoppingCart getShoppingCart(int cartID);
    void removedCartContent(int userID);

    void addItemsToCart(int cartID, List<Integer> itemIDs, List<Integer> shopIDs); // save the cart in the DB
    void removeItemsFromCart(int cartID, List<Integer> itemIDs, List<Integer> shopIDs); // remove the items from the cart in the DB
    Registered getUserByName(String username);
}
