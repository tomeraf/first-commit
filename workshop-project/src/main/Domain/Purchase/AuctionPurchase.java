package Domain.Purchase;

import java.time.LocalDateTime;

public class AuctionPurchase extends Purchase {
    private double highestBid=0;
    private LocalDateTime auctionStartTime; 
    private LocalDateTime auctionEndTime; 
    private boolean isAccepted = false;

    
    public AuctionPurchase(int id,double startingBid, int itemId, LocalDateTime auctionStartTime, LocalDateTime auctionEndTime) {
        super(id,startingBid, itemId, 0); // Assuming the buyerId is not set at this point
        validateAuctionTimes(auctionStartTime, auctionEndTime);
        this.auctionStartTime = auctionStartTime;
        this.auctionEndTime = auctionEndTime;
    }
    private void validateAuctionTimes(LocalDateTime auctionStartTime, LocalDateTime auctionEndTime) {
        if (auctionStartTime.isAfter(auctionEndTime)) {
            throw new IllegalArgumentException("Auction start time must be before end time.");
        }
        if (auctionStartTime.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Auction start time must be in the future.");
        }
        if (auctionEndTime.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Auction end time must be in the future.");
        }  
    }

    public void placeOffer(double bidAmount, int buyerId) {
        if (bidAmount > highestBid && bidAmount >= getAmount() && isAuctionActive()) {
            this.highestBid = bidAmount;
            setBuyerId(buyerId);
        } else {
            throw new IllegalArgumentException("Bid amount must be higher than the current bid and starting bid.");
        }
    }
    
    public double getHighestBid() {
        return highestBid;
    }
    public LocalDateTime getAuctionStartTime() {
        return auctionStartTime;
    }
    public LocalDateTime getAuctionEndTime() {
        return auctionEndTime;
    }
    public boolean isAuctionActive() {
        return auctionStartTime.isBefore(LocalDateTime.now()) && auctionEndTime.isAfter(LocalDateTime.now());
    }
    public boolean isAuctionEnded() {
        return auctionEndTime.isBefore(LocalDateTime.now());
    }
    public boolean isAuctionStarted() {
        return auctionStartTime.isBefore(LocalDateTime.now());
    }
    
    public boolean isAccepted() {
        return isAccepted;
    }



}
