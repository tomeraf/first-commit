package Domain;

import java.util.List;
public interface IUserRepository {
    void addUser(UserDTO user);
    UserDTO getUserById(int id);
    void updateUser(UserDTO user);
    void deleteUser(int id);
    boolean isUsernameExists(String username);
    List<UserDTO> getAllUsers();
}
