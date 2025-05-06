package Domain.Adapters_and_Interfaces;

import Domain.DTOs.ShipmentDetailsDTO;

public class ShipmentAdapter implements IShipment {
    
    private final IShipment shipmentMethod;

    // Dependency injection of the shipment implementation adapter
    // This allows for flexibility in choosing the shipment method at runtime
    public ShipmentAdapter(IShipment shipmentMethod) {
        this.shipmentMethod = shipmentMethod;
    }

    // checking if the shipment details are valid
    @Override
    public boolean validateShipmentDetails() {
        if (shipmentMethod.getShipmentDetails() == null || !shipmentMethod.getShipmentDetails().fullShipmentDetails()) {
            // If shipment details are null or not complete, return false
            return false;
        }
        return shipmentMethod.validateShipmentDetails();
    }

    // return shipment id for good shipment; return null for bad shipment
    @Override
    public boolean processShipment(double price) {
        if (!validateShipmentDetails()) {
            // If shipment details are not valid, return null
            return false;
        }
        return shipmentMethod.processShipment(price);
    }

    @Override
    public ShipmentDetailsDTO getShipmentDetails() {
        return shipmentMethod.getShipmentDetails();
    }

}
