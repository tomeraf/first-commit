package Domain;

import java.util.HashSet;
import java.util.List;

public class Manager implements IRole {
    private int appointerID;
    private int shopID;
    private HashSet<Permission> permission; //hashSet-to prevents duplication

    public Manager(int appointerID, int shopID, HashSet<Permission> permission) {
        this.appointerID = appointerID;
        this.shopID = shopID;
        this.permission = permission;
    }

    @Override
    public boolean hasPermission(Permission permission) {
        return this.permission.contains(permission);
    }

    @Override
    public void addPermission(Permission permission) {
        this.permission.add(permission);
    }

    @Override
    public void removePermission(Permission permission) {
        if (this.permission.contains(permission)) {
            this.permission.remove(permission);
        }
    }

    @Override
    public int getShopID() {
        return shopID;
    }

    @Override
    public int getAppointer() {
        return appointerID;
    }

    @Override
    public void addAppointment(int nomineeID) {
        System.out.println("Manager has no appointments");
    }

    @Override
    public void removeAppointment(int appointeeID) {
        System.out.println("Manager has no appointments");
    }

    @Override
    public List<Integer> getAppointments() {
        // no implementation needed for manager
        return null;
    }

}
