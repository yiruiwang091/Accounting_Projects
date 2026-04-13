package model;

import org.json.JSONObject;
import persistence.Writable;

// Represents an expense with original amount, exchange rate, CAD-normalized amount, and metadata.
public class Expense implements Writable {
    private String description;
    private double time;
    private String currency;
    private double amount;
    private String account;
    private String category;
    private double exchangeRateToCad;
    private double amountInCad;
    private String rateTimestamp;

    // Legacy constructor
    public Expense(String description, double time, String currency, double amount, String account) {
        this(description, time, currency, amount, account, "General", 1.0, amount, "N/A");
    }

    // Intermediate constructor
    public Expense(String description, double time, String currency, double amount,
                   String account, String category, double amountInCad) {
        this(description, time, currency, amount, account, category, 1.0, amountInCad, "N/A");
    }

    // Full constructor
    public Expense(String description, double time, String currency, double amount,
                   String account, String category, double exchangeRateToCad,
                   double amountInCad, String rateTimestamp) {
        this.description = description;
        this.time = time;
        this.currency = currency;
        this.amount = amount;
        this.account = account;
        this.category = category;
        this.exchangeRateToCad = exchangeRateToCad;
        this.amountInCad = amountInCad;
        this.rateTimestamp = rateTimestamp;
    }

    public String getDescription() {
        return description;
    }

    public double getTime() {
        return time;
    }

    public String getCurrency() {
        return currency;
    }

    public double getAmount() {
        return amount;
    }

    public String getAccount() {
        return account;
    }

    public String getCategory() {
        return category;
    }

    public double getExchangeRateToCad() {
        return exchangeRateToCad;
    }

    public double getAmountInCad() {
        return amountInCad;
    }

    public String getRateTimestamp() {
        return rateTimestamp;
    }

    public void updateDescription(String description) {
        this.description = description;
        EventLog.getInstance().logEvent(
                new Event("The description has been updated to: " + this.description)
        );
    }

    public void updateDate(double time) {
        this.time = time;
        EventLog.getInstance().logEvent(
                new Event("The date has been updated to: " + this.time)
        );
    }

    public void updateCurrency(String currency) {
        this.currency = currency;
        EventLog.getInstance().logEvent(
                new Event("The currency has been updated to: " + this.currency)
        );
    }

    public void updateMoney(double amount) {
        this.amount = amount;
        EventLog.getInstance().logEvent(
                new Event("The amount has been updated to: " + this.amount)
        );
    }

    public void updateAccount(String account) {
        this.account = account;
        EventLog.getInstance().logEvent(
                new Event("The bank account has been updated to: " + this.account)
        );
    }

    public void updateCategory(String category) {
        this.category = category;
        EventLog.getInstance().logEvent(
                new Event("The category has been updated to: " + this.category)
        );
    }

    public void updateExchangeRateToCad(double exchangeRateToCad) {
        this.exchangeRateToCad = exchangeRateToCad;
        EventLog.getInstance().logEvent(
                new Event("The exchange rate has been updated to: " + this.exchangeRateToCad)
        );
    }

    public void updateAmountInCad(double amountInCad) {
        this.amountInCad = amountInCad;
        EventLog.getInstance().logEvent(
                new Event("The CAD base amount has been updated to: " + this.amountInCad)
        );
    }

    public void updateAmounts(double amount, double amountInCad) {
        this.amount = amount;
        this.amountInCad = amountInCad;
        EventLog.getInstance().logEvent(
                new Event("The amount has been updated to: " + this.amount
                        + " and the CAD base amount has been updated to: " + this.amountInCad)
        );
    }

    public void updateRateTimestamp(String rateTimestamp) {
        this.rateTimestamp = rateTimestamp;
        EventLog.getInstance().logEvent(
                new Event("The exchange-rate timestamp has been updated to: " + this.rateTimestamp)
        );
    }

    public void updateExchangeInfo(double exchangeRateToCad, double amountInCad, String rateTimestamp) {
        this.exchangeRateToCad = exchangeRateToCad;
        this.amountInCad = amountInCad;
        this.rateTimestamp = rateTimestamp;
        EventLog.getInstance().logEvent(
                new Event("The exchange info has been updated.")
        );
    }

    @Override
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("description", description);
        json.put("time", time);
        json.put("currency", currency);
        json.put("amount", amount);
        json.put("account", account);
        json.put("category", category);
        json.put("exchangeRateToCad", exchangeRateToCad);
        json.put("amountInCad", amountInCad);
        json.put("rateTimestamp", rateTimestamp);
        return json;
    }
}