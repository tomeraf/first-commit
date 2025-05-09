package Domain.Shop.Discount.CompositeDiscount;

import java.util.HashMap;

import Domain.Shop.Discount.Discount;

public abstract class CompositeDiscount extends Discount {
    private HashMap<Integer,Discount> discounts;
    public CompositeDiscount(int discountId) {
        super(discountId);
        this.discounts = new HashMap<>();
    }
    public boolean addDiscount(int ancestor_id,Discount discount) {
        if(ancestor_id==getDiscountId()){
            discounts.put(discount.getDiscountId(), discount);
            return true;
        }
        else{
            for(Discount d: discounts.values()){
                if(d.addDiscount(ancestor_id, discount)){
                    return true;
                }
            }
            return false;
        }
    }
    public boolean removeDiscount(int discountId) {
        if(discounts.containsKey(discountId)){
            discounts.remove(discountId);
            return true;
        }
        for(Discount d: discounts.values()){
            if(d.removeDiscount(discountId)){
                return true;
            }
        }
        return false;
        
    }
    public HashMap<Integer, Discount> getDiscounts() {
        return discounts;
    }



}
