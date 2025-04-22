package Domain;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
public interface IRole {

    boolean hasPermission(Permission permission);
    void addPermission(Permission permission);
    void removePermission(Permission permission);

    public void addAppointment(int nomineeID);
    public void removeAppointment(int appointeeID);

    List<Integer> getAppointments(); // Returns a list of all the appointments the role has made  
    int getAppointer();
    int getShopID();
}
