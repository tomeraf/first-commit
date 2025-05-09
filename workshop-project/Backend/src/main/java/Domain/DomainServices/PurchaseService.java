package Domain.DomainServices;

import Domain.Adapters_and_Interfaces.IPayment;
import Domain.Adapters_and_Interfaces.IShipment;
import Domain.DTOs.ItemDTO;

import Domain.Shop.*;
import Domain.User.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import Domain.DTOs.Order;
import Domain.DTOs.Pair;

public class PurchaseService {
    // Use case #2.3: Add item to cart
    // items = <shop, <itemId, quantity>>
    public void addItemsToCart(Guest user, HashMap<Shop,HashMap<Integer,Integer>> items) {
        ShoppingCart cart = user.getCart();
        List<ItemDTO> itemDTOs = new ArrayList<>();
        for (Shop shop : items.keySet()) {
            HashMap<Integer, Integer> itemsMap = items.get(shop);
            if(shop.canAddItemsToBasket(itemsMap)){
                for(Integer itemId : itemsMap.keySet()) {
                    Item item = shop.getItem(itemId);
                    int quantity = itemsMap.get(itemId);
                    itemDTOs.add(new ItemDTO(item.getName(), item.getCategory(), item.getPrice(), shop.getId(), itemId, quantity, item.getRating(), item.getDescription()));
                }
            }
            else {
                throw new IllegalArgumentException("Error: cant add items.");
            }
        }
        cart.addItems(itemDTOs);
    }

    // Use case #2.4.b: Delete items from cart
    public void removeItemsFromCart(Guest user, HashMap<Integer,List<Integer>> items)
    {
        ShoppingCart cart = user.getCart();
        cart.deleteItems(items);
    }
    
    // use case 2.5
    public Order buyCartContent(Guest user, List<Shop> shops, IShipment ship, IPayment pay,int orderID ) {
        if (shops.isEmpty()) {
            throw new IllegalArgumentException("Error: no shops to purchase from.");    
        }
        ShoppingCart cart = user.getCart();
        List<ItemDTO> items = cart.getItems();
        HashMap<Shop, HashMap<Integer, Integer>> itemsToBuy = new HashMap<>();
        for (ItemDTO item : items) {
            Shop shop = null;
            for (Shop s : shops) {
                if (s.getId() == item.getShopId()) {
                    shop = s;
                    break;
                }
            }
            if (shop != null) {
                if (!itemsToBuy.containsKey(shop)) {
                    itemsToBuy.put(shop, new HashMap<>());
                }
                itemsToBuy.get(shop).put(item.getItemID(), item.getQuantity());
            }
        }
        if(!(ship.validateShipmentDetails()& pay.validatePaymentDetails())){
            throw new IllegalArgumentException("Error: cant validate payment or shipment details.");
        }
        for(Shop shop : itemsToBuy.keySet()) {
            HashMap<Integer, Integer> itemsMap = itemsToBuy.get(shop);
            if(!shop.canPurchaseBasket(itemsMap)) {
                throw new IllegalArgumentException("Error: cant purchase items.");
            }
        }
        Double total = 0.0;
        for(Shop shop : itemsToBuy.keySet()) {
            HashMap<Integer, Integer> itemsMap = itemsToBuy.get(shop);
            total=+shop.purchaseBasket(itemsMap);
        }
        HashMap<Integer, List<ItemDTO>> itemsToShip = new HashMap<>();
        for(Shop shop : itemsToBuy.keySet()) {
            HashMap<Integer, Integer> itemsMap = itemsToBuy.get(shop);
            List<ItemDTO> itemsList = new ArrayList<>();
            for(Integer itemId : itemsMap.keySet()) {
                Item item = shop.getItem(itemId);
                itemsList.add(new ItemDTO(item.getName(), item.getCategory(), item.getPrice(), shop.getId(), itemId, itemsMap.get(itemId), item.getRating(), item.getDescription()));
            }
            itemsToShip.put(shop.getId(), itemsList);
        }
        ship.processShipment(total*0.1);
        pay.processPayment(total);
        cart.clearCart();
        Order order = new Order(orderID, user.getUserID(), total, itemsToShip);
        return order;
    }


    public List<ItemDTO> checkCartContent(Guest user)
    {
        return user.getCart().getItems();
    }

    // public void directPurchase(Guest user, int itemId)
    // {
    //     // something with immediate purchase


    // }

    public void submitBidOffer(Guest user,Shop shop ,int itemId, double offer)
    {
        if(user instanceof Registered) {
            shop.addBidPurchase(itemId, offer,user.getUserID());
        }
        else {
            throw new IllegalArgumentException("Error: guest cannot submit bid.");
        }
    }

	public Order purchaseBidItem(Registered guest, Shop shop, int bidId,int orderID,IPayment pay,IShipment ship) {
        if(!(ship.validateShipmentDetails()& pay.validatePaymentDetails())){
            throw new IllegalArgumentException("Error: cant validate payment or shipment details.");
        }
		Pair<Integer,Double> offer = shop.purchaseBidItem(bidId, guest.getUserID());
        HashMap<Integer, List<ItemDTO>> itemsToShip = new HashMap<>();
        List<ItemDTO> itemsList = new ArrayList<>();
        Item item = shop.getItem(offer.getKey());
        itemsList.add(new ItemDTO(item.getName(), item.getCategory(), item.getPrice(), shop.getId(), offer.getKey(), 1, item.getRating(), item.getDescription()));
        itemsToShip.put(shop.getId(), itemsList);
        pay.processPayment(offer.getValue());
        ship.processShipment(offer.getValue()*0.1);
        Order order= new Order(orderID, guest.getUserID(), offer.getKey(), itemsToShip);
        return order;
    }

    public void submitAuctionOffer(Registered user, Shop shop, int auctionID, double offerPrice) {
        shop.submitAuctionOffer(auctionID, offerPrice, user.getUserID());
    }

    public Order purchaseAuctionItem(Registered user, Shop shop, int auctionID, int orderID, IPayment payment,
            IShipment shipment) {
        if(!(shipment.validateShipmentDetails()& payment.validatePaymentDetails())){
            throw new IllegalArgumentException("Error: cant validate payment or shipment details.");
        }
        Pair<Integer,Double> offer = shop.purchaseAuctionItem(auctionID, user.getUserID());
        HashMap<Integer, List<ItemDTO>> itemsToShip = new HashMap<>();
        List<ItemDTO> itemsList = new ArrayList<>();
        Item item = shop.getItem(offer.getKey());
        itemsList.add(new ItemDTO(item.getName(), item.getCategory(), item.getPrice(), shop.getId(), offer.getKey(), 1, item.getRating(), item.getDescription()));
        itemsToShip.put(shop.getId(), itemsList);
        payment.processPayment(offer.getValue());
        shipment.processShipment(offer.getValue()*0.1);
        Order order= new Order(offer.getKey(), user.getUserID(), offer.getValue(), itemsToShip);
        return order;
    }
}
