package Domain.Purchase;

import java.time.LocalDateTime;

public class AuctionPurchase extends Purchase {
    private double highestBid=0;
    private LocalDateTime auctionStartTime; 
    private LocalDateTime auctionEndTime; 
    private boolean isAccepted = false;

    
    public AuctionPurchase(double startingBid, int itemId, LocalDateTime auctionStartTime, LocalDateTime auctionEndTime) {
        super(startingBid, itemId, 0); // Assuming the buyerId is not set at this point
        this.auctionStartTime = auctionStartTime;
        this.auctionEndTime = auctionEndTime;
    }
    public void placeOffer(double bidAmount, int buyerId) {
        if (bidAmount > highestBid && bidAmount >= getAmount() && auctionEndTime.isAfter(LocalDateTime.now())&& auctionStartTime.isBefore(LocalDateTime.now())) {
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
    public void accept() {
        if (isAuctionEnded()) {
            isAccepted = true;
        } else {
            throw new IllegalStateException("Auction must be ended to accept the bid.");
        }
    }


}
