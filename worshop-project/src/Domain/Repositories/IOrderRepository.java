package Domain.Repositories;

import java.util.List;

import Domain.Order;

public interface IOrderRepository {
    void addOrder(Order order);
    void removeOrder(int orderId);
    Order getOrder(int orderId);
    List<Order> getAllOrders();
    List<Order> getOrdersByCustomerId(int customerId);
    List<Order> getOrdersByShopId(int shopId);
}
