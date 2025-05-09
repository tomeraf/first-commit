package Presenters;

import Domain.DTOs.ItemDTO;
import Domain.DTOs.ShopDTO;
import Domain.Response;
import Service.ShopService;
import Service.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class HomePresenter {
    UserService userService;
    ShopService shopService;
    List<ShopDTO> RandomShops;

    public HomePresenter(UserService userService, ShopService shopService) {
        this.userService = userService;
        this.shopService = shopService;
    }

    public void rnd3Shops() {
        List<ShopDTO> shops = shopService.showAllShops().getData();
        Random rand = new Random();
        int rndNum = rand.nextInt(shops.size());
        RandomShops = new ArrayList<>();
        for (int i = 0; i < 3; i++)
            RandomShops.add(shops.get(rndNum++ % shops.size()));
    }

    public List<String> get3rndShopsNames() {
        List<String> names = new ArrayList<>();
        for (ShopDTO shop : RandomShops)
            names.add(shop.getName());
        return names;
    }
    public List<ItemDTO> get4rndShopItems(ShopDTO shop) {
        List<ItemDTO> RandomItems = new ArrayList<>();
        Random rand = new Random();
        Object[] keys = shop.getItems().keySet().toArray();
        int rndNum = rand.nextInt(keys.length);
        for (int i = 0; i < 4; i++)
            RandomItems.add(shop.getItems().get((Integer)(keys[rndNum%keys.length])));
        return RandomItems;
    }

}
