package Domain;

public class Item {
    private String name;
    private Category category;
    private double price;
    private int quantity;
    private int shopId;
    private int id;
    private double rating;
    private int numOfOrders;

    public Item(String name,Category category, double price, int shopId) {
        this.name = name;
        this.category = category;
        this.price = price;
        this.shopId = shopId;
        this.quantity = 0;
        this.rating = 0.0;
        this.numOfOrders = 0;
    }

    public void updateRating(double newRating) {
        this.rating = (this.rating * numOfOrders + newRating) / (numOfOrders);
    }
    public void buyItem(int quantity) {
        if (this.quantity >= quantity) {
            this.quantity -= quantity;
            this.numOfOrders += 1;
        } else {
            System.out.println("Not enough stock available.");
        }
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public int getQuantity() {
        return quantity;
    }
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
    public int getShopId() {
        return shopId;
    }
    public double getPrice() {
        return price;
    }
    public void setPrice(double price){
        this.price=price;
    }
    public int getId() {
        return id;
    }

    public double getRating() {
        return rating;
    }
    public void setRating(double rating) {
        this.rating = rating;
    }
    public int getNumOfOrders(){
        return numOfOrders;
    }
    public void setNumOfOrders(int numOfOrders) {
        this.numOfOrders = numOfOrders;
    }
    public Category getCategory() {
        return category;
    }
    public void setCategory(Category category) {
        this.category = category;
    }

    public String toString() {
        return "Item{" +
                "name='" + name + '\'' +
                ", category=" + category +
                ", price=" + price +
                ", quantity=" + quantity +
                ", shopId=" + shopId +
                ", id=" + id +
                ", rating=" + rating +
                ", numOfOrders=" + numOfOrders +
                '}';
    }
}

