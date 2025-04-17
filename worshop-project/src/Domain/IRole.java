package Domain;
import java.util.Map;
public interface IRole {

    //Item management
    void addItem(String sessionToken, String shopId, String item);
    void removeItem(String sessionToken, String shopId, String itemId);
    void updateItem(String sessionToken, String shopId, String itemId, String updatedItem);

    //Purchase/discount type management
    void addPurchaseType(String sessionToken, String shopId, String typeDetails);
    void addDiscountType(String sessionToken, String shopId, String typeDetails);
    void removePurchaseType(String sessionToken, String shopId, String purchaseTypeId);
    void removeDiscountType(String sessionToken, String shopId, String discountTypeId);
    void addPurchasePolicy(String sessionToken, String shopId, String policyDetails);
    void addDiscountPolicy(String sessionToken, String shopId, String policyDetails);
    void updatePurchasePolicy(String sessionToken, String shopId, String policyId);
    void updateDiscountPolicy(String sessionToken, String shopId, String policyId);
    
    //Notifications and purchase history
    Map<String, Permissions> getShopMembersInfo(String sessionToken, String shopId);
    void respondToMessage(String sessionToken, String shopId, String userId, String message);
    String viewPurchaseHistory(String sessionToken, String shopId);
}
