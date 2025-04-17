package Tests;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class CartTest {
    
    private Domain.ShoppingCart cart;
    private Domain.ShoppingBasket basket1;
    private Domain.ShoppingBasket basket2;
    
    @BeforeEach
    public void setUp() {
        // Create ShoppingBasket objects with shopIDs and lists of items
        ArrayList<Integer> items_1 = new ArrayList<>();
        items_1.add(101);
        items_1.add(102);
        items_1.add(103);
        
        ArrayList<Integer> items_2 = new ArrayList<>();
        items_2.add(201);
        items_2.add(202);
        items_2.add(203);
        
        basket1 = new Domain.ShoppingBasket(-1, items_1);
        basket2 = new Domain.ShoppingBasket(-2, items_2);

        // Create a ShoppingCart object with a cartID and a list of baskets
        cart = new Domain.ShoppingCart(Arrays.asList(basket1, basket2), -1);
    }
    
    @Test
    public void testGetCartID() {
        assertEquals(-1, cart.getCartID(), "Cart ID should be -1");
    }
    
    @Test
    public void testGetItems() {
        Map<Integer, Integer> expectedItems = new HashMap<>();
        expectedItems.put(101, -1);
        expectedItems.put(102, -1);
        expectedItems.put(103, -1);
        expectedItems.put(201, -2);
        expectedItems.put(202, -2);
        expectedItems.put(203, -2);
        
        assertEquals(expectedItems, cart.getItems(), "Items in cart should match expected mapping of item IDs to shop IDs");
    }
    
    @Test
    public void testDeleteItemsSuccess() {
        Map<Integer, Integer> itemsToDelete = new HashMap<>();
        itemsToDelete.put(101, -1);
        itemsToDelete.put(201, -2);
        
        assertTrue(cart.deleteItems(itemsToDelete), "Should return true when successfully deleting items");
        
        // Verify items after deletion
        Map<Integer, Integer> expectedRemainingItems = new HashMap<>();
        expectedRemainingItems.put(102, -1);
        expectedRemainingItems.put(103, -1);
        expectedRemainingItems.put(202, -2);
        expectedRemainingItems.put(203, -2);
        
        assertEquals(expectedRemainingItems, cart.getItems(), "Cart should contain only remaining items after deletion");
    }
    
    @Test
    public void testDeleteItemsNotInCart() {
        // First delete the items so they're no longer in cart
        Map<Integer, Integer> itemsToDelete = new HashMap<>();
        itemsToDelete.put(101, -1);
        itemsToDelete.put(201, -2);
        cart.deleteItems(itemsToDelete);
        
        // Try to delete them again
        assertFalse(cart.deleteItems(itemsToDelete), "Should return false when trying to delete items not in cart");
    }
}