import java.util.*;

public class EventManager {
    public static void applyEvent(Event event, Stock[][] grid) {
        String type = event.getTargetType();

        if (type.equals("GLOBAL")) {
            applyToAll(event, grid);
        } else if (type.equals("CELL")) {
            int randRow = (int)(Math.random() * grid.length);
            int randCol = (int)(Math.random() * grid[0].length);
            applyToStock(event, grid[randRow][randCol]);
        } else if (type.equals("ROW")) {
            int row = (int)(Math.random() * grid.length);
            for (Stock s : grid[row]) applyToStock(event, s);
        } else if (type.equals("COLUMN")) {
            int col = (int)(Math.random() * grid[0].length);
            for (int r = 0; r < grid.length; r++) applyToStock(event, grid[r][col]);
        }
    }

    private static void applyToAll(Event event, Stock[][] grid) {
        for (Stock[] row : grid)
            for (Stock s : row)
                applyToStock(event, s);
    }

    private static void applyToStock(Event event, Stock stock) {
        if (stock == null) return;

        double multiplier = 1.0 + event.getPriceEffect() / 100.0;
        stock.setStockValue(Math.max(1.00, stock.getStockValue() * multiplier));

        int effectiveShareChange = Math.max(-stock.getShareCount(), event.getStockEffect());
        stock.addShares(effectiveShareChange);
    }
}
