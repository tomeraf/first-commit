package Domain;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;

public class ShoppingCart {
    private List<ShoppingBasket> baskets;
    private int cartID;


    public ShoppingCart(List<ShoppingBasket> baskets, int cartID) {
        this.baskets = baskets;
        this.cartID = cartID;
    }


    public ShoppingCart(int cartID) {
        this.cartID = cartID;
        this.baskets = new java.util.ArrayList<>();
    }


    public int getCartID() {
        return cartID;
    }


    // Use case #2.4.a: Check cart content
    // map<Integer, Integer> items: itemID, basketID
    public Map<Integer, Integer> getItems() {
        Map<Integer, Integer> items = new Hashtable<>(); // Create a new dictionary to store items

        // Iterate through each basket and add items to the dictionary
        // The key is the itemID and the value is the basketID
        for (int i = 0; i < baskets.size(); i++) {
            ShoppingBasket basket = baskets.get(i);
            for (int itemID : basket.getItems()) {
                items.put(itemID, basket.getShopID()); // Add itemID and basketID to the dictionary
            }
        }
        return items;
    }


    // Use case #2.4.b: Change cart content
    // map<Integer, Integer> items: itemID, basketID
    public boolean deleteItems(Map<Integer, Integer> items) {
        // Check if all items are in the baskets
        boolean itemFound = false;
        for (Map.Entry<Integer, Integer> entry : items.entrySet()) {
            int itemID = entry.getKey();
            for (ShoppingBasket basket : baskets) {
                if (basket.isItemIn(itemID) && basket.getShopID() == entry.getValue()) {
                    itemFound = true;
                }
            }
            if (!itemFound) {
                return false;
            }
            itemFound = false;
        }

        // Remove items from all baskets
        for (Map.Entry<Integer, Integer> entry : items.entrySet()) {
            int itemID = entry.getKey();
            for (ShoppingBasket basket : baskets) {
                if (!basket.removeItem(itemID) && basket.getShopID() == entry.getValue()) 
                    return false;
            }
        }
        return true;
    }
}


