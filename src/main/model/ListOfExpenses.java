package model;

import org.json.JSONArray;
import org.json.JSONObject;
import persistence.Writable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Represents a list of expenses and category budgets.
public class ListOfExpenses implements Writable {
    private List<Expense> expenses;
    private List<Budget> budgets;

    public ListOfExpenses() {
        expenses = new ArrayList<>();
        budgets = new ArrayList<>();
    }

    public boolean removeExpense(int index) {
        if (index >= 0 && index < expenses.size()) {
            expenses.remove(index);
            EventLog.getInstance().logEvent(new Event("The expense has been removed."));
            return true;
        }
        return false;
    }

    public void addExpense(Expense expense) {
        expenses.add(expense);
        EventLog.getInstance().logEvent(new Event("Added one expense."));
    }

    public int length() {
        return expenses.size();
    }

    public Expense get(int i) {
        return expenses.get(i);
    }

    public List<Expense> getExpenses() {
        return Collections.unmodifiableList(expenses);
    }

    public List<Budget> getBudgets() {
        return Collections.unmodifiableList(budgets);
    }

    public void setBudget(String category, double monthlyLimit) {
        Budget existingBudget = findBudgetByCategory(category);

        if (existingBudget == null) {
            budgets.add(new Budget(category, monthlyLimit));
            EventLog.getInstance().logEvent(
                    new Event("Created a monthly budget for " + category + ": " + monthlyLimit)
            );
        } else {
            existingBudget.updateMonthlyLimit(monthlyLimit);
        }
    }

    public double getBudgetLimit(String category) {
        Budget budget = findBudgetByCategory(category);
        if (budget == null) {
            return -1;
        }
        return budget.getMonthlyLimit();
    }

    public double getTotalSpentInCad() {
        double total = 0;
        for (Expense expense : expenses) {
            total += expense.getAmountInCad();
        }
        return total;
    }

    public double getTotalSpentByCategoryInCad(String category) {
        double total = 0;
        for (Expense expense : expenses) {
            if (expense.getCategory().equalsIgnoreCase(category)) {
                total += expense.getAmountInCad();
            }
        }
        return total;
    }

    public Map<String, Double> getCategoryTotalsInCad() {
        Map<String, Double> totals = new LinkedHashMap<>();
        for (Expense expense : expenses) {
            String category = expense.getCategory();
            double currentTotal = totals.getOrDefault(category, 0.0);
            totals.put(category, currentTotal + expense.getAmountInCad());
        }
        return totals;
    }

    public boolean isOverBudget(String category) {
        double limit = getBudgetLimit(category);
        if (limit < 0) {
            return false;
        }
        return getTotalSpentByCategoryInCad(category) > limit;
    }

    public double getRemainingBudget(String category) {
        double limit = getBudgetLimit(category);
        if (limit < 0) {
            return -1;
        }
        return limit - getTotalSpentByCategoryInCad(category);
    }

    private Budget findBudgetByCategory(String category) {
        for (Budget budget : budgets) {
            if (budget.getCategory().equalsIgnoreCase(category)) {
                return budget;
            }
        }
        return null;
    }

    @Override
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("listofexpenses", expensesToJson());
        json.put("budgets", budgetsToJson());
        return json;
    }

    private JSONArray expensesToJson() {
        JSONArray jsonArray = new JSONArray();
        for (Expense e : expenses) {
            jsonArray.put(e.toJson());
        }
        return jsonArray;
    }

    private JSONArray budgetsToJson() {
        JSONArray jsonArray = new JSONArray();
        for (Budget budget : budgets) {
            jsonArray.put(budget.toJson());
        }
        return jsonArray;
    }
}