package Domain.Shop.Discount;

import java.util.HashMap;

import Domain.Shop.*;

public interface Discount {
    public double calculateDiscount(HashMap<Item,Integer> allItems);


}
