package Domain.Purchase;

public abstract class Purchase {
    private int id;
    private double amount;
    private int itemId;
    private String buyerUsername;

    public Purchase(int id,double amount, int itemId, String buyerUsername) {
        this.id = id;
        this.amount = amount;
        this.itemId = itemId;
        this.buyerUsername = buyerUsername;
    }
    public int getId() {
        return id;
    }
    public double getAmount() {
        return amount;
    }
    public int getItemId() {
        return itemId;
    }
    public String getBuyerUsername() {
        return buyerUsername;
    }
    public void setBuyerUserName(String buyerUsername) {
        this.buyerUsername = buyerUsername;
    }


    public abstract boolean isAccepted();
    public abstract void accept();

}
