package Domain;

import java.util.List;

public class ShoppingBasket {
    private int shopID;
    private List<ItemDTO> items;


    public ShoppingBasket(int basketID, List<ItemDTO> items) {
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


    public List<ItemDTO> getItems() {
        return items;
    }


    public boolean isItemIn(int itemID) {
        // Check if the itemID is in the list of items
        for (int i = 0; i < items.size(); i++) {
            int tempID = ((ItemDTO) items.get(i)).getItemID();
            if (tempID == itemID) {
                return true; // Item found
            }
        }
        return false; // Item not found
    }


    public boolean removeItem(int itemID) {
        // Check if the itemID is in the list of items
        for (int i = 0; i < items.size(); i++) {
            int tempID = ((ItemDTO) items.get(i)).getItemID();
            if (tempID == itemID) {
                items.remove(i);
                return true; // Item removed successfully
            }
        }
        return false; // Item not found, nothing removed
    }
}
