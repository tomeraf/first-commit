package Domain.Adapters_and_Interfaces;

import Domain.DTOs.PaymentDetailsDTO;

public class ProxyPayment implements IPayment {
    private String currentTransactionID;

    public ProxyPayment() {  
        this.currentTransactionID = "123";
      }

    // This method checks if the payment method is valid
    @Override
    public boolean validatePaymentDetails(PaymentDetailsDTO paymentDetails) {
        return true;
    }

    // This method processes the payment and returns a transaction ID if successful
    @Override
    public String processPayment(double price, PaymentDetailsDTO paymentDetails) {
        String transactionId = currentTransactionID;
        int i = Integer.parseInt(this.currentTransactionID);
        i++;
        this.currentTransactionID = String.valueOf(i);
        return transactionId;
    }
}
