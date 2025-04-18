package Tests;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import Domain.ItemDTO;
import Domain.Category;

public class BasketTest {
    
    private Domain.ShoppingBasket basket;
    private ItemDTO item1;
    private ItemDTO item2;
    private ItemDTO item3;
    private ItemDTO itemNotInBasket;
    
    @BeforeEach
    public void setUp() {
        // Create ItemDTO objects
        item1 = new ItemDTO("Item 1", Category.BEAUTY, 10.0, -1, 101, 5, 4.5);
        item2 = new ItemDTO("Item 2", Category.BEAUTY, 10.0, -1, 102, 5, 4.5);
        item3 = new ItemDTO("Item 3", Category.BEAUTY, 10.0, -1, 103, 5, 4.5);
        itemNotInBasket = new ItemDTO("Item 4", Category.BEAUTY, 10.0, -1, 104, 5, 4.5);
        
        // Create a ShoppingBasket object with a shopID and a list of items
        ArrayList<ItemDTO> items = new ArrayList<>();
        items.add(item1);
        items.add(item2);
        items.add(item3);
        basket = new Domain.ShoppingBasket(-1, items);
    }
    
    @Test
    public void testGetShopID() {
        assertEquals(-1, basket.getShopID(), "Shop ID should be -1");
    }
    
    @Test
    public void testGetItems() {
        ArrayList<ItemDTO> expectedItems = new ArrayList<>();
        expectedItems.add(item1);
        expectedItems.add(item2);
        expectedItems.add(item3);
        assertEquals(expectedItems, basket.getItems(), "Items list should match the initial values");
    }
    
    @Test
    public void testIsItemIn() {
        assertTrue(basket.isItemIn(item2.getItemID()), "Should confirm item2 is in the basket");
        assertFalse(basket.isItemIn(itemNotInBasket.getItemID()), "Should confirm itemNotInBasket is not in the basket");
    }
    
    @Test
    public void testRemoveItem() {
        // Test successful removal
        assertTrue(basket.removeItem(item2.getItemID()), "Should return true when removing existing item2");
        
        // Test removing an item that's no longer in the basket
        assertFalse(basket.removeItem(item2.getItemID()), "Should return false when removing non-existent item2");
        
        // Verify the basket contents after removal
        ArrayList<ItemDTO> expectedItems = new ArrayList<>();
        expectedItems.add(item1);
        expectedItems.add(item3);
        assertEquals(expectedItems, basket.getItems(), "Basket should contain only item1 and item3 after removal");
    }
}