package Domain;

import java.util.HashSet;
import java.util.List;
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
    abstract void removeAllAppointments();

    abstract Map<Integer, IRole> getAppointments(); // Returns a list of all the appointments the role has made  
    abstract int getAppointer();
    abstract int getShopID();
}
