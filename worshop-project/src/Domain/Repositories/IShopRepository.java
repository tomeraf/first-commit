package Domain.Repositories;

import Domain.Shop;
import Domain.DTOs.ShopDTO;

import java.util.HashMap;

public interface IShopRepository {
    void addShop(Shop shop);
    Shop getShopById(int id);
    void updateShop(Shop shop);
    void deleteShop(int id);
    HashMap<Integer,Shop> getAllShops();

}
