package Domain.Discount;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class OpenDiscount{
    private int id;
    private int percentage;
    private Date startDate;
    private Date endDate;
    private List<Integer> itemIds;

    public OpenDiscount(int id, int percentage, String startDate, String endDate) {
        validDetails(id, percentage, startDate, endDate);
        this.id = id;
        this.percentage = percentage;
        this.startDate = Date.valueOf(startDate);
        this.endDate = Date.valueOf(endDate);
        this.itemIds = new ArrayList<>();
    }
    private void validDetails(int id, int percentage, String startDate, String endDate) {
        if (id <= 0) throw new IllegalArgumentException("ID must be positive.");
        if (percentage < 0 || percentage > 100) throw new IllegalArgumentException("Percentage must be between 0 and 100.");
        if(!(validDates(startDate,endDate))) {  
            throw new IllegalArgumentException("Invalid date range.");
        }   
    }

    public boolean validDates(String sDate,String eDate) {
        try {
            Date s=Date.valueOf(sDate);
            Date e=Date.valueOf(eDate);
            if(s.after(e)) return false; // Start date is after end date
            if(e.before(new Date(System.currentTimeMillis()))) return false; // End date is before current date
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
        
    }
    public void addItemId(int itemId) {
        this.itemIds.add(itemId);
    }
    public void removeItemId(int itemId) {
        this.itemIds.remove(Integer.valueOf(itemId));
    }
    public boolean inTime(Date currentDate) {
        return (currentDate.after(startDate) || currentDate.equals(startDate)) && (currentDate.before(endDate) || currentDate.equals(endDate));
    }
    public boolean isApplicable(int itemId) {
        return itemIds.contains(itemId);
    }
    public void extendDiscount(String newEndDate) {
        this.endDate = Date.valueOf(newEndDate);
    }
    public int applyDiscount(int itemId,int price) {
        if(inTime(new Date(System.currentTimeMillis())) && isApplicable(itemId))  // Assuming itemIds has at least one item for simplicity
            return price - (price * percentage / 100);
         else 
            return price; // No discount applied
    }

    public int getId() {
        return id;
    }
    public int getPercentage() {
        return percentage;
    }
    public Date getStartDate() {
        return startDate;
    }
    public Date getEndDate() {
        return endDate;
    }
    public List<Integer> getItemIds() {
        return itemIds;
    }
    public void setPercentage(int percentage) {
        this.percentage = percentage;
    }

}
