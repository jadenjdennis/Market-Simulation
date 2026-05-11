import java.util.*;
/*
Make an array of length 100, and add the object associated with each event
the # of times in correlation with the % of that object

Like if the double val is 0.03 then you'll have 3 instances of that 
object inside the list of 100 items
*/

public class Randomizer {
    /*
    a random stock is selected from StockData.csv, and then once the
    object is added to the cell, it's removed from a copy of the 
    list so that the object doesn't get used more than once.
    */
    public static void stockRandomizer(Stock[][] grid, ArrayList<Stock> Stocks) {
        for(int row = 0; row < grid.length; row++) {
            for(int col = 0; col < grid[row].length; col++) {
                /*
                Picks a random index, and sets the current grid val to the stock at that index
                of the arraylist and then deletes the stock at that index from the ArrayList so 
                there's no repeats.
                */
                int randomIndex = (int) (Math.random() * Stocks.size());
                Stock stockChosen = Stocks.get(randomIndex);
                grid[row][col] = stockChosen;
                Stocks.remove(randomIndex);
            }
        }
    }
    
    public static double startPrice() {
        double minPrice = 1.00;
        double maxPrice = 150.00;
        
        return (Math.random() * (maxPrice - minPrice) + minPrice);
    }
    
    public static double bullMarketBump(Stock chosenStock) {
        double minNoise = 0.05;
        double maxNoise = 0.15;
        
        return (Math.random() * (maxNoise - minNoise) + minNoise);
    }
    
    public static double bearMarketBump(Stock chosenStock) {
        double minNoise = -0.05;
        double maxNoise = -0.15;
        
        return (Math.random() * (maxNoise - minNoise) + minNoise);
    }
    
    public static double getMarketNoise() {
        double minNoise = -0.10;
        double maxNoise = 0.15;
        
        return (Math.random() * (maxNoise - minNoise) + minNoise);
    }
    
    public static int numberOfShares() {
        int minShares = 25;
        int maxShares = 200;
        
        return ((int) (Math.random() * ((maxShares - minShares) + 1) + minShares));
    }
    
    //Return type prob wrong but we'll fix that later trust
    public static Event chooseEvent(ArrayList<Event> events){
        ArrayList<Event> triggered = new ArrayList<>();
        
        for(Event e : events) {
            if(Math.random() < e.getProbability()) {
                triggered.add(e);
            }
        }
        
        if(triggered.isEmpty()) {
            return null;
        }
        return triggered.get((int) (Math.random() * triggered.size()));
    }
}
