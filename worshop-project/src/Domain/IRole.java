package Domain;

import java.util.List;
import java.util.Map;
public interface IRole {


   // Map<List<String>, Map<String, List<Permissions>>> getShopMembersInfo(String shopIds);//<owners, <managers, permissions>>

    // // void respondToMessage(String shopId, String userId, String message);
    // // String viewPurchaseHistory(String shopId);

    // // void updatePurchasePolicy(String shopId, String policyId);
    // // void updateDiscountPolicy(String shopId, String policyId);

    void addItem(Shop shop , String name, Category category, double price);//shop.addItem
    void removeItem(int itemId);//badket.removeItem

    void updatItemName(Shop shop, String itemId, String itemName);
    void updateItemPrice(Shop shop, String itemId, double itemPrice);
    void updateItemQuantity(Shop shop, String itemId, int itemQuantity);//shop.

    public void AddManager(Shop shop, int managerID);
    public void AddOwner(Shop shop, int ownerID);
    public void UpdateManagerPermissions(Shop shop, List<Permission> authorizations);
    public void RemoveOwner(Shop shop, int ownerIdToRemove);
    public void RemoveManager(Shop shop, int managerIdtoRemove);
}
