package Domain;

import Domain.Discount.DiscountPolicy;
import Domain.Purchase.PurchasePolicy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Shop {

    private int id;
    private String name;
    private String description;
    private PurchasePolicy purchasePolicy;
    private DiscountPolicy discountPolicy;
    private HashMap<Integer, Item> items; // itemId -> item
    private boolean isOpen;
    private int counterItemId; // Counter for item IDs
    private double rating;
    private int ratingCount;

    public Shop(int id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.purchasePolicy = new PurchasePolicy();
        this.discountPolicy = new DiscountPolicy();
        this.items = new HashMap<>();
        this.isOpen = false;
        this.counterItemId = 1; // Initialize the item ID counter
        this.rating = 0.0;
        this.ratingCount = 0;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public boolean isOpen() { return isOpen; }
    public PurchasePolicy getPurchasePolicy() { return purchasePolicy; }
    public DiscountPolicy getDiscountPolicy() { return discountPolicy; }
    public HashMap<Integer, Item> getItems() { return items; }
    public double getRating() { return rating; }

    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setPurchasePolicy(PurchasePolicy purchasePolicy) { this.purchasePolicy = purchasePolicy; }
    public void setDiscountPolicy(DiscountPolicy discountPolicy) { this.discountPolicy = discountPolicy; }
    public void setOpen(boolean isOpen) { this.isOpen = isOpen; }

    public boolean addItem(String name, Category category, double price){
        if (price < 0) {
            System.out.println("Item price cannot be negative.");
            return false;
        } 
        else{
            Item item = new Item(name, category, price, this.id, counterItemId);
            items.put(item.getId(), item);
            counterItemId++; // Increment the item ID counter for the next item
            return true;
        }
    }
    
    public void removeItem(int itemId){
        if (items.containsKey(itemId)) {
            items.remove(itemId);
        } 
        else{
            System.out.println("Item ID does not exist in the shop.");
        }
    }
    
    public void updateItemName(int itemId, String name) {
        if (items.containsKey(itemId)) {
            Item item = items.get(itemId);
            item.setName(name);
        } 
        else{
            System.out.println("Item ID does not exist in the shop.");
        }
    }

    public boolean updateItemQuantity(int itemId, int quantity) {
        if (items.containsKey(itemId)){
            Item item = items.get(itemId);
            return item.updateQuantity(quantity);
        } 
        else {
            System.out.println("Item ID does not exist in the shop.");
            return false;
        }
    }

    public void updateItemPrice(int itemId, double price) {
        if (items.containsKey(itemId)){
            Item item = items.get(itemId);
            item.setPrice(price);
        } 
        else{
            System.out.println("Item ID does not exist in the shop.");
        }
    }

    public void updateItemRating(int itemId, double rating) {
        if (items.containsKey(itemId)) {
            Item item = items.get(itemId);
            item.updateRating(rating);
        } 
        else{
            System.out.println("Item ID does not exist in the shop.");
        }
    }

    public void updateItemCategory(int itemId, Category category) {
        if (items.containsKey(itemId)) {
            Item item = items.get(itemId);
            item.setCategory(category);
        } 
        else{
            System.out.println("Item ID does not exist in the shop.");
        }
    }

    public void updateRating(double rating) {
        if (rating < 0 || rating > 5) {
            System.out.println("Can't rate, rating must be between 0 and 5.");
        }
        else {
            ratingCount++;
            this.rating = (rating + this.rating) / ratingCount; // Update the shop's rating based on the new rating
        }
    }

    public void openShop(){ //must fix later on using synchronized methods
        if (isOpen) {
            System.out.println("Shop is already open.");
            return;
        }
        this.isOpen = true;
    }

    public void closeShop(){//must fix later on using synchronized methods
        if (!isOpen) {
            System.out.println("Shop is already closed.");
            return;
        }
        this.isOpen = false;
    }

    public boolean canAddItemToBasket(int itemId, int quantity) {
        if (items.containsKey(itemId) && items.get(itemId).quantityCheck(quantity)) {
            return true;
        }
        else if (!items.containsKey(itemId)) {
            System.out.println("Item does not exist.");
        } 
        else {
            System.out.println("Can't add "+ quantity + " of item: " + itemId +
                                    ", only "+ items.get(itemId).getQuantity() + " left in shop.");
        }
        return false;
    }

    public boolean canPurchaseBasket(HashMap <Integer, Integer> itemsToPurchase){ //itemId -> quantity
        if (!isOpen) {
            System.out.println("Shop is closed. Cannot purchase items.");
            return false;
        }
        if (itemsToPurchase.isEmpty()) {
            System.out.println("Shopping basket is empty. Cannot purchase items.");
            return false;
        }
        boolean result = true;

        for (Integer id : itemsToPurchase.keySet()) {
            if (!items.containsKey(id)) {
                System.out.println("Item " + id + " does not exist in the shop.");
                return false;
            }
            result = result && items.get(id).quantityCheck(itemsToPurchase.get(id)); //assuming basket.get(item) returns the quantity of the item wanting to purchase
        }
        return result;
    }

    public void purchaseBasket(HashMap <Integer, Integer> itemsToPurchase){ //will need to be synchronized later on
        List<Item> allItems = new ArrayList<Item>();
        for(Integer itemId: itemsToPurchase.keySet()){
            allItems.add(items.get(itemId)); 
        }
        for(Item item: allItems){
            item.buyItem(itemsToPurchase.get(item.getId())); 
        }
    }
}

