//Takes the 10x10 grid and adds randomized stocks to the 2D Array
import java.util.*;

public class MarketGrid {
    
    private Stock[][] grid;
    
    public MarketGrid(Stock[][] grid) {
        this.grid = grid;
        
    }
    
    public void loadGrid(ArrayList<Stock> stocks) {
        //Loops through the entire grid and assigns a random stock to each spot
        Randomizer.stockRandomizer(grid, stocks);
        
    }
    
    //Loops through the loop to see if the symbol matches one in the grid
    //if yes then returns, otherwise it returns an error message
    public Stock getCellLocation(String stockName) {
        Stock returnValue = null;
        for(int row = 0; row < grid.length; row++) {
            for(int col = 0; col < grid[row].length; col++) {
                if(stockName.equals(grid[row][col].getAbbrevName())) {
                    returnValue = grid[row][col];
                }
            }
        }
        
        return returnValue;
    }
    
    public Stock[][] getGrid() {
        return grid;
    }
    
    public Stock[] getRow(int row) {
        return grid[row];
    }
    
    public Stock[] getCol(int col) {
        Stock[] column = new Stock[grid.length];
        for(int row = 0; row < grid.length; row++) {
            column[row] = grid[row][col];
        }
        return column;
    }
    
    public String toString() {
        String result = "";
        for (int r = 0; r < grid.length; r++) {
            for (int c = 0; c < grid[r].length; c++) {
                Stock s = grid[r][c];
                result += String.format("%-12s $%6.2f  ", s.getAbbrevName(), s.getStockValue());
            }
            result += "\n";
        }
        return result;
    }
}
