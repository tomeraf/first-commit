package Domain.DomainServices;

import Domain.DTOs.ItemDTO;
import Domain.DTOs.ShopDTO;
import Domain.Guest;
import Domain.Repositories.IShopRepository;
import Domain.ShoppingBasket;
import Domain.ShoppingCart;
import Domain.Item;

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

    public int PurchaseCart(Guest user, IShopRepository shops) {
        // Use case #2.5: Purchase cart
        ShoppingCart cart = user.getCart();
        int price = 0;
        if (!cart.getItems().isEmpty())
        {
            for (ShoppingBasket basket: cart.getBaskets())
            {
                int shopId = basket.getShopID();
                ShopDTO shop = shops.getShopById(shopId);
                shop

            }
        }
        else
            System.out.println("Purchase failed. Your cart is empty.");

        return price;
    }

}
