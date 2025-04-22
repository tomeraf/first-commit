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
        throw new UnsupportedOperationException("Owner cannot add permissions");
    }

    @Override
    public void removePermission(Permission permission) {
        throw new UnsupportedOperationException("Owner cannot remove permissions");
    }

    @Override
    public void addAppointment(int nomineeID) {
        appointments.add(nomineeID);
    }

    @Override
    public void removeAppointment(int appointeeID) {
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
    public int getShopID() {
        return shopID;
    }

}

