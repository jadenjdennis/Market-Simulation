//imports all "extensions" that this program needs to run
//To Do: Check for errors...

//housekeeping
import java.io.*;
import java.util.*;

//These imports are for the market to update every minute
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MarketRunner
{
    public static int score = 0;
    public static boolean keepPlaying = true;
    public static ArrayList<Event> Events = new ArrayList<>();
    public static Stock[][] simulationGrid;
    
    //We need this ArrayList so MarketGrid can pick a random 100 and assign an 
    //object to each cell of the grid
    public static ArrayList<Stock> Stocks = new ArrayList<>();
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        
    public static void main(String[] args)
    {
        Player player = new Player();
        Scanner input = new Scanner(System.in);
        
        //Jaden: You need to work on the method to load events
        loadEventsFromCSV("EventData.csv");
        //Look into this...
        loadStocksFromCSV("StockData.csv");
        simulationGrid = new Stock[10][10];
        
        
        
        //Randomizer.stockRandomizer(simulationGrid, Stocks);
        
        MarketGrid gameGrid = new MarketGrid(simulationGrid);
        gameGrid.loadGrid(Stocks);
        startMarketTimer();
        
        while(keepPlaying) {
            //All the actual gameplay like do you want to purchase and stuff
            //go here...
            
            //introduction to game
            System.out.println("######### Hello! Welcome to Abuse-Jaden's-Wallet-Simulator #########");
            System.out.println("--------------------------------------------------------------------");
            
            System.out.println("Current Balance: $" + player.getAllowance());
            System.out.print("Type 'view' to see the market, 'portfolio' to view portfolio, 'buy' to purchase, 'sell' to sell, or 'quit' to exit: ");
            
            String choice = input.nextLine();
            System.out.println();
            
            if(choice.equals("view")) {
                System.out.println(gameGrid);
            }
            
            else if(choice.equals("portfolio")) {
                ArrayList<PlayerStock> portfolio = player.viewPortfolio();
                if (portfolio.isEmpty()) {
                    System.out.println("Portfolio is empty.");
                } 
                
                else {
                    for (PlayerStock ps : portfolio) {
                        System.out.println(ps);
                    }
                }
            }
            
            else if (choice.equals("buy")) {
                System.out.print("Enter stock symbol: ");
                String symbol = input.nextLine().trim().toUpperCase();
                System.out.print("How many shares? ");
                int amount = input.nextInt();
                input.nextLine();

                player.buyStock(symbol, amount, gameGrid);
                System.out.println("Current balance: $" + player.getAllowance());
                System.out.println("Total Net Worth: $" + player.getNetWorth());
                System.out.println("Market Total: $" + player.getMarketTotal());
            }

            // Same idea for sell when you add it:
            else if (choice.equals("sell")) {
                System.out.print("Enter stock symbol: ");
                String symbol = input.nextLine().trim().toUpperCase();
                System.out.print("How many shares? ");
                int amount = input.nextInt();
                input.nextLine();

                player.sellStock(symbol, amount, gameGrid);
                System.out.println("Current balance: $" + player.getAllowance());
                System.out.println("Total Net Worth: $" + player.getNetWorth());
                System.out.println("Market Total: $" + player.getMarketTotal());
            }
            
            else if(choice.equals("quit")) {
                keepPlaying = false;
                System.out.println("Simulation over. Final balance: $" + player.getAllowance());
                System.out.println("Total Net Worth: $" + player.getNetWorth());
                System.out.println("Market Total: $" + player.getMarketTotal());
            }
        }
    }
    
    //This method is AI Generated to get the grid price updates every minute
    public static void startMarketTimer() {
        scheduler.scheduleAtFixedRate(() -> {
            
            MarketLogic.updateFullMarket(simulationGrid);
            //Add the event part happening here as well
            Event triggered = Randomizer.chooseEvent(Events);
            if (triggered != null) {
                EventManager.applyEvent(triggered, simulationGrid);
                System.out.println("[EVENT]" + triggered.getEventName() + " has occured! (" + triggered.getTargetType() + ", price " + triggered.getPriceEffect() + "%, shares " + triggered.getStockEffect() + ")");
        }            
            
            System.out.println("[MARKET] The minute bell has run. Prices updated");
        }, 1, 1, TimeUnit.MINUTES);
    }
    
    //Called earlier in order to create the Event object that gets added to the 
    //list of objects
    public static void loadEventsFromCSV(String fileName) {
        try (Scanner scanner = new Scanner(new File(fileName))) {
            //Auto skips the first line since that's the formatting instructions
            if (scanner.hasNextLine()) {
                scanner.nextLine();
            }
            
            while (scanner.hasNextLine()) {
                //Event name
                String line = scanner.nextLine().trim();
                if (line.isEmpty()) continue;

                String[] parts = line.split(",");
                String eventName  = parts[0].trim();
                int    year       = parts[1].trim().equals("null") ? -1 : Integer.parseInt(parts[1].trim());
                double probability = Double.parseDouble(parts[2].trim());
                int    stockEffect = Integer.parseInt(parts[3].trim());
                int    priceEffect = Integer.parseInt(parts[4].trim());
                String targetType  = parts[5].trim();
                
                Event newEvent = new Event(eventName, year, probability, stockEffect, priceEffect, targetType);
                
                //Adds the new event to the ArrayList 
                Events.add(newEvent);
            }
        }
        
        catch (FileNotFoundException e) {
           System.out.println("File not found: " + fileName + " Error");
        }
    }
    
    //Called in the MarketGrid.java file in order to initialize the market grid
    public static void loadStocksFromCSV(String fileName) {
        try(Scanner scanner = new Scanner(new File(fileName))) {
            if (scanner.hasNextLine()) {
                scanner.nextLine();
            }
            
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.isEmpty()) continue;

                String[] parts   = line.split(",", 3);
                String abbrevName = parts[0].trim();
                String stockName  = parts[1].trim();
                String description = parts[2].trim();
                
                //Provides a random start price within (0,150) <-- bounds
                double startPrice = Randomizer.startPrice();
                int sharesAvailable = Randomizer.numberOfShares();
                Stock newStock = new Stock(abbrevName, stockName, description, startPrice, sharesAvailable);
                
                Stocks.add(newStock);
            }
        }
        
        catch (FileNotFoundException e) {
            System.out.println("File not found: " + fileName + " Error");
        }
    }
}
