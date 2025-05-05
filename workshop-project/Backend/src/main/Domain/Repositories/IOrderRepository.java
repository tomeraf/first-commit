package main.Domain.Repositories;

import java.util.HashMap;
import java.util.List;

import main.Domain.DTOs.ItemDTO;
import main.Domain.DTOs.Order;

public interface IOrderRepository {
    void addOrder(Order order);
    void removeOrder(int orderId);
    Order getOrder(int orderId);
    HashMap<Integer,Order> getAllOrders();
    List<Order> getOrdersByCustomerId(int userID);
    List<ItemDTO> getOrdersByShopId(int shopId);
}