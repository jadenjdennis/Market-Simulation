//Makes the events based on parameters passed from MarketRunner.java
//This class is COMPLETE
public class Event {
    private String eventName;
    private int year;
    private double probability;
    private int stockEffect;
    private int priceEffect;
    private String targetType;
    
    public Event(String eventName, int year, double probability, int stockEffect, int priceEffect, String targetType) {
        this.eventName = eventName;
        this.year = year;
        this.probability = probability;
        this.stockEffect = stockEffect;
        this.priceEffect = priceEffect;
        this.targetType = targetType;
    }
    
    public String getEventName() {
        return eventName;
    }
    
    public int getYear() {
        return year;
    }
    
    public double getProbability() {
        return probability;
    }
    
    public int getStockEffect() {
        return stockEffect;
    }
    
    public int getPriceEffect() {
        return priceEffect;
    }
    
    public String getTargetType() {
        return targetType;
    }
}

