package ui;

import model.Event;
import model.EventLog;
import model.Expense;
import model.ListOfExpenses;
import persistence.JsonReader;
import persistence.JsonWriter;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

public class ExpenseGUI {
    private static final String JSON_STORE = "./data/expenses.json";

    private JFrame frame;
    private JTable table;
    private DefaultTableModel model;

    private JTextField textDescription;
    private JTextField textDate;
    private JTextField textCurrency;
    private JTextField textAmount;
    private JTextField textCadAmount;
    private JTextField textCategory;
    private JTextField textAccount;
    private JTextField textRate;
    private JTextField textRateTimestamp;

    private ListOfExpenses expenses;
    private JsonWriter jsonWriter;
    private JsonReader jsonReader;

    private JMenu fileMenu;

    private static final Map<String, Double> CAD_PER_UNIT = createExchangeRates();

    public ExpenseGUI() throws IOException {
        expenses = new ListOfExpenses();
        jsonWriter = new JsonWriter(JSON_STORE);
        jsonReader = new JsonReader(JSON_STORE);

        initializeFrame();
        initializeTable();
        initializeFields();
        initializeButtons();
        initializeMenu();
        initializeWindowClosing();

        frame.setVisible(true);
    }

    private static Map<String, Double> createExchangeRates() {
        Map<String, Double> rates = new LinkedHashMap<>();
        rates.put("CAD", 1.0);
        rates.put("USD", 1.36);
        rates.put("CNY", 0.20);
        rates.put("RMB", 0.20);
        rates.put("EUR", 1.45);
        rates.put("JPY", 0.01);
        rates.put("KRW", 0.001);
        rates.put("HKD", 0.17);
        rates.put("INR", 0.017);
        return rates;
    }

    private void initializeFrame() {
        frame = new JFrame("Accounting App");
        frame.setLayout(null);
        frame.setSize(1280, 620);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    }

    private void initializeTable() {
        model = new DefaultTableModel(
                new Object[]{
                        "Description",
                        "Date",
                        "Currency",
                        "Amount",
                        "CAD Base",
                        "Category",
                        "Account",
                        "Rate->CAD",
                        "Rate Time"
                },
                0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(model);
        table.setBackground(Color.LIGHT_GRAY);
        table.setForeground(Color.BLACK);
        table.setFont(new Font("", Font.BOLD, 18));
        table.setRowHeight(28);

        JScrollPane pane = new JScrollPane(table);
        pane.setBounds(0, 0, 1260, 260);
        frame.add(pane);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                populateFieldsFromSelectedRow();
            }
        });
    }

    private void initializeFields() {
        textDescription = new JTextField();
        textDate = new JTextField();
        textCurrency = new JTextField();
        textAmount = new JTextField();
        textCadAmount = new JTextField();
        textCategory = new JTextField();
        textAccount = new JTextField();
        textRate = new JTextField();
        textRateTimestamp = new JTextField();

        textCadAmount.setEditable(false);
        textRate.setEditable(false);
        textRateTimestamp.setEditable(false);

        addField("Description", textDescription, 20, 290);
        addField("Date", textDate, 20, 335);
        addField("Currency", textCurrency, 20, 380);
        addField("Amount", textAmount, 20, 425);

        addField("CAD Base", textCadAmount, 300, 290);
        addField("Category", textCategory, 300, 335);
        addField("Account", textAccount, 300, 380);
        addField("Rate->CAD", textRate, 300, 425);

        addField("Rate Time", textRateTimestamp, 620, 290);

        addAutoConversionListeners();
    }

    private void addField(String labelText, JTextField textField, int x, int y) {
        JLabel label = new JLabel(labelText);
        label.setBounds(x, y, 120, 25);
        frame.add(label);

        textField.setBounds(x + 120, y, 140, 25);
        frame.add(textField);
    }

    private void addAutoConversionListeners() {
        DocumentListener listener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateExchangePreview();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateExchangePreview();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateExchangePreview();
            }
        };

        textCurrency.getDocument().addDocumentListener(listener);
        textAmount.getDocument().addDocumentListener(listener);
    }

    private void initializeButtons() {
        JButton btnAdd = new JButton("Add");
        btnAdd.setBounds(980, 290, 140, 30);
        frame.add(btnAdd);

        btnAdd.addActionListener((ActionEvent e) -> {
            if (!validateInputs()) {
                return;
            }

            Expense newExpense = buildExpenseFromFields();
            expenses.addExpense(newExpense);
            model.addRow(toRow(newExpense));
            clearFields();
        });

        JButton btnUpdate = new JButton("Update");
        btnUpdate.setBounds(980, 335, 140, 30);
        frame.add(btnUpdate);

        btnUpdate.addActionListener((ActionEvent e) -> {
            int selectedRow = table.getSelectedRow();

            if (selectedRow < 0) {
                JOptionPane.showMessageDialog(frame, "Please select a row to update.");
                return;
            }

            if (!validateInputs()) {
                return;
            }

            Expense selectedExpense = expenses.get(selectedRow);

            String description = textDescription.getText().trim();
            double date = Double.parseDouble(textDate.getText().trim());
            String normalizedCurrency = normalizeCurrency(textCurrency.getText());
            double amount = Double.parseDouble(textAmount.getText().trim());
            String category = textCategory.getText().trim();
            String account = textAccount.getText().trim();

            String oldCurrency = selectedExpense.getCurrency();
            double rate = selectedExpense.getExchangeRateToCad();
            String timestamp = selectedExpense.getRateTimestamp();

            boolean currencyChanged = !normalizedCurrency.equalsIgnoreCase(oldCurrency);

            if (currencyChanged) {
                if (hasDisplayedRateInfo()) {
                    rate = Double.parseDouble(textRate.getText().trim());
                    timestamp = textRateTimestamp.getText().trim();
                } else {
                    rate = getRateToCad(normalizedCurrency);
                    timestamp = getCurrentTimestamp();
                }
            } else if (displayedRateInfoDiffersFrom(selectedExpense)) {
                rate = Double.parseDouble(textRate.getText().trim());
                timestamp = textRateTimestamp.getText().trim();
            }

            double cadAmount = roundToTwoDecimals(amount * rate);

            selectedExpense.updateDescription(description);
            selectedExpense.updateDate(date);
            selectedExpense.updateCurrency(normalizedCurrency);
            selectedExpense.updateMoney(amount);
            selectedExpense.updateCategory(category);
            selectedExpense.updateAccount(account);
            selectedExpense.updateExchangeInfo(rate, cadAmount, timestamp);

            refreshTable();
            table.setRowSelectionInterval(selectedRow, selectedRow);
            JOptionPane.showMessageDialog(frame, "Expense updated successfully.");
        });

        JButton btnRefreshRate = new JButton("Refresh Rate");
        btnRefreshRate.setBounds(980, 380, 140, 30);
        frame.add(btnRefreshRate);

        btnRefreshRate.addActionListener((ActionEvent e) -> refreshRateFields());

        JButton btnDelete = new JButton("Delete");
        btnDelete.setBounds(980, 425, 140, 30);
        frame.add(btnDelete);

        btnDelete.addActionListener((ActionEvent e) -> {
            int selectedRow = table.getSelectedRow();

            if (selectedRow < 0) {
                JOptionPane.showMessageDialog(frame, "Please select a row to delete.");
                return;
            }

            expenses.removeExpense(selectedRow);
            model.removeRow(selectedRow);
            clearFields();
        });

        JButton btnClear = new JButton("Clear");
        btnClear.setBounds(980, 470, 140, 30);
        frame.add(btnClear);

        btnClear.addActionListener((ActionEvent e) -> clearFields());
    }

    private void initializeMenu() {
        JMenuBar menuBar = new JMenuBar();
        fileMenu = new JMenu("File");
        menuBar.add(fileMenu);
        frame.setJMenuBar(menuBar);

        JMenuItem saveMenu = new JMenuItem("Save");
        saveMenu.addActionListener((ActionEvent e) -> {
            try {
                jsonWriter.open();
                jsonWriter.write(expenses);
                jsonWriter.close();
                JOptionPane.showMessageDialog(frame, "Saved to " + JSON_STORE);
            } catch (FileNotFoundException exception) {
                JOptionPane.showMessageDialog(frame, "Unable to write to file: " + JSON_STORE);
            }
        });
        fileMenu.add(saveMenu);

        JMenuItem loadMenu = new JMenuItem("Load");
        loadMenu.addActionListener((ActionEvent e) -> {
            try {
                expenses = jsonReader.read();
                refreshTable();
                clearFields();
                JOptionPane.showMessageDialog(frame, "Loaded from " + JSON_STORE);
            } catch (IOException exception) {
                JOptionPane.showMessageDialog(frame, "Unable to read from file: " + JSON_STORE);
            }
        });
        fileMenu.add(loadMenu);
    }

    private void initializeWindowClosing() {
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                for (Event event : EventLog.getInstance()) {
                    System.out.println(event);
                }
                frame.dispose();
                System.exit(0);
            }
        });
    }

    private Expense buildExpenseFromFields() {
        String description = textDescription.getText().trim();
        double date = Double.parseDouble(textDate.getText().trim());
        String currency = normalizeCurrency(textCurrency.getText());
        double amount = Double.parseDouble(textAmount.getText().trim());
        String account = textAccount.getText().trim();
        String category = textCategory.getText().trim();

        double rate;
        String timestamp;

        if (hasDisplayedRateInfo()) {
            rate = Double.parseDouble(textRate.getText().trim());
            timestamp = textRateTimestamp.getText().trim();
        } else {
            rate = getRateToCad(currency);
            timestamp = getCurrentTimestamp();
        }

        double amountInCad = roundToTwoDecimals(amount * rate);

        return new Expense(
                description, date, currency, amount, account,
                category, rate, amountInCad, timestamp
        );
    }

    private Object[] toRow(Expense expense) {
        return new Object[]{
                expense.getDescription(),
                expense.getTime(),
                expense.getCurrency(),
                expense.getAmount(),
                expense.getAmountInCad(),
                expense.getCategory(),
                expense.getAccount(),
                expense.getExchangeRateToCad(),
                expense.getRateTimestamp()
        };
    }

    private void refreshTable() {
        model.setRowCount(0);
        for (Expense expense : expenses.getExpenses()) {
            model.addRow(toRow(expense));
        }
    }

    private void populateFieldsFromSelectedRow() {
        int i = table.getSelectedRow();
        if (i < 0) {
            return;
        }

        textDescription.setText(model.getValueAt(i, 0).toString());
        textDate.setText(model.getValueAt(i, 1).toString());
        textCurrency.setText(model.getValueAt(i, 2).toString());
        textAmount.setText(model.getValueAt(i, 3).toString());
        textCadAmount.setText(model.getValueAt(i, 4).toString());
        textCategory.setText(model.getValueAt(i, 5).toString());
        textAccount.setText(model.getValueAt(i, 6).toString());
        textRate.setText(model.getValueAt(i, 7).toString());
        textRateTimestamp.setText(model.getValueAt(i, 8).toString());
    }

    private boolean validateInputs() {
        if (textDescription.getText().trim().isEmpty()
                || textDate.getText().trim().isEmpty()
                || textCurrency.getText().trim().isEmpty()
                || textAmount.getText().trim().isEmpty()
                || textCategory.getText().trim().isEmpty()
                || textAccount.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Please fill in all fields except CAD Base / Rate / Rate Time.");
            return false;
        }

        try {
            Double.parseDouble(textDate.getText().trim());
            Double.parseDouble(textAmount.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(frame, "Date and Amount must be numbers.");
            return false;
        }

        String currency = normalizeCurrency(textCurrency.getText());
        if (!CAD_PER_UNIT.containsKey(currency)) {
            JOptionPane.showMessageDialog(
                    frame,
                    "Unsupported currency. Use one of: CAD, USD, CNY, RMB, EUR, JPY, KRW, HKD, INR."
            );
            return false;
        }

        return true;
    }

    private boolean validateRateRefreshInputs() {
        if (textCurrency.getText().trim().isEmpty() || textAmount.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Please enter Currency and Amount first.");
            return false;
        }

        try {
            Double.parseDouble(textAmount.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(frame, "Amount must be a valid number.");
            return false;
        }

        String currency = normalizeCurrency(textCurrency.getText());
        if (!CAD_PER_UNIT.containsKey(currency)) {
            JOptionPane.showMessageDialog(
                    frame,
                    "Unsupported currency. Use one of: CAD, USD, CNY, RMB, EUR, JPY, KRW, HKD, INR."
            );
            return false;
        }

        return true;
    }

    private void refreshRateFields() {
        if (!validateRateRefreshInputs()) {
            return;
        }

        String currency = normalizeCurrency(textCurrency.getText());
        double amount = Double.parseDouble(textAmount.getText().trim());
        double rate = getRateToCad(currency);
        double cadAmount = roundToTwoDecimals(amount * rate);
        String timestamp = getCurrentTimestamp();

        textRate.setText(String.format("%.4f", rate));
        textCadAmount.setText(String.format("%.2f", cadAmount));
        textRateTimestamp.setText(timestamp);
    }

    private void updateExchangePreview() {
        String currencyText = textCurrency.getText().trim();
        String amountText = textAmount.getText().trim();

        if (currencyText.isEmpty() || amountText.isEmpty()) {
            textCadAmount.setText("");
            textRate.setText("");
            return;
        }

        try {
            double amount = Double.parseDouble(amountText);
            String currency = normalizeCurrency(currencyText);
            double rate = getRateToCad(currency);
            double cadAmount = roundToTwoDecimals(amount * rate);

            textRate.setText(String.format("%.4f", rate));
            textCadAmount.setText(String.format("%.2f", cadAmount));
        } catch (Exception e) {
            textCadAmount.setText("");
            textRate.setText("");
        }
    }

    private boolean hasDisplayedRateInfo() {
        return !textRate.getText().trim().isEmpty() && !textRateTimestamp.getText().trim().isEmpty();
    }

    private boolean displayedRateInfoDiffersFrom(Expense expense) {
        if (!hasDisplayedRateInfo()) {
            return false;
        }

        try {
            double shownRate = Double.parseDouble(textRate.getText().trim());
            String shownTimestamp = textRateTimestamp.getText().trim();

            double savedRate = expense.getExchangeRateToCad();
            String savedTimestamp = expense.getRateTimestamp();

            return Math.abs(shownRate - savedRate) > 0.0000001 || !shownTimestamp.equals(savedTimestamp);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private String getCurrentTimestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    private String normalizeCurrency(String currency) {
        return currency.trim().toUpperCase();
    }

    private double getRateToCad(String currency) {
        Double rate = CAD_PER_UNIT.get(currency);
        if (rate == null) {
            throw new IllegalArgumentException("Unsupported currency: " + currency);
        }
        return rate;
    }

    private double convertToCad(double amount, String currency) {
        return roundToTwoDecimals(amount * getRateToCad(currency));
    }

    private double roundToTwoDecimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private void clearFields() {
        textDescription.setText("");
        textDate.setText("");
        textCurrency.setText("");
        textAmount.setText("");
        textCadAmount.setText("");
        textCategory.setText("");
        textAccount.setText("");
        textRate.setText("");
        textRateTimestamp.setText("");
        table.clearSelection();
    }
}