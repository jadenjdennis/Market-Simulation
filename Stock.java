//Called by MarketRunner to make an ArrayList of stocks for MarketGrid
public class Stock {
    private String abbrevName;
    private String fullName;
    private String description;
    private double stockValue;
    private int sharesAvailable;
    
    public Stock(String abbrevName, String fullName, String description, double stockValue, int sharesAvailable) {
        this.abbrevName = abbrevName;
        this.fullName = fullName;
        this.description = description;
        this.stockValue = stockValue;
        this.sharesAvailable = sharesAvailable; 
    }
    
    public String getAbbrevName() {
        return abbrevName;
    }
    
    
    public String getFullName() {
        return fullName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public double getStockValue() {
        return stockValue;
    }
    
    public int getShareCount() {
        return sharesAvailable;
    }
    
    public void removeShares(int shareCount) {
        sharesAvailable = sharesAvailable - shareCount;
    }
    
    public void addShares(int shareCount) {
        sharesAvailable = sharesAvailable + shareCount;
    }
    public void setStockValue(double stockVal) {
        stockValue = stockVal;
    }
}
