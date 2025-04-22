package Domain.Repositories;

import Domain.DTOs.UserDTO;
import Domain.DTOs.UserRoleDTO;

import java.util.HashMap;
import java.util.List;

public interface IUserRepository {
    void addUser(UserDTO user);
    UserDTO getUserById(int id);
    void updateUser(UserDTO user);
    void deleteUser(int id);
    boolean isUsernameExists(String username);
    HashMap<Integer,UserDTO> getAllUsers();
    void assignRoleToUserInShop(int userId, int shopId, String role);
    String getRoleOfUserInShop(int userId, int shopId);
    List<UserRoleDTO> getRolesByUserId(int userId);
}
