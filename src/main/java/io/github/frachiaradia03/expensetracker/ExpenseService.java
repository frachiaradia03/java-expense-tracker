package io.github.frachiaradia03.expensetracker;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class ExpenseService {

    // Collection of expenses

    private final List<Expense> expenses;

    // Next available ID

    private int nextId;


// Constructor for a new application session

public ExpenseService() {

    this(List.of());

}


// Constructor for loading saved expenses

public ExpenseService(List<Expense> savedExpenses) {

    Objects.requireNonNull(
            savedExpenses,
            "Saved expenses cannot be null."
    );

    this.expenses = new ArrayList<>();

    Set<Integer> usedIds = new HashSet<>();

    int maxId = 0;

    for (Expense expense : savedExpenses) {

        Objects.requireNonNull(
                expense,
                "An expense cannot be null."
        );

        if (!usedIds.add(expense.getId())) {

            throw new IllegalArgumentException(
                    "Duplicate expense ID: " + expense.getId()
            );
        }

        this.expenses.add(expense);

        if (expense.getId() > maxId) {
            maxId = expense.getId();
        }
    }

    if (maxId == Integer.MAX_VALUE) {

        throw new IllegalArgumentException(
                "No additional expense IDs are available."
        );
    }

    this.nextId = maxId + 1;
}

    // ========================================
    // ADD EXPENSE
    // ========================================

    public Expense addExpense(
            String description,
            BigDecimal amount,
            Category category,
            LocalDate date
    ) {

        Expense expense = new Expense(
                nextId,
                description,
                amount,
                category,
                date
        );

        expenses.add(expense);

        nextId++;

        return expense;
    }


    // ========================================
    // GET ALL EXPENSES
    // ========================================

    public List<Expense> getAllExpenses() {

        return List.copyOf(expenses);

    }


    // ========================================
    // FILTER BY CATEGORY
    // ========================================

    public List<Expense> getExpensesByCategory(
            Category category
    ) {

        Objects.requireNonNull(
                category,
                "Category cannot be null."
        );

        List<Expense> result = new ArrayList<>();

        for (Expense expense : expenses) {

            if (expense.getCategory() == category) {

                result.add(expense);

            }
        }

        return result;
    }


    // ========================================
    // CALCULATE TOTAL EXPENSES
    // ========================================

    public BigDecimal getTotal() {

        BigDecimal total = new BigDecimal("0.00");

        for (Expense expense : expenses) {

            total = total.add(expense.getAmount());

        }

        return total;
    }


    // ========================================
    // TOTAL BY CATEGORY
    // ========================================

    public BigDecimal getTotalByCategory(
            Category category
    ) {

        Objects.requireNonNull(
                category,
                "Category cannot be null."
        );

        BigDecimal total = new BigDecimal("0.00");

        for (Expense expense : expenses) {

            if (expense.getCategory() == category) {

                total = total.add(expense.getAmount());

            }
        }

        return total;
    }


    // ========================================
    // TOTAL BY MONTH
    // ========================================

    public BigDecimal getTotalForMonth(
            YearMonth month
    ) {

        Objects.requireNonNull(
                month,
                "Month cannot be null."
        );

        BigDecimal total = new BigDecimal("0.00");

        for (Expense expense : expenses) {

            YearMonth expenseMonth =
                    YearMonth.from(expense.getDate());

            if (expenseMonth.equals(month)) {

                total = total.add(expense.getAmount());

            }
        }

        return total;
    }


    // ========================================
    // DELETE EXPENSE
    // ========================================

    public boolean deleteExpense(int id) {

        return expenses.removeIf(
                expense -> expense.getId() == id
        );

    }
}