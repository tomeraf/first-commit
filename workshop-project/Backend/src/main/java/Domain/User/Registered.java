package Domain.User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.cglib.core.Local;

import Domain.Adapters_and_Interfaces.IMessage;
import Domain.Adapters_and_Interfaces.IMessageListener;
import Domain.DTOs.ItemDTO;

public class Registered extends Guest implements IMessageListener {
    private Map<Integer, IRole> roleInShops; //<shopID, role>
    private String username;
    private String password;
    private HashMap<Integer, IMessage> inbox = new HashMap<>();
    private boolean isSystemManager = false;
    private Suspension suspension = null;

    private LocalDate dateOfBirth;
    
    public Registered(String username, String password, LocalDate dateOfBirth) {
        this.username = username;
        this.password = password;
        this.dateOfBirth = dateOfBirth;
        this.roleInShops = new ConcurrentHashMap<>();
        this.suspension=new Suspension();
    }
    public String getUsername() {
        return username;
    }
    public String getPassword() {
        return password;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    public IRole getRoleInShop(int shopID) {
        if(roleInShops.containsKey(shopID)) {
            return roleInShops.get(shopID);
        }
        return null;
    }
    public boolean logout() {
        if (!isInSession()) {
            throw new IllegalArgumentException("Unauthorized Action: already logged out.");
        }
        this.sessionToken = null;
        return true;
    }
    public void setRoleToShop(int shopID, IRole newRole) {
        newRole.setUser(this);
        this.roleInShops.put(shopID, newRole);
    }
    public boolean removeRoleFromShop(int shopID) {
        if (!roleInShops.containsKey(shopID)) {
            throw new IllegalArgumentException("No role found for shop ID: " + shopID);
        }
        roleInShops.remove(shopID);
        return true;
    }
    
    public boolean hasPermission(int shopID, Permission permission) {
        if(roleInShops.containsKey(shopID)) {
            return roleInShops.get(shopID).hasPermission(permission);
        }
        throw new IllegalArgumentException("No role found for shop ID: " + shopID);    
    }
    public boolean addPermission(int shopID, Permission permission) {
        if(roleInShops.containsKey(shopID)) {
            roleInShops.get(shopID).addPermission(permission);
            return true;
        } else {
            throw new IllegalArgumentException("No role found for shop ID: " + shopID);
        }
    }
    public boolean removePermission(int shopID, Permission permission) {
        if(roleInShops.containsKey(shopID)) {
            roleInShops.get(shopID).removePermission(permission);
            return true;
        } else {
            throw new IllegalArgumentException("No role found for shop ID: " + shopID);
        }
    }

    // As and owner or manager
    public boolean addManager(int shopID, int nomineeID, Manager nominee)  {
        if (!roleInShops.containsKey(shopID)) {
            throw new IllegalArgumentException("User has no role in ShopId: " + shopID);
        }
        if (!roleInShops.get(shopID).hasPermission(Permission.APPOINTMENT)) {
            throw new IllegalArgumentException("User has no permission to add users.");
        }
        roleInShops.get(shopID).addManager(nomineeID, nominee);      
        return true;      
    }

    public boolean addOwner(int shopID, int nomineeID, Owner nominee) {
        if (!roleInShops.containsKey(shopID)) {
            throw new IllegalArgumentException("No role found for shop ID: " + shopID);
        }
        if (!roleInShops.get(shopID).hasPermission(Permission.APPOINTMENT)) {
            throw new IllegalArgumentException("No permission to add appointment in shop ID: " + shopID);
        }
        roleInShops.get(shopID).addOwner(nomineeID, nominee);      
        return true;      
    }
    
    public List<Integer> removeAppointment(int shopID, int appointeeID) {
        if (!roleInShops.containsKey(shopID)) {
            throw new IllegalArgumentException("No role found for shop ID: " + shopID);
        }
        if (!roleInShops.get(shopID).hasPermission(Permission.APPOINTMENT)) {
            throw new IllegalArgumentException("No permission to remove appointment in shop ID: " + shopID);
        } 
        return roleInShops.get(shopID).removeAppointment(appointeeID);
    }

    public Map<Integer, IRole> getAppointments(int shopID) {
        if(roleInShops.containsKey(shopID)) {
            return roleInShops.get(shopID).getAppointments();
        } else {
            throw new IllegalArgumentException("No role found for shop ID: " + shopID);
        }
    }
    public int getAppointer(int shopID) {
        if(roleInShops.containsKey(shopID)) {
            return roleInShops.get(shopID).getAppointer();
        } else {
            return -1;
        }
    }
    public int getAge() {
        return Period.between(dateOfBirth, LocalDate.now()).getYears();
    }
    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }
    
    public String getPermissions(int shopID) {
        if(roleInShops.containsKey(shopID)) {
            return roleInShops.get(shopID).getPermissionsString();
        } else {
            throw new IllegalArgumentException("No role found for shop ID: " + shopID);
        }
    }
    @Override  
    public void acceptMessage(IMessage message) {
        inbox.put(message.getId(), message);
    }

    public boolean isSystemManager() {
        return isSystemManager;
    }
    public void setSystemManager(boolean isSystemManager) {
        this.isSystemManager = isSystemManager;
    }


    // Suspension methods
    public void addSuspension(Optional<LocalDateTime> startDate, Optional<LocalDateTime> endDate) {
        if (startDate.isPresent() && endDate.isPresent()) {
            this.suspension.setSuspension(startDate.get(), endDate.get());   
        }
        else
            this.suspension.setSuspension();   
    }
    public void removeSuspension() {
        this.suspension.removeSuspension();
    }
    @Override
    public boolean isSuspended() {
        return this.suspension.isSuspended(LocalDateTime.now());
    }
    public String showSuspension() {
        return suspension.toString();
    }



}
