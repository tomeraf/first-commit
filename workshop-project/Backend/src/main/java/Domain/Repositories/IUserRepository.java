package Domain.Repositories;

import Domain.Guest;
import Domain.Registered;
import java.util.Map;

import org.springframework.stereotype.Repository;

@Repository
public interface IUserRepository {
    void saveUser(Guest user);
    void saveUser(Registered user);
    void removeGuestById(int id);
    
    Guest getUserById(int id);
    
    int getIdToAssign(); // It will give a unique ID for the user
    
    Map<Integer, Guest> getAllUsers();

    Registered getUserByName(String username);
}
