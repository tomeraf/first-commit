package Domain.Shop.Discount;

import java.util.HashMap;

import Domain.Shop.*;

public abstract class Discount {
    private int discountId;
    public Discount(int discountId) {
        this.discountId = discountId;
    }
    public int getDiscountId() {
        return discountId;
    }
    public abstract boolean addDiscount(int ancestor_id, Discount discount);
    public abstract boolean removeDiscount(int discountId);
    public abstract double calculateDiscount(HashMap<Item,Integer> allItems);


}
