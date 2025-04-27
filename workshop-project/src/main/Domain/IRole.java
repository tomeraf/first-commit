package Domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
public abstract class IRole {
    protected int appointerID; //-1 for founder
    protected int shopID;
    protected Map<Integer,IRole> appointments;
    protected Registered user; // The user that has this role

    abstract boolean hasPermission(Permission permission);
    abstract void addPermission(Permission permission);
    abstract void removePermission(Permission permission);
    public void setUser(Registered user) {
        this.user = user;
    }
    //abstract void addAppointment(int nomineeID, IRole role );
    public void addManager(int nomineeID, IRole role) {
        if (hasPermission(Permission.APPOINTMENT)) {
            appointments.put(nomineeID, role);
        } else {
            System.out.println("No permission to add appointment");
        }
    }
    public void addOwner(int nomineeID, IRole role) {
        // Default implementation - only Owners should add Owners
        System.out.println("No permission to add owner");
    }
    
    public void removeAppointment(int appointeeID) {
        if (!appointments.containsKey(appointeeID)) {
            System.out.println("No appointment found for user ID: " + appointeeID);
            return;
        }
        appointments.get(appointeeID).removeAllAppointments();
        appointments.get(appointeeID).user.removeRoleFromShop(appointments.get(appointeeID).shopID);
        appointments.remove(appointeeID);
    }

    public void removeAllAppointments() {
        List<Integer> appointeeIDs = new ArrayList<>(appointments.keySet());
        for (int appointeeID : appointeeIDs) {
            appointments.get(appointeeID).removeAllAppointments();
            appointments.get(appointeeID).user.removeRoleFromShop(appointments.get(appointeeID).shopID);
            appointments.remove(appointeeID);
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
