package Domain.Repositories;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import Domain.DTOs.Order;

public class MemoryOrderRepository implements IOrderRepository {
    private HashMap<Integer, Order> orders = new HashMap<>();

    @Override
    public void addOrder(Order order) {
        orders.put(order.getId(), order);
    }

    @Override
    public void removeOrder(int orderId) {
        orders.remove(orderId);
    }

    @Override
    public Order getOrder(int orderId) {
        return orders.get(orderId);
    }

    @Override
    public HashMap<Integer, Order> getAllOrders() {
        return orders;
    }

    @Override
    public List<Order> getOrdersByUserName(String userName) {
        List<Order> orderList = new ArrayList<>();
        for (Order order : orders.values()) {
            if (order.getUserName().equals(userName)) {
                orderList.add(order);
            }
        }
        return orderList;
    }

    @Override
    public List<Order> getOrdersByShopId(int shopId) {
        List<Order> orderList = new ArrayList<>();
        for (Order order : orders.values()) {
            if (order.getShopId() == shopId) {
                orderList.add(order);
            }
        }
        return orderList;
    }


}
