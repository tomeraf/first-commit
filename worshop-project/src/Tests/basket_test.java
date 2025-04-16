package Tests;

import java.util.ArrayList;

public class basket_test {

    public static void main(String[] args) {
        // Create a ShoppingBasket object with a shopID and a list of items
        ArrayList<Integer> items = new ArrayList<>();
        items.add(101);
        items.add(102);
        items.add(103);
        Domain.ShoppingBasket basket = new Domain.ShoppingBasket(-1, items);
        
        // Test the getShopID method
        System.out.println("Shop ID: " + basket.getShopID() + ", Expected output: -1");
        
        // Test the getItems method
        System.out.println("Items: " + basket.getItems() + ", Expected output: [101, 102, 103]");
        
        // Test the isItemIn method
        System.out.println("Is item 102 in the basket? " + basket.isItemIn(102) + ", Expected output: true");
        System.out.println("Is item 104 in the basket? " + basket.isItemIn(104) + ", Expected output: false");

        // Test the removeItem method
        System.out.println("Removing item 102: " + basket.removeItem(102) + ", Expected output: true");
        System.out.println("Removing item 104: " + basket.removeItem(102) + ", Expected output: false");
        System.out.println("Items after removal: " + basket.getItems() + ", Expected output: [101, 103]");
    }
}
