package Domain.Purchase;

import java.util.ArrayList;
import java.util.List;

public class PurchasePolicy {
    private List<IPurchaseType> purchaseTypes;
    private List<IPurchaseRole> purchaseRoles;
    

    public PurchasePolicy(){
        this.purchaseTypes = new ArrayList<>();
        this.purchaseRoles = new ArrayList<>();
    }
    public void addPurchaseType(IPurchaseType purchaseType) {
        this.purchaseTypes.add(purchaseType);
    }
    public void addPurchaseRole(IPurchaseRole purchaseRole) {
        this.purchaseRoles.add(purchaseRole);
    }
    public void removePurchaseType(IPurchaseType purchaseType) {
        this.purchaseTypes.remove(purchaseType);
    }
    public void removePurchaseRole(IPurchaseRole purchaseRole) {
        this.purchaseRoles.remove(purchaseRole);
    }
    
}
