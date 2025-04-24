package Domain.DomainServices;

import Domain.Guest;

public class PurchaseService {
    public void PurchaseProcess(Guest user, int CartID ){
        if(user.getCart().getCartID()!=CartID)
            System.out.println("Current cart is different from the cart you wish to purchase");


    }

}
