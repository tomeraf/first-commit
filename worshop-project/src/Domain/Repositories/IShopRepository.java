package Domain.Repositories;

public class IShopRepository {
    void addShop(ShopDTO shop);
    ShopDTO getShopById(int id);
    void updateShop(ShopDTO shop);
    void deleteShop(int id);
    List<ShopDTO> getAllShops();

}
