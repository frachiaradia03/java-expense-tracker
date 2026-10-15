package io.github.frachiaradia03.expensetracker;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class App {

   public static void main(String[] args) {

    // ========================================
    // INITIALIZE CSV REPOSITORY
    // ========================================

    Path csvPath = Path.of("data", "expenses.csv");

    CsvExpenseRepository repository =
            new CsvExpenseRepository(csvPath);

    ExpenseService service;


    // ========================================
    // LOAD SAVED EXPENSES
    // ========================================

    try {

        service = new ExpenseService(
                repository.load()
        );

        System.out.println(
                "Loaded " + service.getAllExpenses().size()
                        + " expense(s) from CSV."
        );

    } catch (IOException | IllegalArgumentException e) {

        System.err.println(
                "Error loading saved expenses: "
                        + e.getMessage()
        );

        System.err.println(
                "Application stopped to avoid overwriting "
                        + "the existing CSV file."
        );

        return;
    }


    // ========================================
    // START INTERACTIVE MENU
    // ========================================

    try (Scanner scanner = new Scanner(System.in)) {

        boolean running = true;

        while (running) {

            printMenu();

            int choice = readInt(
                    scanner,
                    "Select an option: "
            );

            switch (choice) {

                case 1 -> addExpense(
                        scanner,
                        service
                );

                case 2 -> showExpenses(
                        service.getAllExpenses()
                );

                case 3 -> filterByCategory(
                        scanner,
                        service
                );

                case 4 -> showTotal(service);

                case 5 -> showCategorySummary(service);

                case 6 -> showMonthlySummary(
                        scanner,
                        service
                );

                case 7 -> deleteExpense(
                        scanner,
                        service
                );

                case 8 -> saveExpenses(
                        repository,
                        service
                );

                case 0 -> {

                    System.out.println(
                            "\nSaving expenses before exit..."
                    );

                    if (saveExpenses(repository, service)) {

                        System.out.println(
                                "Thank you for using Java Expense Tracker!"
                        );

                        running = false;

                    } else {

                        System.out.println(
                                "Save failed. Application will remain open."
                        );
                    }
                }

                default -> System.out.println(
                        "\nInvalid option. Please try again."
                );
            }
        }

    } catch (NoSuchElementException e) {

        System.out.println(
                "\nInput closed. Attempting to save expenses..."
        );

        saveExpenses(repository, service);
    }
}

    // ========================================
    // MAIN MENU
    // ========================================

    private static void printMenu() {

        System.out.println();
        System.out.println("========================================");
        System.out.println("          JAVA EXPENSE TRACKER          ");
        System.out.println("========================================");

        System.out.println("\n              MAIN MENU\n");

        System.out.println("1. Add new expense");
        System.out.println("2. View all expenses");
        System.out.println("3. Filter expenses by category");
        System.out.println("4. View total expenses");
        System.out.println("5. View category summary");
        System.out.println("6. View monthly summary");
        System.out.println("7. Delete an expense");
        System.out.println("8. Save expenses");
        System.out.println("0. Exit");

        System.out.println();
        System.out.println("========================================");
    }


    // ========================================
    // READ INTEGER
    // ========================================

    private static int readInt(
            Scanner scanner,
            String message
    ) {

        while (true) {

            System.out.print(message);

            String input = scanner.nextLine().trim();

            try {

                return Integer.parseInt(input);

            } catch (NumberFormatException e) {

                System.out.println(
                        "Invalid number. Please enter an integer."
                );
            }
        }
    }


    // ========================================
    // READ NON-EMPTY TEXT
    // ========================================

    private static String readText(
            Scanner scanner,
            String message
    ) {

        while (true) {

            System.out.print(message);

            String input = scanner.nextLine().trim();

            if (!input.isEmpty()) {

                return input;
            }

            System.out.println(
                    "This field cannot be empty."
            );
        }
    }


    // ========================================
    // READ AMOUNT
    // ========================================

    private static BigDecimal readAmount(
            Scanner scanner
    ) {

        while (true) {

            System.out.print("Enter amount (EUR): ");

            String input = scanner.nextLine()
                    .trim()
                    .replace(',', '.');

            try {

                BigDecimal amount =
                        new BigDecimal(input);

                if (amount.signum() <= 0) {

                    System.out.println(
                            "Amount must be greater than zero."
                    );

                    continue;
                }

                amount = amount.setScale(
                        2,
                        RoundingMode.UNNECESSARY
                );

                return amount;

            } catch (NumberFormatException e) {

                System.out.println(
                        "Invalid amount. Example: 12.50"
                );

            } catch (ArithmeticException e) {

                System.out.println(
                        "Amount cannot have more than two decimal places."
                );
            }
        }
    }


    // ========================================
    // SELECT CATEGORY
    // ========================================

    private static Category selectCategory(
            Scanner scanner
    ) {

        Category[] categories = Category.values();

        System.out.println("\nAvailable categories:");

        for (int i = 0; i < categories.length; i++) {

            System.out.println(
                    (i + 1) + ". " + categories[i]
            );
        }

        while (true) {

            int choice = readInt(
                    scanner,
                    "Choose category: "
            );

            if (choice >= 1 && choice <= categories.length) {

                return categories[choice - 1];
            }

            System.out.println(
                    "Invalid category. Please try again."
            );
        }
    }


    // ========================================
    // READ DATE
    // ========================================

    private static LocalDate readDate(
            Scanner scanner
    ) {

        while (true) {

            System.out.print(
                    "Enter date (YYYY-MM-DD): "
            );

            String input = scanner.nextLine().trim();

            try {

                return LocalDate.parse(input);

            } catch (DateTimeParseException e) {

                System.out.println(
                        "Invalid date. Example: 2026-10-15"
                );
            }
        }
    }


    // ========================================
    // READ MONTH
    // ========================================

    private static YearMonth readMonth(
            Scanner scanner
    ) {

        while (true) {

            System.out.print(
                    "Enter month (YYYY-MM): "
            );

            String input = scanner.nextLine().trim();

            try {

                return YearMonth.parse(input);

            } catch (DateTimeParseException e) {

                System.out.println(
                        "Invalid month. Example: 2026-10"
                );
            }
        }
    }


    // ========================================
    // ADD EXPENSE
    // ========================================

    private static void addExpense(
            Scanner scanner,
            ExpenseService service
    ) {

        System.out.println("\n--- ADD NEW EXPENSE ---");

        String description = readText(
                scanner,
                "Enter description: "
        );

        BigDecimal amount = readAmount(scanner);

        Category category = selectCategory(scanner);

        LocalDate date = readDate(scanner);

        Expense expense = service.addExpense(
                description,
                amount,
                category,
                date
        );

        System.out.println(
                "\nExpense added successfully!"
        );

        System.out.println(
                "Assigned ID: " + expense.getId()
        );
    }


    // ========================================
    // SHOW EXPENSES
    // ========================================

    private static void showExpenses(
            List<Expense> expenses
    ) {

        System.out.println("\n--- EXPENSE LIST ---");

        if (expenses.isEmpty()) {

            System.out.println("No expenses found.");

            return;
        }

        for (Expense expense : expenses) {

            System.out.println(expense);
        }

        System.out.println(
                "\nTotal records: " + expenses.size()
        );
    }


    // ========================================
    // FILTER BY CATEGORY
    // ========================================

    private static void filterByCategory(
            Scanner scanner,
            ExpenseService service
    ) {

        System.out.println("\n--- FILTER BY CATEGORY ---");

        Category category = selectCategory(scanner);

        List<Expense> filteredExpenses =
                service.getExpensesByCategory(category);

        showExpenses(filteredExpenses);
    }


    // ========================================
    // SHOW TOTAL
    // ========================================

    private static void showTotal(
            ExpenseService service
    ) {

        System.out.println("\n--- TOTAL EXPENSES ---");

        System.out.println(
                "Total amount: €" + service.getTotal()
        );
    }


    // ========================================
    // CATEGORY SUMMARY
    // ========================================

    private static void showCategorySummary(
            ExpenseService service
    ) {

        System.out.println("\n--- CATEGORY SUMMARY ---");

        for (Category category : Category.values()) {

            BigDecimal total =
                    service.getTotalByCategory(category);

            System.out.printf(
                    "%-16s €%s%n",
                    category,
                    total
            );
        }
    }


    // ========================================
    // MONTHLY SUMMARY
    // ========================================

    private static void showMonthlySummary(
            Scanner scanner,
            ExpenseService service
    ) {

        System.out.println("\n--- MONTHLY SUMMARY ---");

        YearMonth month = readMonth(scanner);

        BigDecimal total =
                service.getTotalForMonth(month);

        System.out.println(
                "Total for " + month + ": €" + total
        );
    }


    // ========================================
    // DELETE EXPENSE
    // ========================================

    private static void deleteExpense(
            Scanner scanner,
            ExpenseService service
    ) {

        System.out.println("\n--- DELETE EXPENSE ---");

        int id = readInt(
                scanner,
                "Enter expense ID: "
        );

        if (id <= 0) {

            System.out.println("Invalid expense ID.");

            return;
        }

        boolean deleted = service.deleteExpense(id);

        if (deleted) {

            System.out.println(
                    "Expense deleted successfully!"
            );

        } else {

            System.out.println(
                    "Expense not found."
            );
        }
    }

    // ========================================
// SAVE EXPENSES TO CSV
// ========================================

private static boolean saveExpenses(
        CsvExpenseRepository repository,
        ExpenseService service
) {

    try {

        List<Expense> expenses =
                service.getAllExpenses();

        repository.save(expenses);

        System.out.println(
                "\nSuccessfully saved "
                        + expenses.size()
                        + " expense(s) to data/expenses.csv."
        );

        return true;

    } catch (IOException | SecurityException e) {

        System.err.println(
                "\nError saving expenses: "
                        + e.getMessage()
        );

        return false;
    }
}
}