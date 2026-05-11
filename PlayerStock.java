public class PlayerStock {
    private Stock theStock;
    private int sharesOwned;
    
    public PlayerStock(Stock theStock, int sharesOwned) {
        this.theStock = theStock;
        this.sharesOwned = sharesOwned;
    }
    
    public Stock getStock() {
        return theStock;
    }
    
    public int getSharesOwned() {
        return sharesOwned;
    }
    
    public void sellShares(int sharesRemoved) {
        sharesOwned -= sharesRemoved;
    }
    
    public void addShares(int sharesAdded) {
        sharesOwned += sharesAdded;
    }
    
    public String toString() {
        return String.format("%-12s  Shares: %d  Value: $%.2f", theStock.getAbbrevName(), sharesOwned, theStock.getStockValue());
    }
}
