package ui;

import model.Budget;
import model.Expense;
import model.ListOfExpenses;
import persistence.JsonReader;
import persistence.JsonWriter;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

public class AccountingApp {
    private static final String JSON_STORE = "./data/expenses.json";
    private static final Map<String, Double> CAD_PER_UNIT = createExchangeRates();
    private static final Map<String, String> CURRENCY_SYMBOLS = createCurrencySymbols();

    private ListOfExpenses expenses;
    private Scanner input;
    private JsonWriter jsonWriter;
    private JsonReader jsonReader;

    public AccountingApp() throws FileNotFoundException {
        expenses = new ListOfExpenses();
        jsonWriter = new JsonWriter(JSON_STORE);
        jsonReader = new JsonReader(JSON_STORE);
        runAccounting();
    }

    private static Map<String, Double> createExchangeRates() {
        Map<String, Double> rates = new LinkedHashMap<>();
        rates.put("CAD", 1.0);
        rates.put("CNY", 0.20);
        rates.put("EUR", 1.45);
        rates.put("HKD", 0.17);
        rates.put("INR", 0.017);
        rates.put("JPY", 0.01);
        rates.put("KRW", 0.001);
        rates.put("USD", 1.36);
        return rates;
    }

    private static Map<String, String> createCurrencySymbols() {
        Map<String, String> symbols = new LinkedHashMap<>();
        symbols.put("CAD", "$");
        symbols.put("CNY", "¥");
        symbols.put("EUR", "€");
        symbols.put("HKD", "$");
        symbols.put("INR", "₹");
        symbols.put("JPY", "¥");
        symbols.put("KRW", "₩");
        symbols.put("USD", "$");
        return symbols;
    }

    private void runAccounting() {
        boolean keepGoing = true;
        init();

        while (keepGoing) {
            displayMenu();
            String command = input.next().toLowerCase();

            if (command.equals("q")) {
                keepGoing = false;
            } else {
                processCommand(command);
            }
        }

        System.out.println("\nThanks for choosing this accounting software!");
    }

    private void displayMenu() {
        System.out.println("\nSelect from:");
        System.out.println("\tk -> keep a new expense");
        System.out.println("\tm -> modify an expense");
        System.out.println("\td -> delete an expense");
        System.out.println("\tb -> set category budget");
        System.out.println("\ta -> show analytics");
        System.out.println("\ts -> save expenses to file");
        System.out.println("\tp -> print expenses");
        System.out.println("\tl -> load expenses from file");
        System.out.println("\tq -> quit");
    }

    private void processCommand(String command) {
        if (command.equals("k")) {
            doKeep();
        } else if (command.equals("m")) {
            doModify();
        } else if (command.equals("d")) {
            doDelete();
        } else if (command.equals("b")) {
            doSetBudget();
        } else if (command.equals("a")) {
            showAnalytics();
        } else if (command.equals("s")) {
            saveExpenses();
        } else if (command.equals("p")) {
            printExpenses();
        } else if (command.equals("l")) {
            loadExpenses();
        } else {
            System.out.println("Selection not valid...");
        }
    }

    private void saveExpenses() {
        try {
            jsonWriter.open();
            jsonWriter.write(expenses);
            jsonWriter.close();
            System.out.println("Saved expenses to " + JSON_STORE);
        } catch (FileNotFoundException e) {
            System.out.println("Unable to write to file: " + JSON_STORE);
        }
    }

    private void printExpenses() {
        if (expenses.length() == 0) {
            System.out.println("No expenses recorded yet.");
            return;
        }

        for (Expense e : expenses.getExpenses()) {
            System.out.println("Description: " + e.getDescription()
                    + " | Time: " + e.getTime()
                    + " | Category: " + e.getCategory()
                    + " | Currency: " + e.getCurrency()
                    + " | Amount: " + getCurrencySymbol(e.getCurrency()) + formatMoney(e.getAmount())
                    + " | CAD Base: $" + formatMoney(e.getAmountInCad())
                    + " | Account: " + e.getAccount());
        }
    }

    private void loadExpenses() {
        try {
            expenses = jsonReader.read();
            System.out.println("Loaded expenses from " + JSON_STORE);
        } catch (IOException e) {
            System.out.println("Unable to read from file: " + JSON_STORE);
        }
    }

    private void init() {
        expenses = new ListOfExpenses();
        input = new Scanner(System.in);
        input.useDelimiter("\n");
    }

    private void doKeep() {
        Expense expense = new Expense("Description", 1.1, "CAD", 0, "bmo");

        System.out.print("\nEnter the description: ");
        String description = input.next();
        expense.updateDescription(description);

        System.out.print("Enter the date: ");
        double date = input.nextDouble();
        expense.updateDate(date);

        System.out.print("Choose the currency: ");
        String currencyNew = selectCurrency();
        expense.updateCurrency(currencyNew);

        System.out.print("Select a category: ");
        String category = selectCategory();
        expense.updateCategory(category);

        System.out.print("Enter amount in CAD: $");
        convertCadAmountToSelectedCurrency(expense);

        System.out.print("Select an account: ");
        String account = selectAccount();
        expense.updateAccount(account);

        expenses.addExpense(expense);

        System.out.println("Added expense: " + description
                + " | Time: " + date
                + " | Category: " + expense.getCategory()
                + " | Amount: " + expense.getCurrency() + " "
                + getCurrencySymbol(expense.getCurrency()) + formatMoney(expense.getAmount())
                + " | CAD Base: $" + formatMoney(expense.getAmountInCad())
                + " | Account: " + expense.getAccount());
    }

    private void doModify() {
        System.out.print("What expense would you like to modify? Type the description: ");
        String description = input.next();
        Expense thisExpense = findExpenseByDescription(description);

        if (thisExpense == null) {
            System.out.println("Expense not found.");
            return;
        }

        System.out.println("\tChoose the category you'd like to change:");
        System.out.println("\tdes -> description");
        System.out.println("\tdate -> date");
        System.out.println("\tc -> currency");
        System.out.println("\tam -> amount");
        System.out.println("\tacc -> account");
        System.out.println("\tcat -> category");

        String selection = "";
        while (!(selection.equals("des")
                || selection.equals("date")
                || selection.equals("c")
                || selection.equals("am")
                || selection.equals("acc")
                || selection.equals("cat"))) {
            selection = input.next().toLowerCase();
        }

        if (selection.equals("des")) {
            newDes(thisExpense);
        } else if (selection.equals("date")) {
            newDate(thisExpense);
        } else if (selection.equals("c")) {
            newCurrency(thisExpense);
        } else if (selection.equals("am")) {
            newAmount(thisExpense);
        } else if (selection.equals("acc")) {
            newAccount(thisExpense);
        } else {
            newCategory(thisExpense);
        }
    }

    private Expense findExpenseByDescription(String description) {
        for (int i = 0; i < expenses.length(); i++) {
            if (description.equalsIgnoreCase(expenses.get(i).getDescription())) {
                return expenses.get(i);
            }
        }
        return null;
    }

    private void newDes(Expense thisExpense) {
        System.out.println("Enter the new description:");
        String str = input.next();
        thisExpense.updateDescription(str);
        System.out.println("New description: " + thisExpense.getDescription() + " has been updated.");
    }

    private void newDate(Expense thisExpense) {
        System.out.println("Enter the new date:");
        double time = input.nextDouble();
        thisExpense.updateDate(time);
        System.out.println("New date: " + thisExpense.getTime() + " has been updated.");
    }

    private void newCurrency(Expense thisExpense) {
        System.out.println("Choose the new currency:");
        String str = selectCurrency();
        thisExpense.updateCurrency(str);
        System.out.println("Please enter the amount again in CAD:");
        convertCadAmountToSelectedCurrency(thisExpense);
        System.out.println("The new amount is "
                + thisExpense.getCurrency()
                + " "
                + getCurrencySymbol(thisExpense.getCurrency())
                + formatMoney(thisExpense.getAmount()));
    }

    private void newAmount(Expense thisExpense) {
        System.out.println("Enter the new amount in CAD:");
        convertCadAmountToSelectedCurrency(thisExpense);
        System.out.println("New amount: "
                + thisExpense.getCurrency()
                + " "
                + getCurrencySymbol(thisExpense.getCurrency())
                + formatMoney(thisExpense.getAmount())
                + " (CAD base: $" + formatMoney(thisExpense.getAmountInCad()) + ")");
    }

    private void newAccount(Expense thisExpense) {
        System.out.println("Select the new account for the expense:");
        String acc = selectAccount();
        thisExpense.updateAccount(acc);
        System.out.println("New account: " + thisExpense.getAccount() + " has been updated.");
    }

    private void newCategory(Expense thisExpense) {
        System.out.println("Select the new category:");
        String category = selectCategory();
        thisExpense.updateCategory(category);
        System.out.println("New category: " + thisExpense.getCategory() + " has been updated.");
    }

    private void doDelete() {
        if (expenses.length() == 0) {
            System.out.println("No expenses to remove.");
            return;
        }

        for (int i = 0; i < expenses.length(); i++) {
            Expense expense = expenses.get(i);
            System.out.println(i + " | " + expense.getDescription()
                    + " | " + expense.getCategory()
                    + " | " + expense.getCurrency() + " "
                    + getCurrencySymbol(expense.getCurrency()) + formatMoney(expense.getAmount())
                    + " | CAD Base $" + formatMoney(expense.getAmountInCad())
                    + " | " + expense.getAccount());
        }

        System.out.println("Type the number of the expense that you'd like to remove:");
        int number = input.nextInt();

        if (expenses.removeExpense(number)) {
            System.out.println("Expense removed.");
        } else {
            System.out.println("Invalid expense index.");
        }
    }

    private void doSetBudget() {
        System.out.print("Select a category for the budget: ");
        String category = selectCategory();

        System.out.print("Enter monthly budget in CAD for " + category + ": $");
        double limit = input.nextDouble();

        expenses.setBudget(category, limit);
        System.out.println("Budget for " + category + " is now CAD $" + formatMoney(limit));
    }

    private void showAnalytics() {
        if (expenses.length() == 0) {
            System.out.println("No expenses recorded yet.");
            return;
        }

        System.out.println("\nTotal spending in CAD: $" + formatMoney(expenses.getTotalSpentInCad()));
        System.out.println("Category totals:");

        for (Map.Entry<String, Double> entry : expenses.getCategoryTotalsInCad().entrySet()) {
            System.out.println("- " + entry.getKey() + ": CAD $" + formatMoney(entry.getValue()));
        }

        if (expenses.getBudgets().isEmpty()) {
            System.out.println("No budgets set yet.");
            return;
        }

        System.out.println("\nBudget status:");
        for (Budget budget : expenses.getBudgets()) {
            String category = budget.getCategory();
            double limit = budget.getMonthlyLimit();
            double spent = expenses.getTotalSpentByCategoryInCad(category);
            double remaining = expenses.getRemainingBudget(category);

            if (expenses.isOverBudget(category)) {
                System.out.println("- " + category
                        + ": spent CAD $" + formatMoney(spent)
                        + " / budget CAD $" + formatMoney(limit)
                        + " -> OVER by CAD $" + formatMoney(Math.abs(remaining)));
            } else {
                System.out.println("- " + category
                        + ": spent CAD $" + formatMoney(spent)
                        + " / budget CAD $" + formatMoney(limit)
                        + " -> remaining CAD $" + formatMoney(remaining));
            }
        }
    }

    private String selectCurrency() {
        String selection = "";

        while (!CAD_PER_UNIT.containsKey(selection)) {
            printCurrencyOptions();
            selection = input.next().toUpperCase();
        }

        return selection;
    }

    private void printCurrencyOptions() {
        System.out.println("CAD for Canadian Dollar");
        System.out.println("CNY for Chinese Yuan");
        System.out.println("EUR for European Euro");
        System.out.println("HKD for HongKong Dollar");
        System.out.println("INR for Indian Rupee");
        System.out.println("JPY for Japanese Yen");
        System.out.println("KRW for Korean Won");
        System.out.println("USD for US Dollar");
    }

    private String selectAccount() {
        String selection = "";

        while (!(selection.equals("bmo") || selection.equals("td") || selection.equals("cash"))) {
            System.out.println("bmo");
            System.out.println("td");
            System.out.println("cash");
            selection = input.next().toLowerCase();
        }

        return selection;
    }

    private String selectCategory() {
        String selection = "";

        while (!(selection.equals("food")
                || selection.equals("transport")
                || selection.equals("shopping")
                || selection.equals("housing")
                || selection.equals("tuition")
                || selection.equals("entertainment")
                || selection.equals("health")
                || selection.equals("general"))) {
            System.out.println("Food");
            System.out.println("Transport");
            System.out.println("Shopping");
            System.out.println("Housing");
            System.out.println("Tuition");
            System.out.println("Entertainment");
            System.out.println("Health");
            System.out.println("General");
            selection = input.next().toLowerCase();
        }

        if (selection.equals("food")) {
            return "Food";
        } else if (selection.equals("transport")) {
            return "Transport";
        } else if (selection.equals("shopping")) {
            return "Shopping";
        } else if (selection.equals("housing")) {
            return "Housing";
        } else if (selection.equals("tuition")) {
            return "Tuition";
        } else if (selection.equals("entertainment")) {
            return "Entertainment";
        } else if (selection.equals("health")) {
            return "Health";
        } else {
            return "General";
        }
    }

    private void convertCadAmountToSelectedCurrency(Expense expense) {
        double amountInCad = input.nextDouble();
        double convertedMoney = roundToTwoDecimals(convertFromCad(amountInCad, expense.getCurrency()));

        expense.updateAmounts(convertedMoney, roundToTwoDecimals(amountInCad));

        System.out.println(expense.getCurrency()
                + "\t"
                + getCurrencySymbol(expense.getCurrency())
                + formatMoney(convertedMoney));
    }

    private double convertFromCad(double amountInCad, String currency) {
        double cadPerUnit = CAD_PER_UNIT.getOrDefault(currency, 1.0);
        return amountInCad / cadPerUnit;
    }

    private String getCurrencySymbol(String currency) {
        return CURRENCY_SYMBOLS.getOrDefault(currency, "$");
    }

    private String formatMoney(double amount) {
        return String.format("%.2f", amount);
    }

    private double roundToTwoDecimals(double amount) {
        return Math.round(amount * 100.0) / 100.0;
    }
}