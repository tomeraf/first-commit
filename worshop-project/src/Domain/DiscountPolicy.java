package Domain;

import java.util.ArrayList;
import java.util.List;

public class DiscountPolicy {
    private List<IDiscountType> discountTypes;
    private List<IDiscountRole> discountRoles;
    
    public DiscountPolicy(){
        this.discountTypes = new ArrayList<>();
        this.discountRoles = new ArrayList<>();
    }
    public void addDiscountType(IDiscountType discountType) {
        this.discountTypes.add(discountType);
    }
    public void addDiscountRole(IDiscountRole discountRole) {
        this.discountRoles.add(discountRole);
    }
    public void removeDiscountType(IDiscountType discountType) {
        this.discountTypes.remove(discountType);
    }
    public void removeDiscountRole(IDiscountRole discountRole) {
        this.discountRoles.remove(discountRole);
    }

}
