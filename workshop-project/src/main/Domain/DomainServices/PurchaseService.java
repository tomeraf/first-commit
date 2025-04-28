package Domain.DomainServices;

import Domain.*;
import Domain.Adapters_and_Interfaces.IPayment;
import Domain.Adapters_and_Interfaces.IShipment;
import Domain.DTOs.ItemDTO;

import java.util.HashMap;
import java.util.List;
import Domain.DTOs.Order;

public class PurchaseService {
    // Use case #2.3: Add item to cart
    // items = <shop, <itemId, quantity>>
    public void addItemsToCart(Guest user, HashMap<Shop,HashMap<Integer,Integer>> items) {
        ShoppingCart cart = user.getCart();
        for (Shop shop : items.keySet()) {
            HashMap<Integer, Integer> itemsMap = items.get(shop);
            cart.addItems();
            cart.addBasket(basket);
        }
        cart.addItems(items);
    }
    {
        ShoppingCart cart = user.getCart();
        cart.addItems(convertToItemDTO(items));
    }

    // Use case #2.4.b: Delete items from cart
    public void removeItemsFromCart(Guest user, List<Item> items)
    {
        ShoppingCart cart = user.getCart();
        HashMap<Integer,Integer> itemsMap = new HashMap<>();
        for (Item item : items)
            itemsMap.put(item.getId(),item.getShopId());

        cart.deleteItems(itemsMap);
    }
    
    // Use case #2.5: Purchase cart
    public boolean canPurchaseCart(Guest user, List<Shop> shops) {

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
                    throw new IllegalArgumentException("Error: shop not found.");
                }

                HashMap<Integer, Integer> items = new HashMap<>();
                for (ItemDTO item : basket.getItems())
                    items.put(item.getItemID(), item.getQuantity());

                if(!currentShop.canPurchaseBasket(items))
                    return false;
            }
        }

        else {
            throw new IllegalArgumentException("Error: cart is empty.");
        }

        return true;
    }

    public Order buyCartContent(Guest user, List<Shop> shops, IShipment ship, IPayment pay ) {
        ShoppingCart cart = user.getCart();
        double totalCost = 0;
        for (ShoppingBasket basket : cart.getBaskets()) {
            int shopId = basket.getShopID();
            Shop currentShop = null;
            for (Shop shop : shops)
                if (shop.getId() == shopId)
                    currentShop = shop;
            if (currentShop == null) {
                throw new IllegalArgumentException("Error: shop not found.");
            }

            HashMap<Integer, Integer> items = new HashMap<>();
            for (ItemDTO item : basket.getItems())
                items.put(item.getItemID(), item.getQuantity());

            totalCost += currentShop.purchaseBasket(items); // need to return cost

        }

        if (pay.validatePaymentDetails() && ship.validateShipmentDetails()){
            pay.processPayment(totalCost);
            ship.processShipment(totalCost);
        }
        else
        {
            throw new IllegalArgumentException("Error: payment or shipment not valid.");
        }

        return new Order(cart.getCartID(),user.getUserID(),totalCost,OrderHash(cart));
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
                .map(item -> new ItemDTO(item.getName(), item.getCategory(), item.getPrice(), item.getShopId(), item.getId(), item.getQuantity(), item.getRating(), item.getDescription()))
                .toList();

        return itemDTOs;
    }

    private List<Item> convertToItem(List<ItemDTO> itemDTOs)
    {
        List<Item> items = itemDTOs.stream()
                .map(itemDTO -> new Item(itemDTO.getName(), itemDTO.getCategory(), itemDTO.getPrice(), itemDTO.getShopId(), itemDTO.getItemID(), itemDTO.getDescription()))
                .toList();

        return items;
    }

    private HashMap<Integer,List<ItemDTO>> OrderHash (ShoppingCart cart){
        HashMap<Integer,List<ItemDTO>> map = new HashMap<>();
        for (ShoppingBasket basket: cart.getBaskets())
            map.put(basket.getShopID(), basket.getItems());
        return map;
    }



}
