package Domain.Purchase;

public abstract class Purchase {
    private double amount;
    private int itemId;
    private int buyerId;

    public Purchase(double amount, int itemId, int buyerId) {
        this.amount = amount;
        this.itemId = itemId;
        this.buyerId = buyerId;
    }
    public double getAmount() {
        return amount;
    }
    public int getItemId() {
        return itemId;
    }
    public int getBuyerId() {
        return buyerId;
    }
    public void setBuyerId(int buyerId) {
        this.buyerId = buyerId;
    }


    public abstract boolean isAccepted();
    public abstract void accept();

}
