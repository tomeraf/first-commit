package Domain.Discount;

import java.util.HashMap;

import Domain.Item;

public interface Discount {
    public double calculateDiscount(HashMap<Item,Integer> allItems);


}
