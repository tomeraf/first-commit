package Domain;

import java.util.List;
import java.util.Map;

interface IRole {

    public void AddManager(int storeID, int managerID);
    public void AddOwner(int storeID, int ownerID);
    public void UpdateManagerPermissions(int storeID, List<Permissions> authorizations);
    public void RemoveOwnerAppointment(int storeID, int ownerIdToRemove);
    public void RemoveManagerAppointment(int storeID, int managerIdtoRemove);
    Map<List<String>, Map<String, List<Permissions>>> getShopMembersInfo(String shopIds);//<owners, <managers, permissions>>
    

    //for domain service?

    // void respondToMessage(String shopId, String userId, String message);
    // String viewPurchaseHistory(String shopId);

    // void updatePurchasePolicy(String shopId, String policyId);
    // void updateDiscountPolicy(String shopId, String policyId);

    // void addItem(String item);
    // void removeItem(String itemId);
    // void updateItem(String itemId, String updatedItem);
}



