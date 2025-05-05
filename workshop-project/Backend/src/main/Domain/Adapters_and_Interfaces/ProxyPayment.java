package main.Domain.Adapters_and_Interfaces;

import main.Domain.DTOs.PaymentDetailsDTO;

public class ProxyPayment implements IPayment {
    private final PaymentDetailsDTO details;

    public ProxyPayment(PaymentDetailsDTO details) {
        this.details = details;
      }

    // This method checks if the payment method is valid
    @Override
    public boolean validatePaymentDetails() {
        return true;
    }

    // This method processes the payment and returns a transaction ID if successful
    @Override
    public boolean processPayment(double price) {
        return true;
    }

    @Override
    public PaymentDetailsDTO getPaymentDetails() {
        return details;
    }
}
