package Domain.Purchase;

import java.util.ArrayList;
import java.util.List;

public class BidPurchase extends Purchase {
    private int BidID;
    private int submitterId;
    private List<Integer> AcceptingMembers;
    private int rejecterID=-1;
    private int isAccepted = 0; // 0 = not accepted, 1 = accepted, -1 = rejected
    private int CounterBidID=-1;

    public BidPurchase(int id,double bidAmount, int itemId, int buyerId,int submitterId) {
        super(bidAmount, itemId, buyerId);
        this.BidID = id;
        this.submitterId = submitterId;
        this.AcceptingMembers = new ArrayList<>();
        this.AcceptingMembers.add(submitterId);
    }

    public int getSubmitterId() {
        return submitterId;
    }
    public List<Integer> getAcceptingMembers() {
        return AcceptingMembers;
    }
    public int getRejecterID() {
        return rejecterID;
    }
    public boolean isAccepted() {
        return isAccepted==1;
    }
    public void accept(){}
    /* getMembersWithPermission() is a placeholder for the actual implementation of getting members with permission.
    public void accept(){
        if(AcceptingMembers.equals(getMembersWithPermission().add(buyerId)))
            isAccepted = 1;
    }
    */
    public void rejected(int rejecterID){
        this.rejecterID = rejecterID;
        isAccepted = -1;
    }
    public void addAcceptingMember(int memberId) {
        if (!AcceptingMembers.contains(memberId)) {
            AcceptingMembers.add(memberId);
        }
    }
    public void submitCounterBid(int counterID) {
        this.CounterBidID = counterID;
    }
    /* getMembersWithPermission() is a placeholder for the actual implementation of getting members with permission.
    public String showStatus(){
        List<Integer> allMembers = getMembersWithPermission();
        allMembers.add(buyerId);
        StringBuilder status = new StringBuilder("Bid Purchase Status:\n");
        status.append("Bid Amount: ").append(bidAmount).append("\n");
        status.append("Item ID: ").append(itemId).append("\n");
        status.append("Buyer ID: ").append(buyerId).append("\n");
        status.append("Submitter ID: ").append(submitterId).append("\n");
        for (int memberId : allMembers) {
            if(memberId == rejecterID) {
                status.append("Rejecting Member ID: ").append(memberId).append("\n");
            } else if (AcceptingMembers.contains(memberId)) {
                status.append("Accepting Member ID: ").append(memberId).append("\n");
            }
            else {
                status.append("Need to Decide Member ID: ").append(memberId).append("\n");
            }
        }
    }
    */


}
