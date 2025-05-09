package Domain.Shop.Condition;

import java.util.HashMap;

import Domain.Shop.Item;

public interface Condition {
    public boolean checkCondition(HashMap<Item, Integer> allItems);
}
