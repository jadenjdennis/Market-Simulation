public class MarketLogic {
    public static void bullMarket(Stock stockBought, int shareCount) {
        double purchaseRatio = shareCount / stockBought.getShareCount();
        double newPrice = stockBought.getStockValue() * (1 + purchaseRatio + Randomizer.bullMarketBump(stockBought));
        stockBought.setStockValue(newPrice);
    }
    
    public static void bearMarket(Stock stockSold, int shareCount) {
        double purchaseRatio = shareCount / stockSold.getShareCount();
        double newPrice = stockSold.getStockValue() * (1 + purchaseRatio + Randomizer.bearMarketBump(stockSold));
        stockSold.setStockValue(Math.max(1.00, newPrice));
    }
    
    //Updates the entire grid (part of the every minute update)
    public static void updateFullMarket(Stock[][] grid) {
        for(int row = 0; row < grid.length; row++) {
            for(int col = 0; col < grid[row].length; col++) {
                Stock currentStock = grid[row][col];
                
                if(currentStock != null) {
                    double oldPrice = currentStock.getStockValue();
                    
                    double noise = Randomizer.getMarketNoise();
                    
                    double newPrice = oldPrice * (1 + noise);
                    
                    currentStock.setStockValue(Math.max(1.00, newPrice));
                }
            }
        }
    }
}
