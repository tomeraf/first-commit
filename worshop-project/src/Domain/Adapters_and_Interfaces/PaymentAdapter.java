package Domain.Adapters_and_Interfaces;

import Domain.DTOs.PaymentDetailsDTO;

public class PaymentAdapter implements IPayment {

    private IPayment paymentMethod;

    // Dependency injection of the authentication implementation adapter
    // This allows for flexibility in choosing the payment method at runtime
    public PaymentAdapter(IPayment paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    // checking if the payment details are valid
    @Override
    public boolean validatePaymentDetails(PaymentDetailsDTO paymentDetails) {
        if (paymentDetails == null || !paymentDetails.fullDetails()) {
            // If payment details are null or not complete, return false
            return false;
        }
        return paymentMethod.validatePaymentDetails(paymentDetails);
    }
    
    // return the transactionId for good payment; return null for bad payment
    @Override
    public String processPayment(double price, PaymentDetailsDTO paymentDetails) {
        if (validatePaymentDetails(paymentDetails) == false) {
            // If payment details are not valid, return null
            return null;
        }
        return paymentMethod.processPayment(price, paymentDetails);
    }
}
