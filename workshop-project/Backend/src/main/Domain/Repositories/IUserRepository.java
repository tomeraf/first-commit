package main.Domain.Repositories;

import main.Domain.Guest;
import main.Domain.Registered;
import java.util.Map;

public interface IUserRepository {
    void saveUser(Guest user);
    void saveUser(Registered user);
    void removeGuestById(int id);
    
    Guest getUserById(int id);
    
    int getIdToAssign(); // It will give a unique ID for the user
    
    Map<Integer, Guest> getAllUsers();

    Registered getUserByName(String username);
}
