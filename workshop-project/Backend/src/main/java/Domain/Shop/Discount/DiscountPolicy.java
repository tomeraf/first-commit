package Domain.Shop.Discount;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import Domain.Rules.Rule;
import Domain.Shop.Item;
import Domain.Shop.Discount.BaseDiscount.*;
import Domain.Shop.Category;

public class DiscountPolicy {
    private List<DiscountType> discountTypes;
    private DiscountTree discountTree;
    
    public DiscountPolicy(){
        this.discountTypes = new ArrayList<>();
        this.discountTree = new DiscountTree();
    }
    public void addDiscount(int ancestor_id, Discount discount) {
        this.discountTree.addDiscount(ancestor_id, discount);
    }
    public void removeDiscount(int id) {
        this.discountTree.removeDiscount(id);
    }
    public double calculateDiscount(HashMap<Item,Integer> allItems) {
        return this.discountTree.calculateDiscount(allItems);
    }
    public void updateDiscountType(DiscountType discountType) {
        if(this.discountTypes.contains(discountType)) 
            this.discountTypes.remove(discountType);
        else
            this.discountTypes.add(discountType);
    }
    public void addDiscount(HashMap<String,String> discountDetails) {
        
    }
    public Discount parseDiscount(HashMap<String,String> discountDetails) {
        Discount discount;
        String d_type = discountDetails.get("discount type");
        if(d_type.equalsIgnoreCase("max")){
            Discount d1=parseDiscount(discountDetails,"1");
            Discount d2=parseDiscount(discountDetails,"2");
            discount = new MaxDiscount(d1,d2);
        }
        else if(d_type.equalsIgnoreCase("combined")){
            Discount d1=parseDiscount(discountDetails,"1");
            Discount d2=parseDiscount(discountDetails,"2");
            discount = new CombinedDiscount(d1,d2);
        }
        else{
            discount = parseDiscount(discountDetails,"1");
        }
        return discount;
    }
    public Discount parseDiscount(HashMap<String,String> discountDetails,String number){
        Discount discount=null;
        int percentage = Integer.parseInt(discountDetails.get("percentage"+number));

        return discount;
    }

}
