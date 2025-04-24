package Domain.Repositories;

import Domain.DTOs.UserDTO;
import Domain.DTOs.UserRoleDTO;

import java.util.HashMap;
import java.util.List;

public interface IUserRepository {
    void addUser(UserDTO user);
    UserDTO getUserById(String username);
    void updateUser(UserDTO user);
    void deleteUser(String username);
    boolean isUsernameExists(String username);
    HashMap<Integer,UserDTO> getAllUsers();
    void assignRoleToUserInShop(String username, int shopId, String role);
    String getRoleOfUserInShop(String username, int shopId);
    List<UserRoleDTO> getRolesByUserId(String username);
}
