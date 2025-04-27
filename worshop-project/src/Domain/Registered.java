package Domain;

import java.time.LocalDate;
import java.time.Period;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import Domain.Adapters_and_Interfaces.IMessage;
import Domain.Adapters_and_Interfaces.IMessageListener;
import Domain.DTOs.ItemDTO;

public class Registered extends Guest implements IMessageListener {
    private Map<Integer, IRole> roleInShops; //<shopID, role>
    private String username;
    private String password;
    private HashMap<Integer, IMessage> inbox = new HashMap<>();

    private LocalDate dateOfBirth;
    
    public Registered(String username, String password, LocalDate dateOfBirth) {
        this.username = username;
        this.password = password;
        this.dateOfBirth = dateOfBirth;
        this.roleInShops = new ConcurrentHashMap<>();
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
    
    public void setRoleToShop(int shopID, IRole newRole) {
        this.roleInShops.put(shopID, newRole);
    }
    public boolean hasPermission(int shopID, Permission permission) {
        if(roleInShops.containsKey(shopID)) {
            return roleInShops.get(shopID).hasPermission(permission);
        }
        System.out.println("No role found for shop ID: " + shopID);
        return false;      
    }
    public boolean addPermission(int shopID, Permission permission) {
        if(roleInShops.containsKey(shopID)) {
            roleInShops.get(shopID).addPermission(permission);
            return true;
        } else {
            System.out.println("No role found for shop ID: " + shopID);
        }
        return false;
    }
    public boolean removePermission(int shopID, Permission permission) {
        if(roleInShops.containsKey(shopID)) {
            roleInShops.get(shopID).removePermission(permission);
            return true;
        } else {
            System.out.println("No role found for shop ID: " + shopID);
        }
        return false;
    }

    // As and owner or manager
    public boolean addAppointment(int shopID, int nomineeID, IRole nominee) {
        if (!roleInShops.containsKey(shopID)) {
            System.out.println("No role found for shop ID: " + shopID);
            return false;
        }
        if (!roleInShops.get(shopID).hasPermission(Permission.APPOINTMENT)) {
            System.out.println("No permission to add appointment in shop ID: " + shopID);
            return false;
        }
        roleInShops.get(shopID).addAppointment(nomineeID, nominee);      
        return true;      
    }
    
    public boolean removeAppointment(int shopID, int appointeeID) {
        if (!roleInShops.containsKey(shopID)) {
            System.out.println("No role found for shop ID: " + shopID);
            return false;
        }
        if (!roleInShops.get(shopID).hasPermission(Permission.APPOINTMENT)) {
            System.out.println("No permission to remove appointment in shop ID: " + shopID);
            return false;
        } 
        roleInShops.get(shopID).removeAppointment(appointeeID);
        return true;
    }

    public Map<Integer, IRole> getAppointments(int shopID) {
        if(roleInShops.containsKey(shopID)) {
            return roleInShops.get(shopID).getAppointments();
        } else {
            System.out.println("No role found for shop ID: " + shopID);
            return null;
        }
    }
    public int getAppointer(int shopID) {
        if(roleInShops.containsKey(shopID)) {
            return roleInShops.get(shopID).getAppointer();
        } else {
            System.out.println("No role found for shop ID: " + shopID);
            return -1;
        }
    }
    public void addItemToBasket(List<ItemDTO> items) {
        cart.addItems(items);
    }
    public void removeItemFromBasket(Map<Integer, Integer> items) {
        cart.deleteItems(items);
    }
    public int getAge() {
        return Period.between(dateOfBirth, LocalDate.now()).getYears();
    }
    public boolean removeShopRole(int shopID) {
        if (!roleInShops.containsKey(shopID)) {
            System.out.println("No role found for shop ID: " + shopID);
            return false;
        }
        roleInShops.get(shopID).removeAppointment((int)this.getUserID());
        roleInShops.remove(shopID);
        return true;
    }
    @Override  
    public void acceptMessage(IMessage message) {
        inbox.put(message.getId(), message);
        System.out.println("Message received from " + message.getSenderName() + ".");
    }

}
