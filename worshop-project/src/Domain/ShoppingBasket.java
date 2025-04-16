package Domain;

import java.util.List;

public class ShoppingBasket {
    private int shopID;
    private List<Integer> items;


    public ShoppingBasket(int basketID, List<Integer> items) {
        this.shopID = basketID;
        this.items = items;
    }


    public ShoppingBasket(int basketID) {
        this.shopID = basketID;
        this.items = new java.util.ArrayList<>();
    }


    public int getShopID() {
        return shopID;
    }


    public List<Integer> getItems() {
        return items;
    }


    public boolean isItemIn(int itemID) {
        // Check if the itemID is in the list of items
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i) == itemID) {
                return true; // Item found
            }
        }
        return false; // Item not found
    }


    public boolean removeItem(int itemID) {
        // Check if the itemID is in the list of items
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i) == itemID) {
                items.remove(i);
                return true; // Item removed successfully
            }
        }
        return false; // Item not found, nothing removed
    }
}
