package Domain.Shop.Discount;

import java.util.ArrayList;
import java.util.List;

import Domain.Rules.Rule;

public class DiscountPolicy {
    private List<DiscountType> discountTypes;
    private List<Rule> discountRules;
    
    public DiscountPolicy(){
        this.discountTypes = new ArrayList<>();
        this.discountRules = new ArrayList<>();
    }
    public void adddiscountRule(Rule discountRule) {
        this.discountRules.add(discountRule);
    }
    public void removediscountRule(Rule discountRule) {
        this.discountRules.remove(discountRule);
    }
    public void updateDiscountType(DiscountType discountType) {
        if(this.discountTypes.contains(discountType)) 
            this.discountTypes.remove(discountType);
        else
            this.discountTypes.add(discountType);
    }

}
