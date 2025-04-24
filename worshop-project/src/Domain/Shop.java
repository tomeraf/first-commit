package Domain;

import java.time.LocalDateTime;

import Domain.Discount.DiscountPolicy;
import Domain.Purchase.PurchasePolicy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import Domain.Discount.DiscountPolicy;
import Domain.Purchase.AuctionPurchase;
import Domain.Purchase.BidPurchase;
import Domain.Purchase.PurchasePolicy;
import jdk.jshell.spi.ExecutionControl.NotImplementedException;


public class Shop {

    private int id;
    private String name;
    private String description;
    private PurchasePolicy purchasePolicy;
    private DiscountPolicy discountPolicy;
    private HashMap<Integer, Item> items; // itemId -> item
    private Set<Integer> ownerIds;
    private Set<Integer> managerIds;
    private boolean isOpen;
    private int counterItemId; // Counter for item IDs
    private double rating;
    private int ratingCount;
    private HashMap<Integer, BidPurchase> bidPurchaseItems; // BidId -> BidPurchase
    private HashMap<Integer, AuctionPurchase> auctionPurchaseItems; // AuctionId -> AuctionPurchase
    private int bidPurchaseCounter; // Counter for bid purchases
    private int auctionPurchaseCounter; // Counter for auction purchases

    public Shop(int id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.purchasePolicy = new PurchasePolicy();
        this.discountPolicy = new DiscountPolicy();
        this.items = new HashMap<>();
        this.ownerIds = new HashSet<>();
        /////////
        /// TODO: add ownerId to the set of owners
        /////////
        this.managerIds = new HashSet<>(); 
        this.isOpen = false;
        this.counterItemId = 1; // Initialize the item ID counter
        this.rating = 0.0;
        this.ratingCount = 0;
        this.bidPurchaseItems = new HashMap<>();
        this.auctionPurchaseItems = new HashMap<>();
        this.bidPurchaseCounter = 1; 
        this.auctionPurchaseCounter = 1; 
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public boolean isOpen() { return isOpen; }
    public PurchasePolicy getPurchasePolicy() { return purchasePolicy; }
    public DiscountPolicy getDiscountPolicy() { return discountPolicy; }
    public HashMap<Integer, Item> getItems() { return items; }
    public Set<Integer> getOwnerIds() { return ownerIds; }
    public Set<Integer> getManagerIds() { return managerIds; }
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

// not sure if needed - need to check
    public void addBidPurchase(int itemId, double bidAmount, int buyerId) {  
        if (items.containsKey(itemId)) {
            BidPurchase bidPurchase = new BidPurchase(bidPurchaseCounter, bidAmount, itemId, buyerId, buyerId);
            bidPurchaseCounter++;
            bidPurchaseItems.put(bidPurchase.getId(), bidPurchase);
        } else {
            System.out.println("Item ID does not exist in the shop.");
        }
    }

// not sure if needed - need to check
    public void addAuctionPurchase(int itemId, double startingAmount, LocalDateTime startTime, LocalDateTime endTime){
        if (items.containsKey(itemId) && items.get(itemId).getQuantity() > 0) {
            AuctionPurchase auctionPurchase = new AuctionPurchase(auctionPurchaseCounter, startingAmount, itemId, startTime, endTime);
            auctionPurchaseItems.put(auctionPurchase.getId(), auctionPurchase);
            auctionPurchaseCounter++;
        } 
        else if (!items.containsKey(itemId)) {
            System.out.println("Item ID does not exist in the shop.");
        }
        else {
            System.out.println("Item is out of stock.");
        }
    }

    public void addOwner(int ownerId) {
        if (!ownerIds.contains(ownerId)) {
            ownerIds.add(ownerId);
        } else {
            System.out.println("Owner ID already exists in the shop.");
        }
    }

    public void removeOwner(int ownerId) {
        if (ownerIds.contains(ownerId)) {
            ownerIds.remove(ownerId);
        } else {
            System.out.println("Owner ID does not exist in the shop.");
        }
    }

    public void addManager(int managerId) {
        if (!managerIds.contains(managerId)) {
            managerIds.add(managerId);
        } else {
            System.out.println("Manager ID already exists in the shop.");
        }
    }

    public void removeManager(int managerId) {
        if (managerIds.contains(managerId)) {
            managerIds.remove(managerId);
        } else {
            System.out.println("Manager ID does not exist in the shop.");
        }
    }

    public void addBidDecision(int ownerId, int bidId, boolean decision) {
        if(!bidPurchaseItems.containsKey(bidId)) {
            System.out.println("Bid ID does not exist in the shop.");
        } 
        else {
            BidPurchase bidPurchase = bidPurchaseItems.get(bidId);
            bidPurchase.receiveDecision(ownerId, decision);
        }
    }

}

