package Domain;

public class ItemDTO {
    private String name;
    private Category category;
    // for single item
    private double price;
    private int shopId;
    private int itemID;
    private int quantity;
    private double rating;

    public ItemDTO(String name,Category category, double price, int shopId, int itemID, int quantity, double rating) {
        this.name = name;
        this.category = category;
        this.price = price;
        this.shopId = shopId;
        this.itemID = itemID;
        this.quantity = quantity;
        this.rating = rating;
    }
    public String getName() {
        return name;
    }
    public Category getCategory() {
        return category;
    }
    public double getPrice() {
        return price;
    }
    public int getShopId() {
        return shopId;
    }
    public int getItemID() {
        return itemID;
    }
    public int getQuantity() {
        return quantity;
    }
    public double getRating() {
        return rating;
    }
}
