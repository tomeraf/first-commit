package Domain;

import java.util.List;
import java.util.Map;
public interface IRole {

    boolean hasPermission(Permission permission);
    void addPermission(Permission permission);
    void removePermission(Permission permission);

    public void AddAppointment(int nomineeID);
    public void RemoveAppointment(int appointeeID);

    List<Integer> getAppointments(); // Returns a list of all the appointments the role has made  
    int getAppointer();
    List<Permission> getPermissions();
    int getShopID();
}
