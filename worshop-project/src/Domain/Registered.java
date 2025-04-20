package Domain;

import java.time.LocalDate;
import java.time.Period;

public class Registered extends Guest {
    private String username;
    private String password;
    private LocalDate dateOfBirth;
    public Registered(String username, String password) {
        this.username = username;
        this.password = password;
    }
    public String getUsername() {
        return username;
    }
    public String getPassword() {
        return password;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public void addItemToShop(Shop shop, String name, Category category, double price) {
        role.addItem(shop, name, category, price);
    }
    public void addItemToBasket(int itemID) {
        cart.addItem(itemID);
    }
    public void removeItemFromBasket(int itemID) {
        cart.removeItem(itemID);
    }
    public void updateItemName(Shop shop, int itemID, String name) {
        role.updateItemName(itemID, name);
    }
    public void updateItemPrice(Shop shop, int itemID, double price) {
        role.updateItemPrice(itemID, price);
    }
    public void updateItemQuantity(Shop shop, int itemID, int quantity) {
        role.updateItemName(itemID, quantity);
    }


    public int getAge() {
        return Period.between(dateOfBirth, dateOfBirth).getYears();
    }
}
