package io.github.frachiaradia03.expensetracker;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Objects;

public class Expense {

    // Attributes

    private final int id;
    private final String description;
    private final BigDecimal amount;
    private final Category category;
    private final LocalDate date;


    // Constructor

    public Expense(
            int id,
            String description,
            BigDecimal amount,
            Category category,
            LocalDate date
    ) {

        // Validate ID

        if (id <= 0) {
            throw new IllegalArgumentException(
                    "Expense ID must be positive."
            );
        }

        // Validate description

        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException(
                    "Description cannot be empty."
            );
        }

        // Validate amount

        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException(
                    "Amount must be greater than zero."
            );
        }

        BigDecimal validatedAmount;

        try {
            validatedAmount = amount.setScale(
                    2,
                    RoundingMode.UNNECESSARY
            );
        } catch (ArithmeticException ex) {
            throw new IllegalArgumentException(
                    "Amount must have at most two decimal places.",
                    ex
            );
        }

        // Assign values

        this.id = id;
        this.description = description.trim();
        this.amount = validatedAmount;
        this.category = Objects.requireNonNull(
                category,
                "Category cannot be null."
        );
        this.date = Objects.requireNonNull(
                date,
                "Date cannot be null."
        );
    }


    // Getters

    public int getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Category getCategory() {
        return category;
    }

    public LocalDate getDate() {
        return date;
    }


    // String representation

    @Override
    public String toString() {

        return String.format(
                "ID: %d | Date: %s | Category: %s | Description: %s | Amount: €%s",
                id,
                date,
                category,
                description,
                amount
        );
    }
}