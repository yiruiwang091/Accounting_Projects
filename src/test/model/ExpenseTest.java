package model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExpenseTest {
    private Expense testExpense;

    @BeforeEach
    void runBefore() {
        testExpense = new Expense("H-Mart", 2.20, "CAD", 20, "bmo", "Food", 20);
    }

    @Test
    void testLegacyConstructorDefaults() {
        Expense legacyExpense = new Expense("Lunch", 1.10, "CAD", 15, "cash");

        assertEquals("Lunch", legacyExpense.getDescription());
        assertEquals(1.10, legacyExpense.getTime());
        assertEquals("CAD", legacyExpense.getCurrency());
        assertEquals(15, legacyExpense.getAmount());
        assertEquals("cash", legacyExpense.getAccount());
        assertEquals("General", legacyExpense.getCategory());
        assertEquals(15, legacyExpense.getAmountInCad());
    }

    @Test
    void testConstructor() {
        assertEquals("H-Mart", testExpense.getDescription());
        assertEquals(2.20, testExpense.getTime());
        assertEquals("CAD", testExpense.getCurrency());
        assertEquals(20, testExpense.getAmount());
        assertEquals("bmo", testExpense.getAccount());
        assertEquals("Food", testExpense.getCategory());
        assertEquals(20, testExpense.getAmountInCad());
    }

    @Test
    void testUpdateDescription() {
        testExpense.updateDescription("Shopping");
        assertEquals("Shopping", testExpense.getDescription());
    }

    @Test
    void testUpdateDate() {
        testExpense.updateDate(3.5);
        assertEquals(3.5, testExpense.getTime());
    }

    @Test
    void testUpdateCurrency() {
        testExpense.updateCurrency("USD");
        assertEquals("USD", testExpense.getCurrency());
    }

    @Test
    void testUpdateMoney() {
        testExpense.updateMoney(10);
        assertEquals(10, testExpense.getAmount());
    }

    @Test
    void testUpdateAmountInCad() {
        testExpense.updateAmountInCad(14);
        assertEquals(14, testExpense.getAmountInCad());
    }

    @Test
    void testUpdateAmounts() {
        testExpense.updateAmounts(100, 20);
        assertEquals(100, testExpense.getAmount());
        assertEquals(20, testExpense.getAmountInCad());
    }

    @Test
    void testUpdateAccount() {
        testExpense.updateAccount("td");
        assertEquals("td", testExpense.getAccount());
    }

    @Test
    void testUpdateCategory() {
        testExpense.updateCategory("Shopping");
        assertEquals("Shopping", testExpense.getCategory());
    }
}