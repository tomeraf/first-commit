package Domain.Repositories;

import java.util.HashMap;
import java.util.List;

import Domain.DTOs.Order;

public interface IOrderRepository {
    void addOrder(Order order);
    void removeOrder(int orderId);
    Order getOrder(int orderId);
    HashMap<Integer,Order> getAllOrders();
    List<Order> getOrdersByCustomerId(int customerId);
    List<Order> getOrdersByShopId(int shopId);
}
