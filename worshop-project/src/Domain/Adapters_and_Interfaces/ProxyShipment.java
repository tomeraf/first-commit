package Domain.Adapters_and_Interfaces;

public class ProxyShipment implements IShipment {
    private String currentShipmentID;

    public ProxyShipment() {  
        this.currentShipmentID = "123";
      }

    // This method checks if the shipment details are valid
    @Override
    public boolean validateShipmentDetails(String shipmentDetails) {
        return true;
    }

    // This method processes the shipment and returns a shipment ID if successful
    @Override
    public String processShipment(double price, String shipmentDetails) {
        String shipmentID = currentShipmentID;
        int i = Integer.parseInt(this.currentShipmentID);
        i++;
        this.currentShipmentID = String.valueOf(i);
        return "IHx" + shipmentID;
    }

}
