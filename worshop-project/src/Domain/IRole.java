package Domain;

import java.util.Map;

interface IRole {

    //Item management
    void addItem(String item);
    void removeItem(String itemId);
    void updateItem(String itemId, String updatedItem);

    //Purchase/discount type management
    void addPurchaseType(String shopId, String typeDetails);
    void addDiscountType(String shopId, String typeDetails);
    void removePurchaseType(String shopId, String purchaseTypeId);
    void removeDiscountType( String shopId, String discountTypeId);
    void addPurchasePolicy(String shopId, String policyDetails);
    void addDiscountPolicy(String shopId, String policyDetails);
    void updatePurchasePolicy(String shopId, String policyId);
    void updateDiscountPolicy(String shopId, String policyId);
    
    //Notifications and purchase history
    String getShopMembersInfo(String shopId); 
    void respondToMessage(String shopId, String userId, String message);
    String viewPurchaseHistory(String shopId);
}



