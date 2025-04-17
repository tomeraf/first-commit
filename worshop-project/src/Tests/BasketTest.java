package Tests;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;

public class BasketTest {
    
    private Domain.ShoppingBasket basket;
    
    @BeforeEach
    public void setUp() {
        System.out.println("Test is running!");
        // Create a ShoppingBasket object with a shopID and a list of items
        ArrayList<Integer> items = new ArrayList<>();
        items.add(101);
        items.add(102);
        items.add(103);
        basket = new Domain.ShoppingBasket(-1, items);
    }
    
    @Test
    public void testGetShopID() {
        assertEquals(-1, basket.getShopID(), "Shop ID should be -1");
    }
    
    @Test
    public void testGetItems() {
        ArrayList<Integer> expectedItems = new ArrayList<>();
        expectedItems.add(101);
        expectedItems.add(102);
        expectedItems.add(103);
        assertEquals(expectedItems, basket.getItems(), "Items list should match the initial values");
    }
    
    @Test
    public void testIsItemIn() {
        assertTrue(basket.isItemIn(102), "Should confirm item 102 is in the basket");
        assertFalse(basket.isItemIn(104), "Should confirm item 104 is not in the basket");
    }
    
    @Test
    public void testRemoveItem() {
        // Test successful removal
        assertTrue(basket.removeItem(102), "Should return true when removing existing item 102");
        
        // Test removing an item that's no longer in the basket
        assertFalse(basket.removeItem(102), "Should return false when removing non-existent item 102");
        
        // Verify the basket contents after removal
        ArrayList<Integer> expectedItems = new ArrayList<>();
        expectedItems.add(101);
        expectedItems.add(103);
        assertEquals(expectedItems, basket.getItems(), "Basket should contain only items 101 and 103 after removal");
    }
}