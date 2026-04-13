package persistence;

import model.Expense;
import model.ListOfExpenses;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static java.lang.Double.valueOf;

// Represents a reader that reads list of expenses from JSON data stored in file.
public class JsonReader {
    private String source;

    public JsonReader(String source) {
        this.source = source;
    }

    public ListOfExpenses read() throws IOException {
        String jsonData = readFile(source);
        JSONObject jsonObject = new JSONObject(jsonData);
        return parseExpenses(jsonObject);
    }

    private ListOfExpenses parseExpenses(JSONObject jsonObject) {
        ListOfExpenses expenses = new ListOfExpenses();
        addExpenses(expenses, jsonObject);
        addBudgets(expenses, jsonObject);
        return expenses;
    }

    private void addExpenses(ListOfExpenses expenses, JSONObject jsonObject) {
        JSONArray jsonArray = jsonObject.getJSONArray("listofexpenses");
        for (Object json : jsonArray) {
            JSONObject nextExpense = (JSONObject) json;
            addExpense(expenses, nextExpense);
        }
    }

    private void addExpense(ListOfExpenses expenses, JSONObject jsonObject) {
        String des = jsonObject.getString("description");
        double time = valueOf(jsonObject.getDouble("time"));
        String currency = jsonObject.getString("currency");
        double amount = valueOf(jsonObject.getDouble("amount"));
        String account = jsonObject.getString("account");

        String category = jsonObject.has("category") ? jsonObject.getString("category") : "General";

        double exchangeRateToCad;
        if (jsonObject.has("exchangeRateToCad")) {
            exchangeRateToCad = valueOf(jsonObject.getDouble("exchangeRateToCad"));
        } else if (jsonObject.has("amountInCad") && amount != 0) {
            exchangeRateToCad = valueOf(jsonObject.getDouble("amountInCad")) / amount;
        } else {
            exchangeRateToCad = 1.0;
        }

        double amountInCad = jsonObject.has("amountInCad")
                ? valueOf(jsonObject.getDouble("amountInCad")) : amount * exchangeRateToCad;

        String rateTimestamp = jsonObject.has("rateTimestamp")
                ? jsonObject.getString("rateTimestamp") : "N/A";

        Expense e = new Expense(
                des, time, currency, amount, account,
                category, exchangeRateToCad, amountInCad, rateTimestamp
        );
        expenses.addExpense(e);
    }

    private void addBudgets(ListOfExpenses expenses, JSONObject jsonObject) {
        if (!jsonObject.has("budgets")) {
            return;
        }

        JSONArray jsonArray = jsonObject.getJSONArray("budgets");
        for (Object json : jsonArray) {
            JSONObject nextBudget = (JSONObject) json;
            String category = nextBudget.getString("category");
            double monthlyLimit = valueOf(nextBudget.getDouble("monthlyLimit"));
            expenses.setBudget(category, monthlyLimit);
        }
    }

    private String readFile(String source) throws IOException {
        StringBuilder contentBuilder = new StringBuilder();

        try (Stream<String> stream = Files.lines(Paths.get(source), StandardCharsets.UTF_8)) {
            stream.forEach(contentBuilder::append);
        }

        return contentBuilder.toString();
    }
}