package Domain.Rules;

public class RuleFactory {

    //need to implement getAge method in the user class
    // public static Rule minUserAge(int minAge, int shopID) {
    //     return PredicateRule.fromUser(user -> user.getAge() >= minAge, shopID);
    // }
    //

    public static Rule minQuantity(int minQuantity, int shopID) {
        return PredicateRule.from((_, _, quantity) -> quantity >= minQuantity, shopID);
    }

    public static Rule maxQuantity(int maxQuantity, int shopID) {
        return PredicateRule.from((_, _, quantity) ->
            quantity <= maxQuantity, shopID);
    }

    public static Rule itemCategoryEquals(String expectedCategory, int shopID) {
        return PredicateRule.fromItem(item ->
            item.getCategory().equalsIgnoreCase(expectedCategory), shopID);
    }
    public static Rule specficItem(int itemID, int shopID) {
        return PredicateRule.fromItem(item ->
            item.getId() == itemID, shopID);
    }
    // need to implement getTotalPrice method in the user class
    // public static Rule minPurchaseAmount(double minAmount, int shopID) {
    //     return PredicateRule.fromUser(user ->
    //         user.getCart().getBasket(shopID).getTotalPrice() >= minAmount, shopID);
    // }

}

