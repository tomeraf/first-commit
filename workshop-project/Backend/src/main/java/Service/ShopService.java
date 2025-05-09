package Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;

import Domain.Shop.*;
import Domain.DTOs.ItemDTO;
import Domain.User.*;
import Domain.Response;
import Domain.Adapters_and_Interfaces.ConcurrencyHandler;
import Domain.Adapters_and_Interfaces.IAuthentication;
import Domain.Adapters_and_Interfaces.IMessage;
import Domain.DTOs.Order;
import Domain.DTOs.ShopDTO;
import Domain.Discount.DiscountType;
import Domain.DomainServices.InteractionService;

import Domain.DomainServices.ManagementService;
import Domain.DomainServices.ShoppingService;
import Domain.Repositories.IOrderRepository;
import Domain.Repositories.IShopRepository;
import Domain.Repositories.IUserRepository;

public class ShopService {

    private IUserRepository userRepository;
    private IShopRepository shopRepository;
    private IOrderRepository orderRepository;
    private ManagementService managementService = ManagementService.getInstance();
    private ShoppingService shoppingService;
    private IAuthentication authenticationAdapter;
    private InteractionService interactionService = InteractionService.getInstance();
    private final ConcurrencyHandler concurrencyHandler;

    private static final Logger logger = LoggerFactory.getLogger(ShopService.class);

    public ShopService(IUserRepository userRepository, IShopRepository shopRepository, IOrderRepository orderRepository,
            IAuthentication authenticationAdapter, ConcurrencyHandler concurrencyHandler) {
        this.userRepository = userRepository;
        this.shopRepository = shopRepository;
        this.orderRepository = orderRepository;
        this.authenticationAdapter = authenticationAdapter;
        this.shoppingService = new ShoppingService();
        this.concurrencyHandler = concurrencyHandler;
    }

    public Response<List<ShopDTO>> showAllShops(String sessionToken) {
        if (!authenticationAdapter.validateToken(sessionToken)) {
            return Response.error("User not logged in");
        }
        ArrayList<Shop> s = new ArrayList<Shop>(
                shopRepository.getAllShops().values().stream().filter((shop) -> shop.isOpen()).toList());
        List<ShopDTO> shopDTOs = new ArrayList<>();
        for (Shop shop : s) {
            HashMap<Integer, Item> items = shop.getItems();
            HashMap<Integer, ItemDTO> itemDTOs = new HashMap<>();
            for (Item item : items.values()) {
                ItemDTO itemDTO = new ItemDTO(item.getName(), item.getCategory(), item.getPrice(), item.getShopId(),
                        item.getId(), item.getQuantity(), item.getRating(), item.getDescription());
                itemDTOs.put(item.getId(), itemDTO);
            }
            ShopDTO shopDTO = new ShopDTO(shop.getId(), shop.getName(), shop.getDescription(), itemDTOs,
                    shop.getRating(), shop.getRatingCount());
            shopDTOs.add(shopDTO);
        }
        return Response.ok(shopDTOs);
    }

    public Response<List<ItemDTO>> showShopItems(String sessionToken, int shopId) {
        try {
            if (!authenticationAdapter.validateToken(sessionToken)) {
                throw new Exception("User not logged in");
            }
            Shop shop = shopRepository.getShopById(shopId);
            List<Item> items = shop.getItems().values().stream().toList();
            List<ItemDTO> itemDTOs = new ArrayList<>();
            for (Item item : items) {
                ItemDTO itemDTO = new ItemDTO(item.getName(), item.getCategory(), item.getPrice(), item.getShopId(),
                        item.getId(), item.getQuantity(), item.getRating(), item.getDescription());
                itemDTOs.add(itemDTO);
            }
            return Response.ok(itemDTOs);
        } catch (Exception e) {
            logger.error(() -> "Error showing shop items: " + e.getMessage());
            return Response.error("Error: " + e.getMessage());
        }
    }

    public Response<List<ItemDTO>> filterItemsAllShops(String sessionToken, HashMap<String, String> filters) {
        try {
            if (!authenticationAdapter.validateToken(sessionToken)) {
                throw new Exception("User not logged in");
            }
            String category = filters.get("category");
            String name = filters.get("name");
            double minPrice = filters.get("minPrice") != null ? Double.parseDouble(filters.get("minPrice")) : 0;
            double maxPrice = filters.get("maxPrice") != null ? Double.parseDouble(filters.get("maxPrice")) : 0;
            int minRating = filters.get("minRating") != null ? Integer.parseInt(filters.get("minRating")) : 0;
            double shopRating = filters.get("shopRating") != null ? Double.parseDouble(filters.get("shopRating")) : 0;
            List<Item> filteredItems = new ArrayList<>();
            for (Shop shop : shopRepository.getAllShops().values()) {
                filteredItems.addAll(shop.filter(name, category, minPrice, maxPrice, minRating, shopRating));
            }
            List<ItemDTO> itemDTOs = new ArrayList<>();
            for (Item item : filteredItems) {
                ItemDTO itemDTO = new ItemDTO(item.getName(), item.getCategory(), item.getPrice(), item.getShopId(),
                        item.getId(), item.getQuantity(), item.getRating(), item.getDescription());
                itemDTOs.add(itemDTO);
            }
            return Response.ok(itemDTOs);
        } catch (Exception e) {
            logger.error(() -> "Error filtering items in all shops: " + e.getMessage());
            return Response.error("Error: " + e.getMessage());
        }
    }

    public Response<List<ItemDTO>> filterItemsInShop(String sessionToken, int shopId, HashMap<String, String> filters) {
        try {
            if (!authenticationAdapter.validateToken(sessionToken)) {
                throw new Exception("User not logged in");
            }
            String category = filters.get("category");
            String name = filters.get("name");
            double minPrice = filters.get("minPrice") != null ? Double.parseDouble(filters.get("minPrice")) : 0;
            double maxPrice = filters.get("maxPrice") != null ? Double.parseDouble(filters.get("maxPrice")) : 0;
            int minRating = filters.get("minRating") != null ? Integer.parseInt(filters.get("minRating")) : 0;
            List<Item> filteredItems = new ArrayList<>();
            Shop shop = shopRepository.getShopById(shopId);
            filteredItems.addAll(shop.filter(name, category, minPrice, maxPrice, minRating, 0));
            List<ItemDTO> itemDTOs = new ArrayList<>();
            for (Item item : filteredItems) {
                ItemDTO itemDTO = new ItemDTO(item.getName(), item.getCategory(), item.getPrice(), item.getShopId(),
                        item.getId(), item.getQuantity(), item.getRating(), item.getDescription());
                itemDTOs.add(itemDTO);
            }
            return Response.ok(itemDTOs);
        } catch (Exception e) {
            logger.error(() -> "Error filtering items in shop: " + e.getMessage());
            return Response.error("Error: " + e.getMessage());
        }
    }

    public Response<ShopDTO> createShop(String sessionToken, String name, String description) {
        Lock creationLock = concurrencyHandler.getGlobalShopCreationLock();
        try {
            if (!authenticationAdapter.validateToken(sessionToken)) {
                throw new Exception("User not logged in");
            }
            creationLock.lock();
            int userID = Integer.parseInt(authenticationAdapter.getUsername(sessionToken));
            Registered user = (Registered) userRepository.getUserById(userID);
            if(user.isSuspended()) {
                return Response.error("User is suspended");
            }
            Shop shop = managementService.createShop(shopRepository.getAllShops().size(),
                    user, name, description);
            shopRepository.addShop(shop);
            HashMap<Integer, Item> items = shop.getItems();
            HashMap<Integer, ItemDTO> itemDTOs = new HashMap<>();
            for (Item item : items.values()) {
                ItemDTO itemDTO = new ItemDTO(item.getName(), item.getCategory(), item.getPrice(), item.getShopId(),
                        item.getId(), item.getQuantity(), item.getRating(), item.getDescription());
                itemDTOs.put(item.getId(), itemDTO);
            }
            ShopDTO shopDto = new ShopDTO(shop.getId(), shop.getName(), shop.getDescription(), itemDTOs,
                    shop.getRating(), shop.getRatingCount());
            logger.info(() -> "Shop created: " + shopDto.getName() + " by user: " + userID);
            return Response.ok(shopDto);
        } catch (Exception e) {
            logger.error(() -> "Error creating shop: " + e.getMessage());
            return Response.error("Error: " + e.getMessage());
        } finally {
            creationLock.unlock();
        }
    }

    public Response<ShopDTO> getShopInfo(String sessionToken, int shopID) {
        try {
            if (!authenticationAdapter.validateToken(sessionToken)) {
                throw new Exception("User is not logged in");
            }
            int userID = Integer.parseInt(authenticationAdapter.getUsername(sessionToken));
            Registered user = (Registered) this.userRepository.getUserById(userID);
            if(user.isSuspended()) {
                return Response.error("User is suspended");
            }
            Shop shop = this.shopRepository.getShopById(shopID);
            HashMap<Integer, ItemDTO> itemDTOs = new HashMap();
            for (Item item : shop.getItems().values()) {
                ItemDTO itemDTO = new ItemDTO(item.getName(), item.getCategory(), item.getPrice(), item.getShopId(),
                        item.getId(), item.getQuantity(), item.getRating(), item.getDescription());
                itemDTOs.put(item.getId(), itemDTO);
            }
            ShopDTO shopDto = new ShopDTO(shop.getId(), shop.getName(), shop.getDescription(), itemDTOs,
                    shop.getRating(), shop.getRatingCount());
            logger.info(() -> "Shop info retrieved: " + shopDto.getName() + " by user: "
                    + userRepository.getUserById(userID).getUsername());
            return Response.ok(shopDto);
        } catch (Exception e) {
            logger.error(() -> "Error retrieving shop info: " + e.getMessage());
            return Response.error("Error: " + e.getMessage());
        }
    }

    public Response<ItemDTO> addItemToShop(String sessionToken, int shopID, String itemName, Category category,
            double itemPrice, String description) {
        // need to add the Check if the user is logged in
        // If not, prompt to log in or register
        // If logged in, add the item to the shop with the provided details
        try {
            if (!authenticationAdapter.validateToken(sessionToken)) {
                throw new Exception("User is not logged in");
            }
            int userID = Integer.parseInt(authenticationAdapter.getUsername(sessionToken));
            Registered user = (Registered) this.userRepository.getUserById(userID);
            if(user.isSuspended()) {
                return Response.error("User is suspended");
            }
            Shop shop = this.shopRepository.getShopById(shopID);
            Item newItem = managementService.addItemToShop(user, shop, itemName, category, itemPrice, description);
            ItemDTO itemDto = new ItemDTO(newItem.getName(), newItem.getCategory(), newItem.getPrice(),
                    newItem.getShopId(), newItem.getId(), newItem.getQuantity(), newItem.getRating(),
                    newItem.getDescription());
            logger.info(
                    () -> "Item added to shop: " + itemName + " in shop: " + shop.getName() + " by user: " + userID);
            return Response.ok(itemDto);
        } catch (Exception e) {
            logger.error(() -> "Error adding item to shop: " + e.getMessage());
            return Response.error("Error: " + e.getMessage());
        }
    }

    public Response<Void> removeItemFromShop(String sessionToken, int shopID, int itemID) {
        // Check if the user is logged in
        // If not, prompt to log in or register
        // If logged in, remove the item from the shop with the provided details
        Lock shopRead = concurrencyHandler.getShopReadLock(shopID);
        ReentrantLock itemLock = concurrencyHandler.getItemLock(shopID, itemID);

        shopRead.lock();
        try {
            itemLock.lockInterruptibly();
            try {
                if (!authenticationAdapter.validateToken(sessionToken)) {
                    throw new Exception("User is not logged in");
                }
                int userID = Integer.parseInt(authenticationAdapter.getUsername(sessionToken));
                Registered user = (Registered) this.userRepository.getUserById(userID);
                if(user.isSuspended()) {
                    return Response.error("User is suspended");
                }
                Shop shop = shopRepository.getShopById(shopID);
                managementService.removeItemFromShop(user, shop, itemID);
                logger.info(() -> "Item removed from shop: " + itemID + " in shop: " + shop.getName() + " by user: "
                        + userID);
                return Response.ok();
            } catch (Exception e) {
                logger.error(() -> "Error removing item from shop: " + e.getMessage());
                return Response.error("Error: " + e.getMessage());
            } finally {
                itemLock.unlock();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Response.error("Operation interrupted while locking item.");
        } finally {
            shopRead.unlock();
        }
    }

    public Response<Void> changeItemQuantityInShop(String sessionToken, int shopID, int itemID, int newQuantity) {
        // Check if the user is logged in
        // If not, prompt to log in or register
        // If logged in, change the item quantity in the shop with the provided details
        Lock shopRead = concurrencyHandler.getShopReadLock(shopID);
        ReentrantLock itemLock = concurrencyHandler.getItemLock(shopID, itemID);

        shopRead.lock();
        try {
            itemLock.lockInterruptibly();
            try {
                if (!authenticationAdapter.validateToken(sessionToken)) {
                    throw new Exception("User is not logged in");
                }
                int userID = Integer.parseInt(authenticationAdapter.getUsername(sessionToken));
                Registered user = (Registered) this.userRepository.getUserById(userID);
                if(user.isSuspended()) {
                    return Response.error("User is suspended");
                }
                Shop shop = this.shopRepository.getShopById(shopID);
                managementService.updateItemQuantity(user, shop, itemID, newQuantity);
                logger.info(() -> "Item quantity changed in shop: " + itemID + " in shop: " + shop.getName()
                        + " by user: " + userID);
                return Response.ok();
            } catch (Exception e) {
                logger.error(() -> "Error changing item quantity in shop: " + e.getMessage());
                return Response.error("Error: " + e.getMessage());
            } finally {
                itemLock.unlock();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Response.error("Operation interrupted while locking item.");
        } finally {
            shopRead.unlock();
        }
    }

    public Response<Void> changeItemPriceInShop(String sessionToken, int shopID, int itemID, double newPrice) {
        // Check if the user is logged in
        // If not, prompt to log in or register
        // If logged in, change the item price in the shop with the provided details
        Lock shopRead = concurrencyHandler.getShopReadLock(shopID);
        ReentrantLock itemLock = concurrencyHandler.getItemLock(shopID, itemID);

        shopRead.lock();
        try {
            itemLock.lockInterruptibly();

            try {
                if (!authenticationAdapter.validateToken(sessionToken)) {
                    throw new Exception("User is not logged in");
                }
                int userID = Integer.parseInt(authenticationAdapter.getUsername(sessionToken));
                Registered user = (Registered) this.userRepository.getUserById(userID);
                if(user.isSuspended()) {
                    return Response.error("User is suspended");
                }
                Shop shop = shopRepository.getShopById(shopID);
                managementService.updateItemPrice(user, shop, itemID, newPrice);
                logger.info(() -> "Item price changed in shop: " + itemID + " in shop: " + shop.getName() + " by user: "
                        + userID);
                return Response.ok();
            } catch (Exception e) {
                logger.error(() -> "Error changing item price in shop: " + e.getMessage());
                return Response.error("Error: " + e.getMessage());
            } finally {
                itemLock.unlock();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Response.error("Operation interrupted while locking item.");
        } finally {
            shopRead.unlock();
        }

    }

    public Response<Void> changeItemDescriptionInShop(String sessionToken, int shopID, int itemID,
            String newDescription) {
        // Check if the user is logged in
        // If not, prompt to log in or register
        // If logged in, change the item name in the shop with the provided details
        Lock shopRead = concurrencyHandler.getShopReadLock(shopID);
        ReentrantLock itemLock = concurrencyHandler.getItemLock(shopID, itemID);

        shopRead.lock();
        try {
            itemLock.lockInterruptibly();

            try {
                if (!authenticationAdapter.validateToken(sessionToken)) {
                    throw new Exception("User is not logged in");
                }
                int userID = Integer.parseInt(authenticationAdapter.getUsername(sessionToken));
                Registered user = (Registered) this.userRepository.getUserById(userID);
                if(user.isSuspended()) {
                    return Response.error("User is suspended");
                }
                Shop shop = shopRepository.getShopById(shopID);
                managementService.updateItemDescription(user, shop, itemID, newDescription);
                logger.info(() -> "Item description changed in shop: " + itemID + " in shop: " + shop.getName()
                        + " by user: " + userID);
                return Response.ok();
            } catch (Exception e) {
                logger.error(() -> "Error changing item description in shop: " + e.getMessage());
                return Response.error("Error: " + e.getMessage());
            } finally {
                itemLock.unlock();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Response.error("Operation interrupted while locking item.");
        } finally {
            shopRead.unlock();
        }
    }

    public Response<Void> rateShop(String sessionToken, int shopID, int rating) {
        // If logged in, rate the shop with the provided rating
        try {
            if (!authenticationAdapter.validateToken(sessionToken)) {
                throw new Exception("User is not logged in");
            }

            int userID = Integer.parseInt(authenticationAdapter.getUsername(sessionToken));
            Registered user = (Registered) this.userRepository.getUserById(userID);
            if(user.isSuspended()) {
                return Response.error("User is suspended");
            }
            Shop shop = this.shopRepository.getShopById(shopID);
            List<Order> orders = orderRepository.getOrdersByCustomerId(userID);
            shoppingService.RateShop(shop, orders, rating);
            logger.info(() -> "Shop rated: " + shop.getName() + " by user: " + userID);
        } catch (Exception e) {
            logger.error(() -> "Error rating shop: " + e.getMessage());
            return Response.error("Error: " + e.getMessage());
        }
        return Response.ok();
    }

    public Response<Void> rateItem(String sessionToken, int shopID, int itemID, int rating) {
        // Check if the user is logged in
        // If not, prompt to log in or register
        // If logged in, rate the item with the provided rating
        try {
            if (!authenticationAdapter.validateToken(sessionToken)) {
                throw new Exception("User is not logged in");
            }
            int userID = Integer.parseInt(authenticationAdapter.getUsername(sessionToken));
            Registered user = (Registered) this.userRepository.getUserById(userID);
            if(user.isSuspended()) {
                return Response.error("User is suspended");
            }
            Shop shop = this.shopRepository.getShopById(shopID);
            List<Order> orders = orderRepository.getOrdersByCustomerId(userID);
            shoppingService.RateItem(shop, itemID, orders, rating);
            logger.info(() -> "Item rated: " + itemID + " in shop: " + shop.getName() + " by user: " + userID);
        } catch (Exception e) {
            logger.error(() -> "Error rating item: " + e.getMessage());
            return Response.error("Error: " + e.getMessage());
        }
        return Response.ok();
    }

    public Response<Void> updateDiscountType(String sessionToken, int shopID, DiscountType discountType) {
        // Check if the user is logged in
        // If not, prompt to log in or register
        // If logged in, update the discount type for the item in the shop with the
        // provided details
        try {
            if (!authenticationAdapter.validateToken(sessionToken)) {
                throw new Exception("User is not logged in");
            }
            int userID = Integer.parseInt(authenticationAdapter.getUsername(sessionToken));
            Registered user = (Registered) this.userRepository.getUserById(userID);
            if(user.isSuspended()) {
                return Response.error("User is suspended");
            }
            Shop shop = this.shopRepository.getShopById(shopID);
            this.managementService.updateDiscountType(user, shop, discountType);
            logger.info(() -> "Discount type updated in shop: " + shop.getName() + " by user: " + userID);
        } catch (Exception e) {
            logger.error(() -> "Error updating discount type: " + e.getMessage());
            return Response.error("Error: " + e.getMessage());
        }
        return Response.ok();
    }

    public Response<Void> updatePurchaseType(String sessionToken, int shopID, String purchaseType) {
        // Check if the user is logged in
        // If not, prompt to log in or register
        // If logged in, update the purchase type for the item in the shop with the
        // provided details
        try {
            if (!authenticationAdapter.validateToken(sessionToken)) {
                throw new Exception("User is not logged in");
            }
            int userID = Integer.parseInt(authenticationAdapter.getUsername(sessionToken));
            Registered user = (Registered) this.userRepository.getUserById(userID);
            if(user.isSuspended()) {
                return Response.error("User is suspended");
            }
            Shop shop = this.shopRepository.getShopById(shopID);
            this.managementService.updatePurchaseType(user, shop, purchaseType);
            logger.info(() -> "Purchase type updated in shop: " + shop.getName() + " by user: " + userID);
        } catch (Exception e) {
            logger.error(() -> "Error updating purchase type: " + e.getMessage());
            return Response.error("Error: " + e.getMessage());
        }
        return Response.ok();
    }

    public Response<Void> addShopOwner(String sessionToken, int shopID, String appointeeName) {
        ReentrantLock lock = concurrencyHandler.getShopUserLock(shopID, appointeeName);
        try {
            lock.lockInterruptibly();
            try {
                if (!authenticationAdapter.validateToken(sessionToken)) {
                    throw new Exception("User is not logged in");
                }
                int userID = Integer.parseInt(authenticationAdapter.getUsername(sessionToken));
                Registered user = (Registered) this.userRepository.getUserById(userID);
                if(user.isSuspended()) {
                    return Response.error("User is suspended");
                }
                Registered appointee = this.userRepository.getUserByName(appointeeName);
                if(appointee.isSuspended()) {
                    return Response.error("User is suspended");
                }
                Shop shop = this.shopRepository.getShopById(shopID);
                this.managementService.addOwner(user, shop, appointee);
                logger.info(() -> "Shop owner added: " + appointeeName + " in shop: " + shop.getName() + " by user: "
                        + userID);
            } catch (Exception e) {
                logger.error(() -> "Error adding shop owner: " + e.getMessage());
                return Response.error("Error: " + e.getMessage());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            // handle interruption
        } finally {
            lock.unlock();
        }
        return Response.ok();
    }

    public Response<Void> removeAppointment(String sessionToken, int shopID, String appointeeName) {
        ReentrantLock lock = concurrencyHandler.getShopUserLock(shopID, appointeeName);

        try {
            lock.lockInterruptibly();

            try {
                if (!authenticationAdapter.validateToken(sessionToken)) {
                    throw new Exception("User is not logged in");
                }
                int userID = Integer.parseInt(authenticationAdapter.getUsername(sessionToken));
                Registered user = (Registered) this.userRepository.getUserById(userID);
                if(user.isSuspended()) {
                    return Response.error("User is suspended");
                }
                Registered appointee = userRepository.getUserByName(appointeeName);
                if(appointee.isSuspended()) {
                    return Response.error("User is suspended");
                }
                Shop shop = shopRepository.getShopById(shopID);
                managementService.removeAppointment(user, shop, appointee);
            } catch (Exception e) {
                logger.error(() -> "Error removing appointment: " + e.getMessage());
                return Response.error("Error: " + e.getMessage());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            // handle interruption
        } finally {
            lock.unlock();
        }
        return Response.ok();
    }

    public Response<Void> addShopManager(String sessionToken, int shopID, String appointeeName,
            Set<Permission> permission) {
        ReentrantLock lock = concurrencyHandler.getShopUserLock(shopID, appointeeName);
        try {
            lock.lockInterruptibly();
            try {
                if (!authenticationAdapter.validateToken(sessionToken)) {
                    throw new Exception("User is not logged in");
                }
                int userID = Integer.parseInt(authenticationAdapter.getUsername(sessionToken));
                Registered user = (Registered) this.userRepository.getUserById(userID);
                if(user.isSuspended()) {
                    return Response.error("User is suspended");
                }
                Registered appointee = userRepository.getUserByName(appointeeName);
                if(appointee.isSuspended()) {
                    return Response.error("User is suspended");
                }
                Shop shop = shopRepository.getShopById(shopID);
                managementService.addManager(user, shop, appointee, permission);
                logger.info(() -> "Shop manager added: " + appointeeName + " in shop: " + shop.getName() + " by user: "
                        + userID);
            } catch (Exception e) {
                logger.error(() -> "Error adding shop manager: " + e.getMessage());
                return Response.error("Error: " + e.getMessage());
            } finally {
                lock.unlock();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            // handle interruption
        }
        return Response.ok();
    }

    public Response<Void> addShopManagerPermission(String sessionToken, int shopID, String appointeeName,
            Permission permission) {
        ReentrantLock lock = concurrencyHandler.getShopUserLock(shopID, appointeeName);
        try {
            lock.lockInterruptibly();
            try {
                if (!authenticationAdapter.validateToken(sessionToken)) {
                    throw new Exception("User is not logged in");
                }
                int userID = Integer.parseInt(authenticationAdapter.getUsername(sessionToken));
                Registered user = (Registered) this.userRepository.getUserById(userID);
                if(user.isSuspended()) {
                    return Response.error("User is suspended");
                }
                Registered appointee = userRepository.getUserByName(appointeeName);
                if(appointee.isSuspended()) {
                    return Response.error("User is suspended");
                }
                Shop shop = shopRepository.getShopById(shopID);
                managementService.addPermission(user, shop, appointee, permission);
                logger.info(() -> "Shop manager permission added: " + appointeeName + " in shop: " + shop.getName()
                        + " by user: " + userID);
            } catch (Exception e) {
                logger.error(() -> "Error adding shop manager permission: " + e.getMessage());
                return Response.error("Error: " + e.getMessage());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            // handle interruption
        } finally {
            lock.unlock();
        }
        return Response.ok();
    }

    public Response<Void> removeShopManagerPermission(String sessionToken, int shopID, String appointeeName,
            Permission permission) {
        ReentrantLock lock = concurrencyHandler.getShopUserLock(shopID, appointeeName);
        try {
            lock.lockInterruptibly();
            try {
                if (!authenticationAdapter.validateToken(sessionToken)) {
                    throw new Exception("User is not logged in");
                }
                int userID = Integer.parseInt(authenticationAdapter.getUsername(sessionToken));
                Registered user = (Registered) this.userRepository.getUserById(userID);
                if(user.isSuspended()) {
                    return Response.error("User is suspended");
                }
                Registered appointee = userRepository.getUserByName(appointeeName);
                if(appointee.isSuspended()) {
                    return Response.error("User is suspended");
                }
                Shop shop = shopRepository.getShopById(shopID);
                managementService.removePermission(user, shop, appointee, permission);
                logger.info(() -> "Shop manager permission removed: " + appointeeName + " in shop: " + shop.getName()
                        + " by user: " + userID);
            } catch (Exception e) {
                logger.error(() -> "Error removing shop manager permission: " + e.getMessage());
                return Response.error("Error: " + e.getMessage());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            // handle interruption
        } finally {
            lock.unlock();
        }
        return Response.ok();
    }

    public Response<Void> closeShop(String sessionToken, int shopID) {
        // Check if the user is logged in
        // If not, prompt to log in or register
        // If logged in, close the shop with the provided details
        try {
            Lock shopWrite = concurrencyHandler.getShopWriteLock(shopID);
            shopWrite.lock();
            try {
                // now exclusive: no reads or other writes
                if (!authenticationAdapter.validateToken(sessionToken)) {
                    throw new Exception("User is not logged in");
                }
                int userID = Integer.parseInt(authenticationAdapter.getUsername(sessionToken));
                Registered user = (Registered) this.userRepository.getUserById(userID);
                if(user.isSuspended()) {
                    return Response.error("User is suspended");
                }
                Shop shop = shopRepository.getShopById(shopID);
                managementService.closeShop(user, shop);
                logger.info(() -> "Shop closed: " + shop.getName() + " by user: " + userID);
                return Response.ok();
            } finally {
                shopWrite.unlock();
            }
        } catch (Exception e) {
            logger.error(() -> "Error closing shop: " + e.getMessage());
            return Response.error("Error: " + e.getMessage());
        }
    }

    public Response<String> getMembersPermissions(String sessionToken, int shopID) {
        try {
            if (!authenticationAdapter.validateToken(sessionToken)) {
                throw new Exception("User is not logged in");
            }
            int userID = Integer.parseInt(authenticationAdapter.getUsername(sessionToken));
            Registered user = (Registered) this.userRepository.getUserById(userID);
            if(user.isSuspended()) {
                return Response.error("User is suspended");
            }
            Shop shop = shopRepository.getShopById(shopID);
            List<Integer> membersUserIds = managementService.getMembersPermissions(user, shop);
            StringBuilder permissions = new StringBuilder();
            for (int memberId : membersUserIds) {
                Registered member = (Registered) userRepository.getUserById(memberId);
                permissions.append(member.getUsername()).append(": ");
                permissions.append(member.getPermissions(shopID)).append("\n");
            }
            logger.info(() -> "Members permissions retrieved in shop: " + shop.getName() + " by user: " + userID);
            return Response.ok(permissions.toString());
        } catch (Exception e) {
            logger.error(() -> "Error retrieving members permissions: " + e.getMessage());
            return Response.error("Error: " + e.getMessage());
        }
    }

    public Response<Void> sendMessage(String sessionToken, int shopId, String title, String content) {
        try {
            if (!authenticationAdapter.validateToken(sessionToken)) {
                throw new Exception("User is not logged in");
            }
            int userID = Integer.parseInt(authenticationAdapter.getUsername(sessionToken));
            Registered user = (Registered) this.userRepository.getUserById(userID);
            if(user.isSuspended()) {
                return Response.error("User is suspended");
            }
            Shop shop = shopRepository.getShopById(shopId);
            int newMessageId = shop.getNextMessageId();
            IMessage message = interactionService.createMessage(newMessageId, shop.getId(), shop.getName(),
                    shop.getId(), title, content);
            interactionService.sendMessage(shop, message);
            logger.info(() -> "Message sent: " + message.getId() + " in shop: " + shop.getName() + " by user: "
                    + sessionToken);
            return Response.ok();
        } catch (Exception e) {
            logger.error(() -> "Error sending message: " + e.getMessage());
            return Response.error("Error: " + e.getMessage());
        }
    }

    public Response<Void> respondToMessage(String sessionToken, int shopId, int messageId, String title,
            String content) {
        ReentrantLock messageLock = concurrencyHandler.getBidLock(shopId, messageId);

        try {
            messageLock.lockInterruptibly();
            try {
                if (!authenticationAdapter.validateToken(sessionToken)) {
                    throw new Exception("User is not logged in");
                }
                String username = authenticationAdapter.getUsername(sessionToken);
                Registered user = userRepository.getUserByName(username);
                if(user.isSuspended()) {
                    return Response.error("User is suspended");
                }
                Shop shop = shopRepository.getShopById(shopId);
                int newMessageId = shop.getNextMessageId();
                IMessage parentMessage = shop.getAllMessages().get(messageId);
                IMessage responseMessage = interactionService.createMessage(newMessageId, shop.getId(), shop.getName(),
                        shop.getId(), title, content);
                interactionService.respondToMessage(user, parentMessage, responseMessage);
            } finally {
                messageLock.unlock();
            }
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            return Response.error("Thread was interrupted.");
        }

        catch (Exception e) {
            logger.error(() -> "Error responding to message: " + e.getMessage());
            return Response.error("Error: " + e.getMessage());
        }
        return Response.ok();
    }

    public Response<Void> answerBid(String sessionToken, int shopID, int bidID, boolean accept) {
        ReentrantLock bidLock = concurrencyHandler.getBidLock(shopID, bidID);

        try {
            bidLock.lockInterruptibly();
            try {
                if (!authenticationAdapter.validateToken(sessionToken)) {
                    throw new Exception("User is not logged in");
                }
                int userID = Integer.parseInt(authenticationAdapter.getUsername(sessionToken));
                Registered user = (Registered) this.userRepository.getUserById(userID);
                if(user.isSuspended()) {
                    return Response.error("User is suspended");
                }
                Shop shop = shopRepository.getShopById(shopID);
                managementService.answerBid(user, shop, bidID, accept);
                logger.info(() -> "Bid answered: " + bidID + " in shop: " + shop.getName() + " by user: " + userID);
                return Response.ok();
            } finally {
                bidLock.unlock();
            }
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            return Response.error("Thread was interrupted.");
        }

        catch (Exception e) {
            logger.error(() -> "Error answering bid: " + e.getMessage());
            return Response.error("Error: " + e.getMessage());
        }
    }

    public Response<Void> submitCounterBid(String sessionToken, int shopID, int bidID, double offerAmount) {
        ReentrantLock bidLock = concurrencyHandler.getBidLock(shopID, bidID);

        try {
            bidLock.lockInterruptibly();
            try {
                if (!authenticationAdapter.validateToken(sessionToken)) {
                    throw new Exception("User is not logged in");
                }
                int userID = Integer.parseInt(authenticationAdapter.getUsername(sessionToken));
                Registered user = (Registered) this.userRepository.getUserById(userID);
                if(user.isSuspended()) {
                    return Response.error("User is suspended");
                }
                Shop shop = shopRepository.getShopById(shopID);
                managementService.submitCounterBid(user, shop, bidID, offerAmount);
                logger.info(() -> "Counter bid submitted: " + bidID + " in shop: " + shop.getName() + " by user: "
                        + userID);
                return Response.ok();
            } finally {
                bidLock.unlock();
            }
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            return Response.error("Thread was interrupted.");
        } catch (Exception e) {
            logger.error(() -> "Error submitting counter bid: " + e.getMessage());
            return Response.error("Error: " + e.getMessage());
        }
    }

    public Response<HashMap<Integer, IMessage>> getInbox(int shopID) {
        try {
            return Response.ok(shopRepository.getShopById(shopID).getAllMessages());
        } catch (Exception e) {
            logger.error(() -> "Error getting all messages: " + e.getMessage());
            return Response.error("Error: " + e.getMessage());
        }
    }

    public Response<Void> openAuction(String sessionToken, int shopID, int itemID, double startingPrice,
            LocalDateTime startDate, LocalDateTime endDate) {
        try {
            if (!authenticationAdapter.validateToken(sessionToken)) {
                throw new Exception("User is not logged in");
            }
            int userID = Integer.parseInt(authenticationAdapter.getUsername(sessionToken));
            Registered user = (Registered) this.userRepository.getUserById(userID);
            if(user.isSuspended()) {
                return Response.error("User is suspended");
            }
            Shop shop = this.shopRepository.getShopById(shopID);
            managementService.openAuction(user, shop, itemID, startingPrice, startDate, endDate);
            logger.info(() -> "Auction opened: " + itemID + " in shop: " + shop.getName() + " by user: " + userID);
        } catch (Exception e) {
            logger.error(() -> "Error opening auction: " + e.getMessage());
            return Response.error("Error: " + e.getMessage());
        }
        return Response.ok();
    }
}
