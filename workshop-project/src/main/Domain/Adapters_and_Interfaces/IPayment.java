package Domain.Adapters_and_Interfaces;

import Domain.DTOs.PaymentDetailsDTO;

public interface IPayment {

    // checking if the payment details are valid
    boolean validatePaymentDetails(PaymentDetailsDTO paymentDetails);

    // return the transactionId for good payment; return null for bad payment
    String processPayment(double price, PaymentDetailsDTO paymentDetails);
}
