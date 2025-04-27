package Domain.Repositories;

import java.util.HashMap;

import Domain.Shop;

public class MemoryShopRepository implements IShopRepository {
    private HashMap<Integer, Shop> shops = new HashMap<>();

    @Override
    public void addShop(Shop shop) {
        shops.put(shop.getId(), shop);
    }

    @Override
    public Shop getShopById(int id) {
        return shops.get(id);
    }

    @Override
    public void updateShop(Shop shop) {
        shops.put(shop.getId(), shop);
    }

    @Override
    public void deleteShop(int id) {
        shops.remove(id);
    }

    @Override
    public HashMap<Integer, Shop> getAllShops() {
        return shops;
    }

}
