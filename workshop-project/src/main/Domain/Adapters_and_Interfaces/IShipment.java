package Domain.Adapters_and_Interfaces;

public interface IShipment {

    // checking if the shipment details are valid
    boolean validateShipmentDetails(String shipmentDetails);

    // return the transactionId for good shipment; return null for bad shipment
    String processShipment(double price, String shipmentDetails);
}
