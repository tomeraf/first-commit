package Domain.DomainServices;

import Domain.DTOs.ItemDTO;
import Domain.Guest;
import Domain.Repositories.IShopRepository;
import Domain.ShoppingBasket;
import Domain.ShoppingCart;
import Domain.Item;

public class PurchaseService {

    public int PurchaseCart(Guest user, IShopRepository shops) {//return price if everything was fine
        //is puting repos like that ok?
        ShoppingCart cart = user.getCart();
        int price = 0;
        for (ItemDTO item : cart.getItems()) {
            //checking if item is in stock and exist
            //calculate the price with polices and discounts


        }
        return price;
    }

    public void addItemToBasket(Guest user, Item item)
    {
        // Use case #2.3: Add item to cart
        ShoppingCart cart = user.getCart();
        for (ShoppingBasket basket : cart.getBaskets()) {
            if (basket.getShopID() == item.getShopId()) {
                if (!basket.addItem(item)) {
                    // Item already exists in the basket
                    return;
                }
            }
        }
    }

    public void checkCartContent()
    {
        // Use case #2.4.a: Check cart content
    }

    public void editCartContent()
    {
        // Use case #2.4.b: Edit cart content
    }



}
