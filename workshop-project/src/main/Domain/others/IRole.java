package others;

import java.util.Map;
public abstract class IRole {
    protected int appointerID; //-1 for founder
    protected int shopID;
    protected Map<Integer,IRole> appointments;


    abstract boolean hasPermission(Permission permission);
    abstract void addPermission(Permission permission);
    abstract void removePermission(Permission permission);
    
    abstract void addAppointment(int nomineeID, IRole role );
    abstract void removeAppointment(int appointeeID);
    public void removeAllAppointments() {
        for (int appointeeID : appointments.keySet()) {
            appointments.get(appointeeID).removeAllAppointments();
        }
        appointments.clear();
    }

    
    abstract Map<Integer, IRole> getAppointments(); // Returns a list of all the appointments the role has made  
    public int getShopID() {
        return shopID;
    }
    public int getAppointer() {
        return appointerID;
    }
    abstract String getPermissionsString();

    
}
