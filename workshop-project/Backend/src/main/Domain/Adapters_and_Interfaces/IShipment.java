package main.Domain.Adapters_and_Interfaces;

import main.Domain.DTOs.ShipmentDetailsDTO;

public interface IShipment {

    // checking if the shipment details are valid
    boolean validateShipmentDetails();

    // return the transactionId for good shipment; return null for bad shipment
    boolean processShipment(double price);

    ShipmentDetailsDTO getShipmentDetails();
}
