package Tests;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import Domain.DTOs.ItemDTO;
import Domain.Category;
import Domain.ShoppingBasket;
import Domain.ShoppingCart;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CartTest {
    
    private ShoppingCart cart;
    private ShoppingBasket basket1;
    private ShoppingBasket basket2;
    private ItemDTO item1, item2, item3, item4, item5, item6;
    
    @BeforeEach
    public void setUp() {
        // Create ItemDTO objects
        item1 = new ItemDTO("Item 1", Category.BEAUTY, 10.0, -1, 101, 5, 4.5,"item1");
        item2 = new ItemDTO("Item 2", Category.BEAUTY, 10.0, -1, 102, 5, 4.5,"item2");
        item3 = new ItemDTO("Item 3", Category.BEAUTY, 10.0, -1, 103, 5, 4.5,"item3");
        item4 = new ItemDTO("Item 4", Category.BEAUTY, 10.0, -2, 201, 5, 4.5,"item4");
        item5 = new ItemDTO("Item 5", Category.BEAUTY, 10.0, -2, 202, 5, 4.5,"item5");
        item6 = new ItemDTO("Item 6", Category.BEAUTY, 10.0, -2, 203, 5, 4.5,"item6");
        
        // Create ShoppingBasket objects with shopIDs
        // and initialize them with empty item lists
        basket1 = new ShoppingBasket(-1);
        basket2 = new ShoppingBasket(-2);

        // Create a ShoppingCart object with a cartID and a list of baskets
        cart = new ShoppingCart(Arrays.asList(basket1, basket2), -1);
    }
    

    @Test
    public void testAddItemsSuccess() {
        List<ItemDTO> itemsToAdd = Arrays.asList(item1, item2, item3, item4, item5, item6);
        
        assertTrue(cart.addItems(itemsToAdd), "Should return true when adding items to the cart successfully");
        
        // Verify items in each basket after adding
        assertTrue(basket1.getItems().containsAll(Arrays.asList(item1, item2, item3)), "Basket 1 should contain items 1, 2, and 3");
        assertTrue(basket2.getItems().containsAll(Arrays.asList(item4, item5, item6)), "Basket 2 should contain items 4, 5, and 6");
    }

    @Test
    public void testGetCartID() {
        assertEquals(-1, cart.getCartID(), "Cart ID should be -1");
    }
    
    @Test
    public void testGetItems() {
        Map<ItemDTO, Integer> expectedItems = new HashMap<>();
        expectedItems.put(item1, -1);
        expectedItems.put(item2, -1);
        expectedItems.put(item3, -1);
        expectedItems.put(item4, -2);
        expectedItems.put(item5, -2);
        expectedItems.put(item6, -2);
        
        assertEquals(expectedItems, cart.getItems(), "Items in cart should match expected mapping of ItemDTO objects to shop IDs");
    }
    
    @Test
    public void testDeleteItemsSuccess() {
        Map<Integer, Integer> itemsToDelete = new HashMap<>();
        itemsToDelete.put(item1.getItemID(), item1.getShopId());
        itemsToDelete.put(item2.getItemID(), item2.getShopId());
        
        assertTrue(cart.deleteItems(itemsToDelete), "Should return true when successfully deleting items");
        
        // Verify items after deletion
        List<ItemDTO> expectedRemainingItems = new ArrayList<>();
        expectedRemainingItems.add(item3);
        expectedRemainingItems.add(item4);
        expectedRemainingItems.add(item5);
        expectedRemainingItems.add(item6);
        
        assertEquals(expectedRemainingItems, cart.getItems(), "Cart should contain only remaining items after deletion");
    }
    
    @Test
    public void testDeleteItemsNotInCart() {
        // First delete the items so they're no longer in cart
        Map<Integer, Integer> itemsToDelete = new HashMap<>();
        itemsToDelete.put(item1.getItemID(), item1.getShopId());
        itemsToDelete.put(item4.getItemID(), item4.getShopId());
        cart.deleteItems(itemsToDelete);
        
        // Try to delete them again
        assertFalse(cart.deleteItems(itemsToDelete), "Should return false when trying to delete items not in cart");
    }
}