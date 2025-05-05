package main.Domain.Adapters_and_Interfaces;

import main.Domain.DTOs.PaymentDetailsDTO;

public interface IPayment {

    // checking if the payment details are valid
    boolean validatePaymentDetails();

    // return the transactionId for good payment; return null for bad payment
    boolean processPayment(double price);

    PaymentDetailsDTO getPaymentDetails();

}
