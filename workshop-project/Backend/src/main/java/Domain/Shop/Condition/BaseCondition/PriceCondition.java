package Domain.Shop.Condition.BaseCondition;

import java.util.HashMap;

import Domain.Shop.Item;

public class PriceCondition extends BaseCondition {
    private double price;

    public PriceCondition(double price) {
        super();
        this.price = price;
    }

    public double getPrice() {
        return price;
    }

    @Override
    public boolean checkItemCondition(HashMap<Item, Integer> allItems) {
        for (Item item : allItems.keySet()) {
            if (item.getId() == getItemId()) {
                return item.getPrice()*allItems.get(item) >= price;
            }
        }
        return false;
    }
    @Override
    public boolean checkCategoryCondition(HashMap<Item, Integer> allItems) {
        double totalPrice = 0;
        for (Item item : allItems.keySet()) {
            if (item.getCategory().equals(getCategory())) {
                totalPrice += item.getPrice()*allItems.get(item);
            }
        }
        return totalPrice >= price;
    }
    @Override
    public boolean checkShopCondition(HashMap<Item, Integer> allItems) {
        double totalPrice = 0;
        for (Item item : allItems.keySet()) {
            totalPrice += item.getPrice()*allItems.get(item);
        }
        return totalPrice >= price;
    }

}
