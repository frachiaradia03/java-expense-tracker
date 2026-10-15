package io.github.frachiaradia03.expensetracker;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ExpenseServiceTest {

    private ExpenseService service;

    private static final LocalDate TEST_DATE =
            LocalDate.of(2026, 10, 15);


    // ========================================
    // SETUP
    // ========================================

    @BeforeEach
    void setUp() {

        service = new ExpenseService();

    }


    // ========================================
    // TEST 1 - ADD EXPENSES
    // ========================================

    @Test
    void shouldAddExpensesWithSequentialIds() {

        Expense first = service.addExpense(
                "Groceries",
                new BigDecimal("25.50"),
                Category.FOOD,
                TEST_DATE
        );

        Expense second = service.addExpense(
                "Bus ticket",
                new BigDecimal("12.00"),
                Category.TRANSPORT,
                TEST_DATE
        );

        assertEquals(1, first.getId());

        assertEquals(2, second.getId());

        assertEquals(2, service.getAllExpenses().size());
    }


    // ========================================
    // TEST 2 - INPUT VALIDATION
    // ========================================

    @Test
    void shouldRejectInvalidExpenseData() {

        assertThrows(
                IllegalArgumentException.class,
                () -> service.addExpense(
                        "Invalid amount",
                        new BigDecimal("-10.00"),
                        Category.FOOD,
                        TEST_DATE
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.addExpense(
                        "Too many decimals",
                        new BigDecimal("12.345"),
                        Category.FOOD,
                        TEST_DATE
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.addExpense(
                        "   ",
                        new BigDecimal("10.00"),
                        Category.FOOD,
                        TEST_DATE
                )
        );

        assertTrue(service.getAllExpenses().isEmpty());
    }


    // ========================================
    // TEST 3 - CALCULATE TOTALS
    // ========================================

    @Test
    void shouldCalculateTotalAndCategoryTotal() {

        service.addExpense(
                "Lunch",
                new BigDecimal("12.50"),
                Category.FOOD,
                TEST_DATE
        );

        service.addExpense(
                "Coffee",
                new BigDecimal("3.20"),
                Category.FOOD,
                TEST_DATE
        );

        service.addExpense(
                "Taxi",
                new BigDecimal("15.00"),
                Category.TRANSPORT,
                TEST_DATE
        );

        assertEquals(
                new BigDecimal("30.70"),
                service.getTotal()
        );

        assertEquals(
                new BigDecimal("15.70"),
                service.getTotalByCategory(Category.FOOD)
        );

        assertEquals(
                new BigDecimal("15.00"),
                service.getTotalByCategory(Category.TRANSPORT)
        );
    }


    // ========================================
    // TEST 4 - FILTER BY CATEGORY
    // ========================================

    @Test
    void shouldFilterExpensesByCategory() {

        service.addExpense(
                "Lunch",
                new BigDecimal("10.00"),
                Category.FOOD,
                TEST_DATE
        );

        service.addExpense(
                "Bus ticket",
                new BigDecimal("5.00"),
                Category.TRANSPORT,
                TEST_DATE
        );

        service.addExpense(
                "Coffee",
                new BigDecimal("2.50"),
                Category.FOOD,
                TEST_DATE
        );

        List<Expense> foodExpenses =
                service.getExpensesByCategory(Category.FOOD);

        assertEquals(2, foodExpenses.size());

        assertTrue(
                foodExpenses.stream().allMatch(
                        expense ->
                                expense.getCategory() == Category.FOOD
                )
        );
    }


    // ========================================
    // TEST 5 - MONTHLY TOTAL
    // ========================================

    @Test
    void shouldCalculateMonthlyTotal() {

        service.addExpense(
                "October expense",
                new BigDecimal("40.00"),
                Category.SHOPPING,
                LocalDate.of(2026, 10, 5)
        );

        service.addExpense(
                "Another October expense",
                new BigDecimal("10.00"),
                Category.FOOD,
                LocalDate.of(2026, 10, 15)
        );

        service.addExpense(
                "September expense",
                new BigDecimal("20.00"),
                Category.OTHER,
                LocalDate.of(2026, 9, 20)
        );

        assertEquals(
                new BigDecimal("50.00"),
                service.getTotalForMonth(
                        YearMonth.of(2026, 10)
                )
        );

        assertEquals(
                new BigDecimal("20.00"),
                service.getTotalForMonth(
                        YearMonth.of(2026, 9)
                )
        );
    }


    // ========================================
    // TEST 6 - DELETE EXPENSE
    // ========================================

    @Test
    void shouldDeleteExpenseById() {

        Expense expense = service.addExpense(
                "Shopping",
                new BigDecimal("25.00"),
                Category.SHOPPING,
                TEST_DATE
        );

        boolean deleted =
                service.deleteExpense(expense.getId());

        assertTrue(deleted);

        assertTrue(service.getAllExpenses().isEmpty());

        assertEquals(
                new BigDecimal("0.00"),
                service.getTotal()
        );

        // Deleting the same expense again must fail.

        assertFalse(
                service.deleteExpense(expense.getId())
        );
    }


    // ========================================
    // TEST 7 - RESTORE NEXT ID
    // ========================================

    @Test
    void shouldRestoreNextIdFromSavedExpenses() {

        Expense first = new Expense(
                3,
                "Saved expense 1",
                new BigDecimal("10.00"),
                Category.FOOD,
                TEST_DATE
        );

        Expense second = new Expense(
                7,
                "Saved expense 2",
                new BigDecimal("20.00"),
                Category.TRANSPORT,
                TEST_DATE
        );

        ExpenseService restoredService =
                new ExpenseService(
                        List.of(first, second)
                );

        Expense newExpense = restoredService.addExpense(
                "New expense",
                new BigDecimal("5.00"),
                Category.OTHER,
                TEST_DATE
        );

        assertEquals(8, newExpense.getId());
    }


    // ========================================
    // TEST 8 - DUPLICATE IDs
    // ========================================

    @Test
    void shouldRejectDuplicateIdsDuringRestoration() {

        Expense first = new Expense(
                1,
                "Expense A",
                new BigDecimal("10.00"),
                Category.FOOD,
                TEST_DATE
        );

        Expense second = new Expense(
                1,
                "Expense B",
                new BigDecimal("20.00"),
                Category.OTHER,
                TEST_DATE
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new ExpenseService(
                        List.of(first, second)
                )
        );
    }


    // ========================================
    // TEST 9 - ENCAPSULATION
    // ========================================

    @Test
    void shouldReturnUnmodifiableExpenseList() {

        service.addExpense(
                "Test expense",
                new BigDecimal("10.00"),
                Category.FOOD,
                TEST_DATE
        );

        List<Expense> expenses =
                service.getAllExpenses();

        assertThrows(
                UnsupportedOperationException.class,
                expenses::clear
        );

        assertEquals(1, service.getAllExpenses().size());
    }
}