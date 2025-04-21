package Domain;

import java.util.ArrayList;
import java.util.List;

public class Owner implements IRole {
    private int appointerID; //-1 for founder
    private int shopID;
    private List<Integer> appointments;

    public Owner(int appointerID, int shopID) {
        this.appointerID = appointerID;
        this.shopID = shopID;
        this.appointments = new ArrayList<>();
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

    @Override
    public void AddAppointment(int nomineeID) {
        appointments.add(nomineeID);
    }

    @Override
    public void RemoveAppointment(int appointeeID) {
        appointments.remove(appointeeID);
    }


    @Override
    public List<Integer> getAppointments() {
        return appointments;
    }

    @Override
    public int getAppointer() {
        return appointerID;
    }

    @Override
    public List<Permission> getPermissions() {
        //the owner has all permissions there is no need to return nothing
        return null;
    }

    @Override
    public int getShopID() {
        return shopID;
    }

}

