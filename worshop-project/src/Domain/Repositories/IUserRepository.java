package Domain.Repositories;

import Domain.DTOs.UserDTO;

import java.util.HashMap;

public interface IUserRepository {
    void addUser(UserDTO user);
    UserDTO getUserById(int id);
    void updateUser(UserDTO user);
    void deleteUser(int id);
    boolean isUsernameExists(String username);
    HashMap<Integer,UserDTO> getAllUsers();
}
