package Tests;

import java.util.ArrayList;

public class cart_test {

    public static void main(String[] args) {
        
        // Create a ShoppingBasket object with a shopID and a list of items
        ArrayList<Integer> items_1 = new ArrayList<>();
        items_1.add(101);
        items_1.add(102);
        items_1.add(103);
        ArrayList<Integer> items_2 = new ArrayList<>();
        items_2.add(201);
        items_2.add(202);
        items_2.add(203);
        Domain.ShoppingBasket basket1 = new Domain.ShoppingBasket(-1, items_1);
        Domain.ShoppingBasket basket2 = new Domain.ShoppingBasket(-2, items_2);

        // Create a ShoppingCart object with a cartID and a list of baskets
        Domain.ShoppingCart cart = new Domain.ShoppingCart(java.util.Arrays.asList(basket1, basket2), -1);

        
        // Test the getCartID method
        System.out.println("Cart ID: " + cart.getCartID() + ", Expected output: -1");
        
        // Test the getItems method
        System.out.println("Items in cart: " + cart.getItems() + ", Expected output: {101=-1, 102=-1, 103=-1, 201=-2, 202=-2, 203=-2}");
        
        // Test the deleteItems method
        java.util.Map<Integer, Integer> itemsToDelete = new java.util.HashMap<>();
        itemsToDelete.put(101, -1);
        itemsToDelete.put(201, -2);
        
        System.out.println("Deleting items: " + cart.deleteItems(itemsToDelete) + ", Expected output: true");
        
        // Check items after deletion
        System.out.println("Items in cart after deletion: " + cart.getItems() + ", Expected output: {102=-1, 103=-1, 202=-2, 203=-2}");

        // Test the deleteItems method with items not in the cart
        itemsToDelete = new java.util.HashMap<>();
        itemsToDelete.put(101, -1);
        itemsToDelete.put(201, -2);

        System.out.println("Deleting items: " + cart.deleteItems(itemsToDelete) + ", Expected output: false");
    }
}
