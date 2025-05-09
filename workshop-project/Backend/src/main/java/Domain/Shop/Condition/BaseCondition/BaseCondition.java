package Domain.Shop.Condition.BaseCondition;

import java.util.HashMap;
import Domain.Shop.Category;
import Domain.Shop.Item;
import Domain.Shop.Condition.Condition;

public abstract class BaseCondition implements Condition {
    private int itemId;
    private Category category;

    public BaseCondition(int itemId) {
        this.itemId = itemId;
        this.category = null;
    }
    public BaseCondition(Category category) {
        this.itemId = -1;
        this.category = category;
    }
    public BaseCondition() {
        this.itemId = -1;
        this.category = null;
    }
    public int getItemId() {
        return itemId;
    }
    public Category getCategory() {
        return category;
    }
    public String getType(){
        if (itemId != -1) {
            return "Item";
        } else if (category != null) {
            return "Category";
        } else {
            return "Shop";
        }
    }
    public boolean checkCondition(HashMap<Item, Integer> allItems){
        switch (getType()) {
            case "Item":
                return checkItemCondition(allItems);
            case "Category":
                return checkCategoryCondition(allItems);
            case "Shop":
                return checkShopCondition(allItems);
            default:
                return false;
        }
    }
    public abstract boolean checkItemCondition(HashMap<Item, Integer> allItems);
    public abstract boolean checkCategoryCondition(HashMap<Item, Integer> allItems);
    public abstract boolean checkShopCondition(HashMap<Item, Integer> allItems);

}
