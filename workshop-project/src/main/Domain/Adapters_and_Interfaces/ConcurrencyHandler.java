package Domain.Adapters_and_Interfaces;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.springframework.stereotype.Component;

/**
 * Central registry for shop- and item-level locks.
 * Shared as a singleton bean via Spring.
 */
@Component
public class ConcurrencyHandler {
    // Read/write locks per shop
    private final ConcurrentHashMap<Integer, ReentrantReadWriteLock> shopLocks = new ConcurrentHashMap<>();
    // Fine-grained locks per item
    private final ConcurrentHashMap<String, ReentrantLock> itemLocks = new ConcurrentHashMap<>();

    private ReentrantReadWriteLock getShopRWLock(int shopId) {
        return shopLocks.computeIfAbsent(shopId, id -> new ReentrantReadWriteLock());
    }

    /**
     * For operations that only need to read/lock a single item concurrently
     * while blocking only when a full-shop write is in progress.
     */
    public Lock getShopReadLock(int shopId) {
        return getShopRWLock(shopId).readLock();
    }

    /**
     * For full-shop operations (e.g., supply updates, close-shop) that must be exclusive.
     */
    public Lock getShopWriteLock(int shopId) {
        return getShopRWLock(shopId).writeLock();
    }

    /**
     * For operations targeting a single item within a shop.
     */
    public ReentrantLock getItemLock(int shopId, int itemId) {
        String key = "shop:" + shopId + ":item:" + itemId;
        return itemLocks.computeIfAbsent(key, k -> new ReentrantLock());
    }
}
