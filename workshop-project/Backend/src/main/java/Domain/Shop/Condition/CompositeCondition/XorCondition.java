package Domain.Shop.Condition.CompositeCondition;

import java.util.HashMap;
import Domain.Shop.Condition.Condition;
import Domain.Shop.Item;
public class XorCondition extends CompositeCondition {
    public XorCondition(Condition condition1, Condition condition2) {
        super(condition1, condition2);
    }

    @Override
    public boolean checkCondition(HashMap<Item, Integer> allItems) {
        return (getCondition1().checkCondition(allItems) && !getCondition2().checkCondition(allItems)) ||
               (!getCondition1().checkCondition(allItems) && getCondition2().checkCondition(allItems));
    }

}
