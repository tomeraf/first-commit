package Domain.Purchase;

import java.util.ArrayList;
import java.util.List;
import Domain.Rules.Rule;

public class PurchasePolicy {
    private List<PurchaseType> purchaseTypes;
    private List<Rule> purchaseRules;
    

    public PurchasePolicy(){
        this.purchaseTypes = new ArrayList<>();
        this.purchaseTypes.add(PurchaseType.BID);
        this.purchaseTypes.add(PurchaseType.AUCTION);
        this.purchaseTypes.add(PurchaseType.IMMEDIATE);
        this.purchaseRules = new ArrayList<>();
    }
    public void addPurchaseType(String purchaseType) {
        this.purchaseTypes.add(PurchaseType.fromString(purchaseType));
    }
    public void addPurchaseRole(Rule purchaseRule) {
        this.purchaseRules.add(purchaseRule);
    }
    public void removePurchaseType(String purchaseType) {
        this.purchaseTypes.remove(PurchaseType.fromString(purchaseType));
    }
    public void removePurchaseRole(Rule purchaseRule) {
        this.purchaseRules.remove(purchaseRule);
    }
    //need to implement to check that a basket is valid for purchase
    
}
