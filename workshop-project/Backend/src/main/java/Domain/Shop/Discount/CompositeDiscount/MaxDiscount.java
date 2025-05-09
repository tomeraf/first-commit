package Domain.Shop.Discount.CompositeDiscount;

import java.util.HashMap;

import Domain.Shop.Item;
import Domain.Shop.Discount.Discount;

public class MaxDiscount extends CompositeDiscount {
    

    public MaxDiscount(int discountId) {
        super(discountId);
    }


    @Override
    public double calculateDiscount(HashMap<Item, Integer> allItems) {
        double maxDiscount = 0;
        for (Discount discount : getDiscounts().values()) {
            maxDiscount=Math.max(maxDiscount,discount.calculateDiscount(allItems));
        }
        return maxDiscount;
    }

}
