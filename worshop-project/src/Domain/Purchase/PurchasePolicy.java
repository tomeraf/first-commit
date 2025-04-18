package Domain.Purchase;

import java.util.ArrayList;
import java.util.List;

import Domain.ShoppingBasket;

public class PurchasePolicy {
    private List<PurchaseType> purchaseTypes;
    private List<IPurchaseRole> purchaseRoles;
    

    public PurchasePolicy(){
        this.purchaseTypes = new ArrayList<>();
        this.purchaseRoles = new ArrayList<>();
    }
    public void addPurchaseType(PurchaseType purchaseType) {
        this.purchaseTypes.add(purchaseType);
    }
    public void addPurchaseRole(IPurchaseRole purchaseRole) {
        this.purchaseRoles.add(purchaseRole);
    }
    public void removePurchaseType(PurchaseType purchaseType) {
        this.purchaseTypes.remove(purchaseType);
    }
    public void removePurchaseRole(IPurchaseRole purchaseRole) {
        this.purchaseRoles.remove(purchaseRole);
    }
    //purchasing ShoppingBasket(need to discuss with the team how to integrate this with the purchase policy)
    
}
