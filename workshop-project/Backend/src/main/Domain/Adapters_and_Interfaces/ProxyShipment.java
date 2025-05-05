package main.Domain.Adapters_and_Interfaces;

import main.Domain.DTOs.ShipmentDetailsDTO;

public class ProxyShipment implements IShipment {
    private final ShipmentDetailsDTO details;

    public ProxyShipment(ShipmentDetailsDTO details) {
        this.details = details;
      }

    // This method checks if the shipment details are valid
    @Override
    public boolean validateShipmentDetails() {
        return true;
    }

    // This method processes the shipment and returns a shipment ID if successful
    @Override
    public boolean processShipment(double price) {
        return true;
    }

    @Override
    public ShipmentDetailsDTO getShipmentDetails() {
        return details;
    }

}
