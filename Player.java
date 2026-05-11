import java.util.ArrayList;

public class Player {
    private ArrayList<PlayerStock> portfolio;
    private double allowance;
    private double marketTotal;
    //Total amount of money on hand and in the market currently
    private double netWorth;
    
    //change smth w this add
    
    public Player() {
        portfolio = new ArrayList<>();
        allowance = 10000;
        marketTotal = 0;
        netWorth = allowance + marketTotal;
    }
    
    public ArrayList<PlayerStock> viewPortfolio() {
        return portfolio;
    }
    
    public void updateNetWorth() {
        marketTotal = 0.0;
        netWorth = allowance;
        for(int i = 0; i < portfolio.size(); i++) {
            netWorth += portfolio.get(i).getStock().getStockValue();
            marketTotal += portfolio.get(i).getStock().getStockValue();
        }
    }
    
    public double getNetWorth() {
        return netWorth;
    }
    
    public double getAllowance() {
        return allowance;
    }
    
    public double getMarketTotal() {
        return marketTotal;
    }
    
    public void buyStock(String stockSymbol, int shareCount, MarketGrid grid) {
        Stock stock = grid.getCellLocation(stockSymbol);
        if (stock == null) {
            System.out.println("Stock not found: " + stockSymbol);
            return;
        }
        double totalCost = stock.getStockValue() * shareCount;
        if (allowance >= totalCost && shareCount <= stock.getShareCount()) {
            boolean found = false;
            for (int i = 0; i < portfolio.size(); i++) {
                if (stock.getAbbrevName().equals(portfolio.get(i).getStock().getAbbrevName())) {
                    portfolio.get(i).addShares(shareCount);
                    found = true;
                    break;
                }
            }
            if (!found) {
                portfolio.add(new PlayerStock(stock, shareCount));
            }
            stock.removeShares(shareCount);
            allowance -= totalCost;
            updateNetWorth();
            MarketLogic.bullMarket(stock, shareCount);
        } 
        
        else {
            System.out.println("Insufficient funds or shares not available.");
        }
    }
    
    public void sellStock(String stockSymbol, int shareCount, MarketGrid grid) {
        Stock stock = grid.getCellLocation(stockSymbol);
        if (stock == null) {
            System.out.println("Stock not found: " + stockSymbol);
            return;
        }
        stock.addShares(shareCount);
        allowance += stock.getStockValue() * shareCount;
        for (int i = 0; i < portfolio.size(); i++) {
            if (stock.getAbbrevName().equals(portfolio.get(i).getStock().getAbbrevName())){
                portfolio.get(i).sellShares(shareCount);
            }
        }
        updateNetWorth();
        MarketLogic.bearMarket(stock, shareCount);
    }
}
