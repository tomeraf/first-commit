package Domain.Repositories;

import Domain.Item;
import Domain.Registered;
import Domain.DTOs.UserDTO;
import Domain.DTOs.UserRoleDTO;

import java.util.HashMap;
import java.util.List;

public interface IUserRepository {
    void saveUser(Registered user);
    Registered getUserById(int id);
    void updateUser(Registered user);
    void deleteUser(int id);
    boolean isUsernameExists(String username);
    HashMap<Integer,Registered> getAllUsers();
    void assignRoleToUserInShop(int userId, int shopId, String role);
    String getRoleOfUserInShop(int userId, int shopId);
    int getIdToAssign();
    boolean removedId(int id);
    void saveCartContent(int cartID, List<Item> items); // middle-table
}
