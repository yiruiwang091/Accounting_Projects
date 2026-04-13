package ui;

import model.Event;
import model.EventLog;
import model.Expense;
import model.ListOfExpenses;
import persistence.JsonReader;
import persistence.JsonWriter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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

    private static final Color BG = new Color(245, 247, 251);
    private static final Color CARD = new Color(255, 255, 255);
    private static final Color PRIMARY = new Color(76, 110, 245);
    private static final Color PRIMARY_HOVER = new Color(59, 91, 219);
    private static final Color SUCCESS = new Color(18, 184, 134);
    private static final Color DANGER = new Color(250, 82, 82);
    private static final Color TEXT = new Color(33, 37, 41);
    private static final Color MUTED = new Color(108, 117, 125);
    private static final Color BORDER = new Color(226, 232, 240);
    private static final Color TABLE_ALT = new Color(248, 250, 252);
    private static final Color TABLE_SELECTION = new Color(220, 234, 255);

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
        frame.setSize(1380, 820);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        JPanel root = new JPanel(new BorderLayout(20, 20));
        root.setBorder(new EmptyBorder(20, 20, 20, 20));
        root.setBackground(BG);
        frame.setContentPane(root);

        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildCenter(), BorderLayout.CENTER);
    }

    private JPanel buildHeader() {
        RoundedPanel header = new RoundedPanel(28, new Color(255, 255, 255));
        header.setLayout(new BorderLayout());
        header.setBorder(new EmptyBorder(20, 28, 20, 28));

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

        FadeInLabel title = new FadeInLabel("Accounting App");
        title.setFont(new Font("SansSerif", Font.BOLD, 30));
        title.setForeground(TEXT);

        FadeInLabel subtitle = new FadeInLabel("Clean multi-currency expense tracking with better visuals");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subtitle.setForeground(MUTED);

        left.add(title);
        left.add(Box.createVerticalStrut(6));
        left.add(subtitle);

        JLabel badge = new JLabel("Modern UI");
        badge.setOpaque(true);
        badge.setBackground(new Color(237, 242, 255));
        badge.setForeground(PRIMARY);
        badge.setBorder(new EmptyBorder(10, 16, 10, 16));
        badge.setFont(new Font("SansSerif", Font.BOLD, 13));

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 8));
        right.setOpaque(false);
        right.add(badge);

        header.add(left, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);

        title.startFadeIn(12);
        subtitle.startFadeIn(30);

        return header;
    }

    private JPanel buildCenter() {
        JPanel center = new JPanel(new BorderLayout(20, 20));
        center.setOpaque(false);

        center.add(buildTableCard(), BorderLayout.CENTER);
        center.add(buildBottomSection(), BorderLayout.SOUTH);

        return center;
    }

    private JPanel buildTableCard() {
        RoundedPanel card = new RoundedPanel(28, CARD);
        card.setLayout(new BorderLayout(0, 16));
        card.setBorder(new EmptyBorder(18, 18, 18, 18));
        card.setPreferredSize(new Dimension(0, 360));

        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setOpaque(false);

        JLabel title = new JLabel("Expenses");
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setForeground(TEXT);

        JLabel desc = new JLabel("View, select, and manage your records");
        desc.setFont(new Font("SansSerif", Font.PLAIN, 13));
        desc.setForeground(MUTED);

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.add(title);
        left.add(Box.createVerticalStrut(4));
        left.add(desc);

        titleBar.add(left, BorderLayout.WEST);

        initializeTable();
        JScrollPane pane = new JScrollPane(table);
        pane.setBorder(BorderFactory.createEmptyBorder());
        pane.getViewport().setBackground(Color.WHITE);

        card.add(titleBar, BorderLayout.NORTH);
        card.add(pane, BorderLayout.CENTER);

        return card;
    }

    private JPanel buildBottomSection() {
        JPanel bottom = new JPanel(new BorderLayout(20, 20));
        bottom.setOpaque(false);
        bottom.setPreferredSize(new Dimension(0, 320));

        bottom.add(buildFormCard(), BorderLayout.CENTER);
        bottom.add(buildButtonCard(), BorderLayout.EAST);

        return bottom;
    }

    private JPanel buildFormCard() {
        RoundedPanel card = new RoundedPanel(28, CARD);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(22, 22, 22, 22));

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Expense Details");
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setForeground(TEXT);

        JLabel subtitle = new JLabel("Edit fields below and keep exchange info as a saved snapshot");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subtitle.setForeground(MUTED);

        top.add(title);
        top.add(Box.createVerticalStrut(4));
        top.add(subtitle);

        JPanel formGrid = new JPanel(new GridBagLayout());
        formGrid.setOpaque(false);
        formGrid.setBorder(new EmptyBorder(18, 0, 0, 0));

        initializeFields();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 18);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        addFormRow(formGrid, gbc, 0, 0, "Description", textDescription, "CAD Base", textCadAmount);
        addFormRow(formGrid, gbc, 0, 1, "Date", textDate, "Category", textCategory);
        addFormRow(formGrid, gbc, 0, 2, "Currency", textCurrency, "Account", textAccount);
        addFormRow(formGrid, gbc, 0, 3, "Amount", textAmount, "Rate→CAD", textRate);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weightx = 0;
        formGrid.add(createFieldLabel("Rate Time"), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 3;
        gbc.weightx = 1;
        formGrid.add(textRateTimestamp, gbc);

        card.add(top, BorderLayout.NORTH);
        card.add(formGrid, BorderLayout.CENTER);

        return card;
    }

    private JPanel buildButtonCard() {
        RoundedPanel card = new RoundedPanel(28, CARD);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(22, 18, 22, 18));
        card.setPreferredSize(new Dimension(210, 0));

        JLabel title = new JLabel("Actions");
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setForeground(TEXT);

        JLabel subtitle = new JLabel("Quick controls");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subtitle.setForeground(MUTED);

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.add(title);
        top.add(Box.createVerticalStrut(4));
        top.add(subtitle);

        JPanel buttons = new JPanel();
        buttons.setOpaque(false);
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.Y_AXIS));
        buttons.setBorder(new EmptyBorder(18, 0, 0, 0));

        initializeButtons(buttons);

        card.add(top, BorderLayout.NORTH);
        card.add(buttons, BorderLayout.CENTER);

        return card;
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
                        "Rate→CAD",
                        "Rate Time"
                },
                0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(model) {
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);

                if (isRowSelected(row)) {
                    c.setBackground(TABLE_SELECTION);
                    c.setForeground(TEXT);
                } else {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : TABLE_ALT);
                    c.setForeground(TEXT);
                }
                return c;
            }
        };

        table.setRowHeight(34);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setFont(new Font("SansSerif", Font.PLAIN, 14));
        table.setSelectionBackground(TABLE_SELECTION);
        table.setSelectionForeground(TEXT);
        table.setFillsViewportHeight(true);

        JTableHeader header = table.getTableHeader();
        header.setReorderingAllowed(false);
        header.setFont(new Font("SansSerif", Font.BOLD, 14));
        header.setBackground(new Color(241, 245, 249));
        header.setForeground(TEXT);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));
        header.setPreferredSize(new Dimension(header.getWidth(), 38));

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setBorder(new EmptyBorder(0, 12, 0, 12));
        renderer.setHorizontalAlignment(SwingConstants.LEFT);
        table.setDefaultRenderer(Object.class, renderer);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                populateFieldsFromSelectedRow();
            }
        });
    }

    private void initializeFields() {
        textDescription = createTextField();
        textDate = createTextField();
        textCurrency = createTextField();
        textAmount = createTextField();
        textCadAmount = createTextField();
        textCategory = createTextField();
        textAccount = createTextField();
        textRate = createTextField();
        textRateTimestamp = createTextField();

        textCadAmount.setEditable(false);
        textRate.setEditable(false);
        textRateTimestamp.setEditable(false);

        addAutoConversionListeners();
    }

    private JTextField createTextField() {
        JTextField field = new JTextField();
        field.setPreferredSize(new Dimension(220, 42));
        field.setFont(new Font("SansSerif", Font.PLAIN, 15));
        field.setForeground(TEXT);
        field.setBackground(new Color(250, 251, 253));
        field.setCaretColor(PRIMARY);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                new EmptyBorder(10, 12, 10, 12)
        ));

        field.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (field.isEditable()) {
                    field.setBackground(Color.WHITE);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                field.setBackground(field.isEditable() ? new Color(250, 251, 253) : new Color(245, 247, 250));
            }
        });

        return field;
    }

    private JLabel createFieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 14));
        label.setForeground(TEXT);
        return label;
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, int x, int y,
                            String label1, JTextField field1,
                            String label2, JTextField field2) {
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.weightx = 0;
        panel.add(createFieldLabel(label1), gbc);

        gbc.gridx = x + 1;
        gbc.weightx = 1;
        panel.add(field1, gbc);

        gbc.gridx = x + 2;
        gbc.weightx = 0;
        panel.add(createFieldLabel(label2), gbc);

        gbc.gridx = x + 3;
        gbc.weightx = 1;
        panel.add(field2, gbc);
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

    private void initializeButtons(JPanel buttonPanel) {
        AnimatedButton btnAdd = new AnimatedButton("Add", PRIMARY);
        AnimatedButton btnUpdate = new AnimatedButton("Update", SUCCESS);
        AnimatedButton btnRefreshRate = new AnimatedButton("Refresh Rate", new Color(15, 98, 254));
        AnimatedButton btnDelete = new AnimatedButton("Delete", DANGER);
        AnimatedButton btnClear = new AnimatedButton("Clear", new Color(134, 142, 150));

        buttonPanel.add(btnAdd);
        buttonPanel.add(Box.createVerticalStrut(14));
        buttonPanel.add(btnUpdate);
        buttonPanel.add(Box.createVerticalStrut(14));
        buttonPanel.add(btnRefreshRate);
        buttonPanel.add(Box.createVerticalStrut(14));
        buttonPanel.add(btnDelete);
        buttonPanel.add(Box.createVerticalStrut(14));
        buttonPanel.add(btnClear);

        btnAdd.addActionListener((ActionEvent e) -> {
            if (!validateInputs()) {
                return;
            }

            Expense newExpense = buildExpenseFromFields();
            expenses.addExpense(newExpense);
            model.addRow(toRow(newExpense));
            clearFields();
            showInfo("Expense added.");
        });

        btnUpdate.addActionListener((ActionEvent e) -> {
            int selectedRow = table.getSelectedRow();

            if (selectedRow < 0) {
                showError("Please select a row to update.");
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
                rate = getRateToCad(normalizedCurrency);
                timestamp = getCurrentTimestamp();
            } else if (!textRate.getText().trim().isEmpty() && !textRateTimestamp.getText().trim().isEmpty()) {
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
            showInfo("Expense updated.");
        });

        btnRefreshRate.addActionListener((ActionEvent e) -> refreshRateFields());

        btnDelete.addActionListener((ActionEvent e) -> {
            int selectedRow = table.getSelectedRow();

            if (selectedRow < 0) {
                showError("Please select a row to delete.");
                return;
            }

            expenses.removeExpense(selectedRow);
            model.removeRow(selectedRow);
            clearFields();
            showInfo("Expense deleted.");
        });

        btnClear.addActionListener((ActionEvent e) -> clearFields());
    }

    private void initializeMenu() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));

        fileMenu = new JMenu("File");
        fileMenu.setFont(new Font("SansSerif", Font.BOLD, 14));
        menuBar.add(fileMenu);
        frame.setJMenuBar(menuBar);

        JMenuItem saveMenu = new JMenuItem("Save");
        saveMenu.addActionListener((ActionEvent e) -> {
            try {
                jsonWriter.open();
                jsonWriter.write(expenses);
                jsonWriter.close();
                showInfo("Saved to " + JSON_STORE);
            } catch (FileNotFoundException exception) {
                showError("Unable to write to file: " + JSON_STORE);
            }
        });
        fileMenu.add(saveMenu);

        JMenuItem loadMenu = new JMenuItem("Load");
        loadMenu.addActionListener((ActionEvent e) -> {
            try {
                expenses = jsonReader.read();
                refreshTable();
                clearFields();
                showInfo("Loaded from " + JSON_STORE);
            } catch (IOException exception) {
                showError("Unable to read from file: " + JSON_STORE);
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

        if (!textRate.getText().trim().isEmpty() && !textRateTimestamp.getText().trim().isEmpty()) {
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
            showError("Please fill in all fields except CAD Base / Rate / Rate Time.");
            return false;
        }

        try {
            Double.parseDouble(textDate.getText().trim());
            Double.parseDouble(textAmount.getText().trim());
        } catch (NumberFormatException e) {
            showError("Date and Amount must be numbers.");
            return false;
        }

        String currency = normalizeCurrency(textCurrency.getText());
        if (!CAD_PER_UNIT.containsKey(currency)) {
            showError("Unsupported currency. Use one of: CAD, USD, CNY, RMB, EUR, JPY, KRW, HKD, INR.");
            return false;
        }

        return true;
    }

    private boolean validateRateRefreshInputs() {
        if (textCurrency.getText().trim().isEmpty() || textAmount.getText().trim().isEmpty()) {
            showError("Please enter Currency and Amount first.");
            return false;
        }

        try {
            Double.parseDouble(textAmount.getText().trim());
        } catch (NumberFormatException e) {
            showError("Amount must be a valid number.");
            return false;
        }

        String currency = normalizeCurrency(textCurrency.getText());
        if (!CAD_PER_UNIT.containsKey(currency)) {
            showError("Unsupported currency. Use one of: CAD, USD, CNY, RMB, EUR, JPY, KRW, HKD, INR.");
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

        showInfo("Rate snapshot refreshed.");
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

    private void showInfo(String message) {
        JOptionPane.showMessageDialog(frame, message, "Message", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(frame, message, "Message", JOptionPane.ERROR_MESSAGE);
    }

    static class RoundedPanel extends JPanel {
        private final int radius;
        private final Color backgroundColor;

        RoundedPanel(int radius, Color backgroundColor) {
            this.radius = radius;
            this.backgroundColor = backgroundColor;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(new Color(0, 0, 0, 18));
            g2.fillRoundRect(4, 6, getWidth() - 8, getHeight() - 8, radius, radius);

            g2.setColor(backgroundColor);
            g2.fillRoundRect(0, 0, getWidth() - 8, getHeight() - 8, radius, radius);

            g2.dispose();
            super.paintComponent(g);
        }
    }

    static class AnimatedButton extends JButton {
        private final Color baseColor;
        private Color currentColor;

        AnimatedButton(String text, Color baseColor) {
            super(text);
            this.baseColor = baseColor;
            this.currentColor = baseColor;

            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setForeground(Color.WHITE);
            setFont(new Font("SansSerif", Font.BOLD, 15));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setMaximumSize(new Dimension(170, 44));
            setPreferredSize(new Dimension(170, 44));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    currentColor = brighten(baseColor, 18);
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    currentColor = baseColor;
                    repaint();
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    currentColor = baseColor.darker();
                    repaint();
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    currentColor = brighten(baseColor, 18);
                    repaint();
                }
            });
        }

        private Color brighten(Color c, int amount) {
            return new Color(
                    Math.min(255, c.getRed() + amount),
                    Math.min(255, c.getGreen() + amount),
                    Math.min(255, c.getBlue() + amount)
            );
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(new Color(0, 0, 0, 18));
            g2.fillRoundRect(3, 5, getWidth() - 6, getHeight() - 6, 18, 18);

            g2.setColor(currentColor);
            g2.fillRoundRect(0, 0, getWidth() - 6, getHeight() - 6, 18, 18);

            FontMetrics fm = g2.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(getText())) / 2 - 3;
            int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent() - 2;

            g2.setColor(Color.WHITE);
            g2.setFont(getFont());
            g2.drawString(getText(), x, y);

            g2.dispose();
        }
    }

    static class FadeInLabel extends JLabel {
        private float alpha = 0f;

        FadeInLabel(String text) {
            super(text);
            setOpaque(false);
        }

        void startFadeIn(int delayMs) {
            Timer timer = new Timer(delayMs, null);
            timer.addActionListener(e -> {
                alpha += 0.06f;
                if (alpha >= 1f) {
                    alpha = 1f;
                    timer.stop();
                }
                repaint();
            });
            timer.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            super.paintComponent(g2);
            g2.dispose();
        }
    }
}