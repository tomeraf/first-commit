package Domain.Repositories;

import Domain.DTOs.ShopDTO;

import java.util.HashMap;

public interface IShopRepository {
    void addShop(ShopDTO shop);
    ShopDTO getShopById(int id);
    void updateShop(ShopDTO shop);
    void deleteShop(int id);
    HashMap<Integer,ShopDTO> getAllShops();

}
