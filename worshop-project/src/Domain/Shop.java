import java.util.HashMap;
import java.util.Set;

import jdk.jshell.spi.ExecutionControl.NotImplementedException;

public class Shop {

    private int id;
    private String name;
    private String description;
    private PurchasePolicy purchasePolicy;
    private DiscountPolicy discountPolicy;
    private HashMap<Integer, Item> items; // itemId -> item
    private boolean isOpen;

    public Shop(int id, String name, String description, int founderId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.purchasePolicy = new PurchasePolicy();
        this.discountPolicy = new DiscountPolicy();
        this.items = new HashMap<>();
        this.isOpen = false;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public boolean isOpen() { return isOpen; }
    public PurchasePolicy getPurchasePolicy() { return purchasePolicy; }
    public DiscountPolicy getDiscountPolicy() { return discountPolicy; }
    public HashMap<Integer, Item> getItems() { return items; }

    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setPurchasePolicy(PurchasePolicy purchasePolicy) { this.purchasePolicy = purchasePolicy; }
    public void setDiscountPolicy(DiscountPolicy discountPolicy) { this.discountPolicy = discountPolicy; }
    public void setOpen(boolean isOpen) { this.isOpen = isOpen; }

    public void addItem(Item item) {
        if (items.containsKey(item.getId())) {
            System.out.println("Item ID already exists in the shop.");
        }
        else if (item.getShopId() != this.id) {
            System.out.println("Item does not belong to this shop.");
        }
        else{
            items.put(item.getId(), item);
        }
    }
    
    public void removeItem(Item item){
        if (items.containsKey(item.getId())) {
            items.remove(item.getId());
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

    public void updateItemQuantity(int itemId, int quantity) {
        if (items.containsKey(itemId)) {
            Item item = items.get(itemId);
            item.setQuantity(quantity);
        } 
        else {
            System.out.println("Item ID does not exist in the shop.");
        }
    }

    public void updateItemPrice(int itemId, double price) {
        if (items.containsKey(itemId)) {
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
            item.setRating(rating);
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

    public boolean canPurchaseBasket(ShoppingBasket basket){
        if (!isOpen) {
            System.out.println("Shop is closed. Cannot purchase items.");
            return false;
        }
        if (basket.isEmpty()) {
            System.out.println("Shopping basket is empty. Cannot purchase items.");
            return false;
        }
        boolean result = true;
        for (Item item : basket.keySet()) {
            if (!items.containsKey(item.getId())) {
                System.out.println("Item " + item.getName() + " does not exist in the shop.");
                return false;
            }
            result = result && item.quantityCheck(basket.get(item)); //assuming basket.get(item) returns the quantity of the item wanting to purchase
        }
        return result;
    }

    public void purchaseBasket(ShoppingBasket basket){ //will need to be synchronized later on
        Set<Item> allItems = basket.keySet(); //assuming basket is a HashMap<Item, Integer> : item -> quantity
        for(Item item: allItems){
            item.buyItem(basket.get(item)); //assuming basket.get(item) returns the quantity of the item in the basket
        }
    }
}

