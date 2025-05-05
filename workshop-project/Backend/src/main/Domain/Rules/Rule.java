package main.Domain.Rules;

import main.Domain.Guest;
import main.Domain.Item;

@FunctionalInterface
public interface Rule {
    boolean evaluate(Guest user, Item item, int quantity);

    // Add shopID getter
    default int getShopID() {
        return -1; // Default implementation (can be overridden)
    }
}

