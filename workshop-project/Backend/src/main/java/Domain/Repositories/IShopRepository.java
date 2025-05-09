package Domain.Repositories;

import Domain.Shop.Shop;

import java.util.HashMap;

public interface IShopRepository {
    void addShop(Shop shop);
    Shop getShopById(int id);
    void updateShop(Shop shop);
    void deleteShop(int id);
    HashMap<Integer,Shop> getAllShops();

}
