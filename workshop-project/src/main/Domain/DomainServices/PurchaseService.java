package Domain.DomainServices;

import Domain.*;
import Domain.DTOs.ItemDTO;
import Domain.DTOs.ShopDTO;
import Domain.Purchase.BidPurchase;
import Domain.Repositories.IShopRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import Domain.DTOs.Order;

public class PurchaseService {
    public void addItemsToCart(Guest user, List<Item> items)
    {
        // Use case #2.3: Add item to cart
        ShoppingCart cart = user.getCart();
        cart.addItems(convertToItemDTO(items));
    }

    public void removeItemsFromCart(Guest user, Map<Integer, Integer> items)
    {
        // Use case #2.4.b: Delete items from cart
        ShoppingCart cart = user.getCart();
        cart.deleteItems(items);
    }

    public boolean canPurchaseCart(Guest user,List<Shop> shops) {
        // Use case #2.5: Purchase cart

        ShoppingCart cart = user.getCart();

        if (!cart.getItems().isEmpty())
        {
            for (ShoppingBasket basket: cart.getBaskets())
            {
                int shopId = basket.getShopID();
                Shop currentShop = null;
                for (Shop shop: shops)
                    if(shop.getId() == shopId)
                        currentShop= shop;

                if (currentShop == null) {
                    System.out.println("Error: shop not found.");
                    return false;
                }

                HashMap<Integer, Integer> items = new HashMap<>();
                for (ItemDTO item : basket.getItems())
                    items.put(item.getItemID(), item.getQuantity());

                if(!currentShop.canPurchaseBasket(items))
                    return false;
            }
        }

        else {
            System.out.println("Purchase failed. Your cart is empty.");
            return false;
        }

        return true;
    }

    public Order buyCartContent(Guest user,List<Shop> shops)
    {
        ShoppingCart cart = user.getCart();
        int totalCost = 0;
        for (ShoppingBasket basket : cart.getBaskets()) {
            int shopId = basket.getShopID();
            Shop currentShop = null;
            for (Shop shop : shops)
                if (shop.getId() == shopId)
                    currentShop = shop;
            if (currentShop == null) {
                System.out.println("Error: shop not found.");
                return null;
            }

            HashMap<Integer, Integer> items = new HashMap<>();
            for (ItemDTO item : basket.getItems())
                items.put(item.getItemID(), item.getQuantity());

            totalCost += currentShop.purchaseBasket(items); // need to return cost

        }

        return new Order(cart.getItems(),totalCost,cart.getCartID()); // orderID???
    }


    public List<ItemDTO> checkCartContent(Guest user)
    {
        return user.getCart().getItems();
    }

    public void directPurchase(Guest user, int itemId)
    {
        // something with immediate purchase


    }

    public void submitBidOffer(Guest user, int itemId, double offer)
    {
        List<Item> items = convertToItem(user.getCart().getItems());
        for (Item item : items)
        {
            if (item.getId() == itemId)
            {
                // something with bid purchase
            }
        }
    }

    private List<ItemDTO> convertToItemDTO(List<Item> items)
    {
        List<ItemDTO> itemDTOs = items.stream()
                .map(item -> new ItemDTO(item.getName(), item.getCategory(), item.getPrice(), item.getShopId(), item.getId(), item.getQuantity(), item.getRating()))
                .toList();

        return itemDTOs;
    }

    private List<Item> convertToItem(List<ItemDTO> itemDTOs)
    {
        List<Item> items = itemDTOs.stream()
                .map(itemDTO -> new Item(itemDTO.getName(), itemDTO.getCategory(), itemDTO.getPrice(), itemDTO.getShopId(), itemDTO.getItemID()))
                .toList();

        return items;
    }

}
