package Domain.DomainServices;

import java.util.List;
import java.util.Set;

import Domain.Category;
import Domain.Manager;
import Domain.Owner;
import Domain.Permission;
import Domain.Registered;
import Domain.Shop;
import Domain.DTOs.ShopDTO;
import Domain.DTOs.UserDTO;

public class ManagementService {

    public ShopDTO createShop(int shopId, UserDTO user, String name, String description, String address, String phoneNumber) {
        throw new UnsupportedOperationException("Not implemented yet.");
        //shoukd adapt user role
    }
    
    public void addOwner(Registered appointer, Shop shop, Registered appointee) {
        Owner owner = new Owner((int)appointer.getUserID(),shop.getId());
        appointer.addAppointment(shop.getId(), (int)appointee.getUserID(), owner);
        appointee.setRoleToShop(shop.getId(), owner);
    }

    public void removeAppointment(Registered appointer, Shop shop, Registered userToRemove) {
        appointer.removeAppointment(shop.getId(), (int)userToRemove.getUserID());
        userToRemove.removeShopRole(shop.getId());

    }
    public void addManager(Registered appointer, Shop shop, Registered appointee, Set<Permission> permission) {
        Manager manager = new Manager((int)appointer.getUserID(),shop.getId(), permission);
        appointer.addAppointment(shop.getId(), (int)appointee.getUserID(), manager);
        appointee.setRoleToShop(shop.getId(), manager);   
    }

    public void addPermission(Registered appointer, Shop shop, Registered appointee, Permission permission) {
        if(appointer.hasPermission(shop.getId(), Permission.UPDATE_PERMISSIONS)) {
            appointee.addPermission(shop.getId(), permission);
        } else {
            System.out.println("You don't have permission to add permissions");
        }
    }
    public void removePermission(Registered appointer, Shop shop, Registered appointee, Permission permission) {
        if(appointer.hasPermission(shop.getId(), Permission.UPDATE_PERMISSIONS)) {
            appointee.removePermission(shop.getId(), permission);
        } else {
            System.out.println("You don't have permission to remove permissions");
        }
    }
    
    public void addItemToShop(Registered supplyManager, Shop shop, String name, Category category, double price) {
        if (supplyManager.hasPermission(shop.getId(), Permission.UPDATE_SUPPLY)){
            shop.addItem(name, category, price);
        }
        else {
            System.out.println("You don't have permission to add items to the shop");
        }
    }
    public void removeItemFromShop(Registered supplyManager, Shop shop, int itemID) {
        if (supplyManager.hasPermission(shop.getId(), Permission.UPDATE_SUPPLY)) {
            shop.removeItem(itemID);
        } else {
            System.out.println("You don't have permission to remove items from the shop");
        }
    }
    public void updateItemName(Registered supplyManager, Shop shop, int itemID, String name) {
        if (supplyManager.hasPermission(shop.getId(), Permission.UPDATE_ITEM_DESCRIPTION)) {
            shop.updateItemName(itemID, name);
        } else {
            System.out.println("You don't have permission to update item name");
        }
    }
    public void updateItemPrice(Registered supplyManager, Shop shop, int itemID, double price) {
        if (supplyManager.hasPermission(shop.getId(), Permission.UPDATE_ITEM_PRICE)) {
            shop.updateItemPrice(itemID, price);
        } else {
            System.out.println("You don't have permission to update item price");
        }
    }
    public void updateItemQuantity(Registered supplyManager, Shop shop, int itemID, int quantity) {
        if (supplyManager.hasPermission(shop.getId(), Permission.UPDATE_ITEM_QUANTITY)) {
            shop.updateItemQuantity(itemID, quantity);
        } else {
            System.out.println("You don't have permission to update item quantity");
        }
    }
    public void updateItemRating(Registered supplyManager, Shop shop, int itemID, double rating) {
        if (supplyManager.hasPermission(shop.getId(), Permission.UPDATE_ITEM_RATING)) {
            shop.updateItemRating(itemID, rating);
        } else {
            System.out.println("You don't have permission to update item rating");
        }
    }
    public void updateItemCategory(Registered supplyManager, Shop shop, int itemID, Category category) {
        if (supplyManager.hasPermission(shop.getId(), Permission.UPDATE_ITEM_DESCRIPTION)) {
            shop.updateItemCategory(itemID, category);
        } else {
            System.out.println("You don't have permission to update item category");
        }
    }
    //Not to forget purchase and sale policy
}
