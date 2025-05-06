package com.halilovindustries.restservice;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import Domain.Response;
import Domain.Adapters_and_Interfaces.ConcurrencyHandler;
import Domain.Adapters_and_Interfaces.IAuthentication;
import Domain.Adapters_and_Interfaces.IPayment;
import Domain.Adapters_and_Interfaces.IShipment;
import Domain.Adapters_and_Interfaces.JWTAdapter;
import Service.OrderService;
import Service.ShopService;
import Service.UserService;
import Domain.Repositories.*;
import Infrastructure.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
//@RequestMapping("/api/user")
public class UserController {

    protected IShopRepository shopRepository;
    protected IUserRepository userRepository;
    protected IOrderRepository orderRepository;
    protected IAuthentication jwtAdapter;
    protected IShipment shipment;
    protected IPayment payment;
    protected UserService userService;
    protected ShopService shopService;
    protected OrderService orderService;
    protected ConcurrencyHandler concurrencyHandler;


    public UserController() {
        userRepository   = new MemoryUserRepository();
        orderRepository  = new MemoryOrderRepository();
        jwtAdapter       = new JWTAdapter();
        concurrencyHandler = new ConcurrencyHandler();
    
    
        userService  = new UserService(userRepository, jwtAdapter, concurrencyHandler);
        shopService  = new ShopService(userRepository, shopRepository, orderRepository, jwtAdapter, concurrencyHandler);
        orderService = new OrderService(userRepository, shopRepository, orderRepository, jwtAdapter, payment, shipment, concurrencyHandler);
    }
    
    

    @GetMapping("/enter")
    public Response<String> enterToSystem() {
        return userService.enterToSystem();
    }
    
}
