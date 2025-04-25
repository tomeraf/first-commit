package Domain.DomainServices;

import Domain.*;
import Domain.DTOs.ItemDTO;
import Domain.DTOs.ShopDTO;
import Domain.Repositories.IShopRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PurchaseService {
    public void addItemToBasket(Guest user, List<ItemDTO> items)
    {
        // Use case #2.3: Add item to cart
        ShoppingCart cart = user.getCart();
        cart.addItems(items);
    }

    public void deleteItemsFromCart(Guest user, Map<Integer, Integer> items)
    {
        // Use case #2.4.b: Delete items from cart
        ShoppingCart cart = user.getCart();
        cart.deleteItems(items);
    }

    public boolean canPurchaseCart(Guest user, IShopRepository shops) {
        // Use case #2.5: Purchase cart
        // The price and all discounts with it should be calculated in the shop or
        // in the paymentService or in another function

        ShoppingCart cart = user.getCart();
        int price = 0;

        if (!cart.getItems().isEmpty())
        {
            for (ShoppingBasket basket: cart.getBaskets())
            {
                HashMap<Integer, Integer> items = new HashMap<>();
                for (ItemDTO item : basket.getItems())
                    items.put(item.getItemID(), item.getQuantity());
                int shopId = basket.getShopID();
                Shop shop = shops.getShopById(shopId);

                if(!shop.canPurchaseBasket(items))
                    return false;
            }
        }

        else {
            System.out.println("Purchase failed. Your cart is empty.");
            return false;
        }

        return true;
    }

}
