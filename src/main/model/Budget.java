package model;
import org.json.JSONObject;
import persistence.Writable;

public class Budget implements Writable {
    private String category;
    private double monthlyLimit;

    public Budget(String category, double monthlyLimit) {
        this.category = category;
        this.monthlyLimit = monthlyLimit;
    }

    public String getCategory() {
        return category;
    }

    public double getMonthlyLimit() {
        return monthlyLimit;
    }

    public void updateMonthlyLimit(double monthlyLimit) {
        this.monthlyLimit = monthlyLimit;
        EventLog.getInstance().logEvent(
                new Event("The monthly budget for " + this.category + " has been updated to: " + this.monthlyLimit)
        );
    }

    public double getRemaining(double spentInCad) {
        return monthlyLimit - spentInCad;
    }

    public boolean isOverBudget(double spentInCad) {
        return spentInCad > monthlyLimit;
    }

    @Override
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("category", category);
        json.put("monthlyLimit", monthlyLimit);
        return json;
    }
}
