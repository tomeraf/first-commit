package Domain;

import java.util.List;

public class Manager implements IRole {
    private int appointerID;
    private int shopID;
    private List<Permission> permission;

    public Manager(int appointerID, int shopID, List<Permission> permission) {
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
    public void AddAppointment(int nomineeID) {
        throw new UnsupportedOperationException("Manager has no appointments");
    }

    @Override
    public void RemoveAppointment(int appointeeID) {
        throw new UnsupportedOperationException("Manager has no appointments");
    }

    @Override
    public List<Integer> getAppointments() {
        // no implementation needed for manager
        return null;
    }

    @Override
    public List<Permission> getPermissions() {
        return permission;
    }

}
