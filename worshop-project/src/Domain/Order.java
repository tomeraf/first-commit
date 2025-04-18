package Domain;

import java.util.List;

public class Order {
    private List<ItemDTO> items;
    private int totalPrice;
    private int orderID;

    // items Map: <<itemID, shopID>, <price, quantity>>
    public Order(List<ItemDTO> items, int totalPrice, int orderID) {
        this.items = items;
        this.totalPrice = totalPrice;
        this.orderID = orderID;
    }

    public String getOrderDetails() {
        StringBuilder details = new StringBuilder("Order ID: " + orderID + "\n");
        details.append("Items:\n");
        for (ItemDTO item : items) {
            details.append("Item ID: ").append(item.getItemID()).append(", Name: ").append(item.getName())
                    .append(", Price: ").append(item.getPrice()).append(", Quantity: ").append(item.getQuantity())
                    .append("\n");
        }
        details.append("Total Price: ").append(totalPrice).append("\n");
        return details.toString();
    }
}
