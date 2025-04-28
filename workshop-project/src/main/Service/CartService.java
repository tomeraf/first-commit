package Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;

import Domain.Guest;
import Domain.Item;
import Domain.Response;
import Domain.Shop;
import Domain.Adapters_and_Interfaces.ConcurrencyHandler;
import Domain.Adapters_and_Interfaces.IAuthentication;
import Domain.Adapters_and_Interfaces.IPayment;
import Domain.Adapters_and_Interfaces.IShipment;
import Domain.DTOs.ItemDTO;
import Domain.DTOs.Order;
import Domain.DTOs.PaymentDetailsDTO;
import Domain.DomainServices.PurchaseService;
import Domain.Repositories.IOrderRepository;
import Domain.Repositories.IShopRepository;
import Domain.Repositories.IUserRepository;


public class CartService {
    private PurchaseService purchaseService = new PurchaseService();
    private IUserRepository userRepository;
    private IShopRepository shopRepository;
    private IOrderRepository orderRepository;
    private IAuthentication jwtAdapter;
    private IPayment payment;
    private IShipment shipment;

    private final ConcurrencyHandler ConcurrencyHandler;
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    public CartService(IUserRepository userRepository, IShopRepository shopRepository, IOrderRepository orderRepository, IAuthentication jwtAdapter, IPayment payment, IShipment shipment,  ConcurrencyHandler concurrencyHandler) {
        this.userRepository = userRepository;
        this.shopRepository = shopRepository;
        this.orderRepository = orderRepository;
        this.jwtAdapter = jwtAdapter;
        this.payment = payment;
        this.shipment = shipment;
        this.ConcurrencyHandler = concurrencyHandler;
    }

    /**
     * Retrieves the contents of the user's shopping cart.
     *
     * @param sessionToken current session token
     * @return list of ItemDTOs in the cart, or null on error
     */
    public Response<List<ItemDTO>> checkCartContent(String sessionToken) {
        try {
            if (!jwtAdapter.validateToken(sessionToken)) {
                throw new Exception("User not logged in");
            }
            int userID = Integer.parseInt(jwtAdapter.getUsername(sessionToken));
            
            System.out.println("User ID: " + userID);
            
            Guest guest = userRepository.getUserById(userID);
            List<ItemDTO> itemDTOs = purchaseService.checkCartContent(guest);
            // List<ItemDTO> itemDTOs = items.stream()
            //         .map(item -> new ItemDTO(item.getName(), item.getCategory(), item.getPrice(), item.getShopId(), item.getId(), item.getQuantity(), item.getRating()))
            //         .toList(); // Convert Item to ItemDTO

            logger.info(() -> "All items were listed successfully");
            return Response.ok(itemDTOs);
        } 
        catch (Exception e) {
            logger.error(() -> "Error viewing cart: " + e.getMessage());
            return Response.error("Error viewing cart: " + e.getMessage());
        }
    }


    /**
     * Adds items to the user's shopping cart.
     *
     * @param sessionToken current session token
     * @param itemDTOs list of items to add
     */
    public Response<Void> addItemsToCart(String sessionToken, List<ItemDTO> itemDTOs) {
        try {
            if (!jwtAdapter.validateToken(sessionToken)) {
                throw new Exception("User not logged in");
            }
            int cartID = Integer.parseInt(jwtAdapter.getUsername(sessionToken));
            Guest guest = userRepository.getUserById(cartID); // Get the guest user by I
            List<Item> items = itemDTOs.stream()
                    .map(itemDTO -> new Item(itemDTO.getName(), itemDTO.getCategory(), itemDTO.getPrice(), itemDTO.getShopId(), itemDTO.getItemID(), itemDTO.getDescription()))
                    .toList(); // Convert ItemDTO to Item

            purchaseService.addItemsToCart(guest, items); // Add items to the cart

            logger.info(() -> "Items added to cart successfully");
            return Response.ok();
        } catch (Exception e) {
            logger.error(() -> "Error adding items to cart: " + e.getMessage());
            return Response.error("Error adding items to cart: " + e.getMessage());
        }
    }

    /**
     * Removes items from the user's shopping cart.
     *
     * @param sessionToken current session token
     * @param itemDTOs list of items to remove
     */
    public Response<Void> removeItemsFromCart(String sessionToken, List<ItemDTO> itemDTOs) {

        try {
            if (!jwtAdapter.validateToken(sessionToken)) {
                throw new Exception("User not logged in");
            }
            int cartID = Integer.parseInt(jwtAdapter.getUsername(sessionToken));
            Guest guest = userRepository.getUserById(cartID); // Get the guest user by I
            List<Item> items = itemDTOs.stream()
                    .map(itemDTO -> new Item(itemDTO.getName(), itemDTO.getCategory(), itemDTO.getPrice(), itemDTO.getShopId(), itemDTO.getItemID(), itemDTO.getDescription()))
                    .toList(); // Convert ItemDTO to Item

            purchaseService.removeItemsFromCart(guest, items); // Save the updated cart to the repository
            
            logger.info(() -> "Items removed from cart successfully");
            return Response.ok();
        } catch (Exception e) {
            logger.error(() -> "Error removing items from cart: " + e.getMessage());
            return Response.error("Error removing items from cart: " + e.getMessage());
        }
    }

    /**
     * Executes purchase of all items in the cart, creates and records an Order.
     *
     * @param sessionToken current session token
     * @return the created Order, or null on failure
     */
    public Response<Order> buyCartContent(String sessionToken,PaymentDetailsDTO paymentDetailsDTO, String shipmentDetails) {
        try {
            if (!jwtAdapter.validateToken(sessionToken)) {
                throw new Exception("User not logged in");
            }
            int cartID = Integer.parseInt(jwtAdapter.getUsername(sessionToken));
            Guest guest = userRepository.getUserById(cartID); // Get the guest user by I
            List<Shop> shops = new ArrayList<>();
            for (int i = 0; i < guest.getCart().getBaskets().size(); i++) {
                int shopID = guest.getCart().getBaskets().get(i).getShopID();
                Shop shop = shopRepository.getShopById(shopID); // Get the shop by ID
                shops.add(shop); // Add the shop to the list of shops
            }
            
            Order order = purchaseService.buyCartContent(guest, shops, shipment, payment,paymentDetailsDTO, shipmentDetails); // Buy the cart content
            orderRepository.addOrder(order); // Save the order to the repository
            logger.info(() -> "Purchase completed successfully for cart ID: " + cartID);
            return Response.ok(order); 
        } catch (Exception e) {
            logger.error(() -> "Error buying cart content: " + e.getMessage());
            return Response.error("Error buying cart content: " + e.getMessage());
        }
    }


    /**
     * Submits a bid offer for a specific item.
     *
     * @param sessionToken current session token
     * @param itemID the item to bid on
     * @param offerPrice the bid amount
     */
    public Response<Void> submitBidOffer(String sessionToken, int shopId, int itemID, double offerPrice) {

        Lock shopRead = ConcurrencyHandler.getShopReadLock(shopId);
        ReentrantLock itemLock = ConcurrencyHandler.getItemLock(shopId, itemID);

        shopRead.lock();     
        try {
            itemLock.lockInterruptibly();

            try {
                if (!jwtAdapter.validateToken(sessionToken)) {
                    throw new Exception("User not logged in");
                }
                int cartID = Integer.parseInt(jwtAdapter.getUsername(sessionToken));
                Guest guest = userRepository.getUserById(cartID); // Get the guest user by I
                purchaseService.submitBidOffer(guest, itemID, offerPrice);
    
                logger.info(() -> "Bid offer submitted successfully for item ID: " + itemID);
                return Response.ok();

            } 
            catch (Exception e) {
                logger.error(() -> "Error submitting bid offer: " + e.getMessage());
                return Response.error("Error submitting bid offer: " + e.getMessage());
            }
            finally {
                itemLock.unlock();
            }
        } 
        catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            // handle interruption…
        } finally {
            shopRead.unlock();
        }
        return null;
    }

    /**
     * Performs a direct purchase of a single item.
     *
     * @param sessionToken current session token
     * @param itemID the item to purchase
     */
    public Response<Void> directPurchase(String sessionToken, int shopId, int itemID) {
        Lock shopRead = ConcurrencyHandler.getShopReadLock(shopId);
        ReentrantLock itemLock = ConcurrencyHandler.getItemLock(shopId, itemID);

        shopRead.lock();     
        try {
            itemLock.lockInterruptibly();
            try {
                if (!jwtAdapter.validateToken(sessionToken)) {
                    throw new Exception("User not logged in");
                }
                int cartID = Integer.parseInt(jwtAdapter.getUsername(sessionToken));
                Guest guest = userRepository.getUserById(cartID); // Get the guest user by I
                purchaseService.directPurchase(guest, itemID);
    
                logger.info(() -> "Direct purchase completed successfully for item ID: " + itemID);
                return Response.ok();
            } catch (Exception e) {
                logger.error(() -> "Error completing direct purchase: " + e.getMessage());
                return Response.error("Error completing direct purchase: " + e.getMessage());
            }
            finally {
                itemLock.unlock();
            }
        } 
        catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            // handle interruption…
        } finally {
            shopRead.unlock();
        }
    }

}
