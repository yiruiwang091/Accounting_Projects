package model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ListOfExpensesTest {
    private ListOfExpenses expenses;
    private Expense expenseOne;
    private Expense expenseTwo;
    private Expense expenseThree;

    @BeforeEach
    void runBefore() {
        expenses = new ListOfExpenses();
        expenseOne = new Expense("H-Mart", 2.1, "CAD", 50, "bmo", "Food", 50);
        expenseTwo = new Expense("Tuition", 3.3, "CNY", 5000, "td", "Tuition", 1000);
        expenseThree = new Expense("Residence Fee", 1.1, "CAD", 1000, "bmo", "Housing", 1000);
    }

    @Test
    void testAddExpense() {
        assertEquals(0, expenses.length());
        expenses.addExpense(expenseOne);
        assertEquals(1, expenses.length());
        expenses.addExpense(expenseTwo);
        expenses.addExpense(expenseTwo);
        expenses.addExpense(expenseThree);
        assertEquals(4, expenses.length());
    }

    @Test
    void testLength() {
        assertEquals(0, expenses.length());
        expenses.addExpense(expenseOne);
        expenses.addExpense(expenseTwo);
        assertEquals(2, expenses.length());
    }

    @Test
    void testRemove() {
        assertFalse(expenses.removeExpense(0));
    }

    @Test
    void testRemoveMulti() {
        expenses.addExpense(expenseOne);
        expenses.addExpense(expenseTwo);
        expenses.addExpense(expenseThree);

        assertTrue(expenses.removeExpense(2));
        assertEquals(2, expenses.length());

        expenses.removeExpense(0);
        assertTrue(expenses.removeExpense(0));
        assertEquals(0, expenses.length());

        assertFalse(expenses.removeExpense(0));
    }

    @Test
    void testGetExpense() {
        expenses.addExpense(expenseOne);
        expenses.addExpense(expenseTwo);
        expenses.addExpense(expenseThree);

        assertEquals(expenseOne, expenses.get(0));
        assertEquals(expenseThree, expenses.get(2));
    }

    @Test
    void testBudgetAndAnalytics() {
        expenses.addExpense(expenseOne);
        expenses.addExpense(expenseTwo);
        expenses.addExpense(expenseThree);

        expenses.setBudget("Food", 40);
        expenses.setBudget("Tuition", 1200);
        expenses.setBudget("Housing", 1200);

        assertEquals(2050, expenses.getTotalSpentInCad());
        assertEquals(50, expenses.getTotalSpentByCategoryInCad("Food"));
        assertEquals(1000, expenses.getTotalSpentByCategoryInCad("Tuition"));
        assertEquals(1000, expenses.getTotalSpentByCategoryInCad("Housing"));

        assertTrue(expenses.isOverBudget("Food"));
        assertFalse(expenses.isOverBudget("Tuition"));
        assertFalse(expenses.isOverBudget("Housing"));

        assertEquals(-10, expenses.getRemainingBudget("Food"));
        assertEquals(200, expenses.getRemainingBudget("Tuition"));
        assertEquals(200, expenses.getRemainingBudget("Housing"));

        Map<String, Double> totals = expenses.getCategoryTotalsInCad();
        assertEquals(3, totals.size());
        assertEquals(50, totals.get("Food"));
        assertEquals(1000, totals.get("Tuition"));
        assertEquals(1000, totals.get("Housing"));
    }
}