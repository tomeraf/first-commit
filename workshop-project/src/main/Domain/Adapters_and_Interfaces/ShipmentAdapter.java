package Domain.Adapters_and_Interfaces;

public class ShipmentAdapter implements IShipment {
    
    private IShipment shipmentMethod;

    // Dependency injection of the shipment implementation adapter
    // This allows for flexibility in choosing the shipment method at runtime
    public ShipmentAdapter(IShipment shipmentMethod) {
        this.shipmentMethod = shipmentMethod;
    }

    // checking if the shipment details are valid
    @Override
    public boolean validateShipmentDetails(String shipmentDetails) {
        if (shipmentDetails == null || shipmentDetails.isEmpty()) {
            // If shipment details are null or not complete, return false
            return false;
        }
        return shipmentMethod.validateShipmentDetails(shipmentDetails);
    }

    // return shipment id for good shipment; return null for bad shipment
    @Override
    public String processShipment(double price, String shipmentDetails) {
        if (!validateShipmentDetails(shipmentDetails)) {
            // If shipment details are not valid, return null
            return null;
        }
        return shipmentMethod.processShipment(price, shipmentDetails);
    }

}
