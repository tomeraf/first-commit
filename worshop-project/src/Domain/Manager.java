package Domain;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Manager extends IRole {
    private Set<Permission> permission; //hashSet-to prevents duplication

    public Manager(int appointerID, int shopID, Set<Permission> permission) {
        this.appointerID = appointerID;
        this.shopID = shopID;
        this.permission = permission;
        this.appointments = new HashMap<>();
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
    public void addAppointment(int nomineeID, IRole role) {
        System.out.println("Manager has no appointments");
    }

    @Override
    public void removeAppointment(int appointeeID) {
        System.out.println("Manager has no appointments");
    }

    @Override
    public Map<Integer, IRole> getAppointments() {
        // no implementation needed for manager
        return null;
    }

    @Override
    public void removeAllAppointments() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'removeAllAppointments'");
    }

    public Set<Permission> getPermissions() {
        return permission;
    }
    
}
