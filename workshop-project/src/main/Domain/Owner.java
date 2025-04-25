package Domain;

import java.util.HashMap;
import java.util.Map;


public class Owner extends IRole {

    public Owner(int appointerID, int shopID) {
        this.appointerID = appointerID;
        this.shopID = shopID;
        this.appointments = new HashMap<>();
    }

    @Override
    public boolean hasPermission(Permission permission) {
        // Assuming the owner has all permissions
        return true;
    }

    @Override
    public void addPermission(Permission permission) {
        //No implementation needed for owner
    }

    @Override
    public void removePermission(Permission permission) {
        //No implementation needed for owner
    }

    
    public void addAppointment(int nomineeID, IRole role) {
        appointments.put(nomineeID, role);
    }

    @Override
    public void removeAppointment(int appointeeID) {
        if (!appointments.containsKey(appointeeID)) {
            System.out.println("No appointment found for user ID: " + appointeeID);
            return;
        }
        appointments.get(appointeeID).removeAllAppointments();
        appointments.remove(appointeeID);
    }

    @Override
    public Map<Integer,IRole> getAppointments() {
        return appointments;
    }

    public String getPermissionsString() {
        return "Owner - has all permissions";
    }
}

