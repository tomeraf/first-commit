package Domain.Shop.Discount;

import java.util.HashMap;

import Domain.Shop.Item;
import Domain.Shop.Discount.CompositeDiscount.CombinedDiscount;

public class DiscountTree {

    private Discount root;
    public DiscountTree() {
        this.root = new CombinedDiscount(0);
    }
    
    public Discount getRoot() {
        return root;
    }
    public void addDiscount(int ancestor_id,Discount discount) {
        if (root == null) {
            root = discount;
        } else {
            root.addDiscount(ancestor_id,discount);
        }
    }
    public void removeDiscount(int id) {
        if (root != null) {
            root.removeDiscount(id);
        }
    }
    public double calculateDiscount(HashMap<Item,Integer> allItems) {
        if (root != null) {
            return root.calculateDiscount(allItems);
        }
        return 0;
    }
}