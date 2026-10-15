package io.github.frachiaradia03.expensetracker;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CsvExpenseRepositoryTest {

    @TempDir
    Path temporaryDirectory;

    private static final LocalDate TEST_DATE =
            LocalDate.of(2026, 10, 15);


    // ========================================
    // TEST 1 - MISSING CSV FILE
    // ========================================

    @Test
    void shouldReturnEmptyListWhenFileDoesNotExist()
            throws IOException {

        Path csvPath =
                temporaryDirectory.resolve("expenses.csv");

        CsvExpenseRepository repository =
                new CsvExpenseRepository(csvPath);

        List<Expense> expenses = repository.load();

        assertTrue(expenses.isEmpty());
    }


    // ========================================
    // TEST 2 - SAVE AND LOAD
    // ========================================

    @Test
    void shouldSaveAndLoadExpenses()
            throws IOException {

        Path csvPath =
                temporaryDirectory.resolve("expenses.csv");

        CsvExpenseRepository repository =
                new CsvExpenseRepository(csvPath);

        Expense first = new Expense(
                1,
                "Groceries",
                new BigDecimal("45.90"),
                Category.FOOD,
                TEST_DATE
        );

        Expense second = new Expense(
                2,
                "Bus ticket",
                new BigDecimal("12.50"),
                Category.TRANSPORT,
                TEST_DATE
        );

        repository.save(List.of(first, second));

        assertTrue(Files.exists(csvPath));

        List<Expense> loadedExpenses =
                repository.load();

        assertEquals(2, loadedExpenses.size());

        Expense loadedFirst = loadedExpenses.get(0);

        assertEquals(1, loadedFirst.getId());

        assertEquals(
                "Groceries",
                loadedFirst.getDescription()
        );

        assertEquals(
                new BigDecimal("45.90"),
                loadedFirst.getAmount()
        );

        assertEquals(
                Category.FOOD,
                loadedFirst.getCategory()
        );

        assertEquals(
                TEST_DATE,
                loadedFirst.getDate()
        );

        Expense loadedSecond = loadedExpenses.get(1);

        assertEquals(2, loadedSecond.getId());

        assertEquals(
                "Bus ticket",
                loadedSecond.getDescription()
        );
    }


    // ========================================
    // TEST 3 - SPECIAL CHARACTERS
    // ========================================

    @Test
    void shouldHandleCommasAndQuotes()
            throws IOException {

        Path csvPath =
                temporaryDirectory.resolve("expenses.csv");

        CsvExpenseRepository repository =
                new CsvExpenseRepository(csvPath);

        String description =
                "Lunch, \"special menu\"";

        Expense expense = new Expense(
                1,
                description,
                new BigDecimal("25.50"),
                Category.FOOD,
                TEST_DATE
        );

        repository.save(List.of(expense));

        String csvContent = Files.readString(
                csvPath,
                StandardCharsets.UTF_8
        );

        assertTrue(
                csvContent.contains(
                        "\"Lunch, \"\"special menu\"\"\""
                )
        );

        List<Expense> loadedExpenses =
                repository.load();

        assertEquals(
                description,
                loadedExpenses.get(0).getDescription()
        );
    }


    // ========================================
    // TEST 4 - INVALID HEADER
    // ========================================

    @Test
    void shouldRejectInvalidCsvHeader()
            throws IOException {

        Path csvPath =
                temporaryDirectory.resolve("expenses.csv");

        Files.writeString(
                csvPath,
                "invalid,header\n",
                StandardCharsets.UTF_8
        );

        CsvExpenseRepository repository =
                new CsvExpenseRepository(csvPath);

        assertThrows(
                IOException.class,
                repository::load
        );
    }


    // ========================================
    // TEST 5 - INVALID AMOUNT
    // ========================================

    @Test
    void shouldRejectInvalidExpenseAmount()
            throws IOException {

        Path csvPath =
                temporaryDirectory.resolve("expenses.csv");

        String csvContent =
                "id,description,amount,category,date\n" +
                "1,\"Invalid expense\",abc,FOOD,2026-10-15\n";

        Files.writeString(
                csvPath,
                csvContent,
                StandardCharsets.UTF_8
        );

        CsvExpenseRepository repository =
                new CsvExpenseRepository(csvPath);

        assertThrows(
                IOException.class,
                repository::load
        );
    }


    // ========================================
    // TEST 6 - DUPLICATE IDs
    // ========================================

    @Test
    void shouldRejectDuplicateIdsInCsv()
            throws IOException {

        Path csvPath =
                temporaryDirectory.resolve("expenses.csv");

        String csvContent =
                "id,description,amount,category,date\n" +
                "1,\"Expense A\",10.00,FOOD,2026-10-15\n" +
                "1,\"Expense B\",20.00,OTHER,2026-10-15\n";

        Files.writeString(
                csvPath,
                csvContent,
                StandardCharsets.UTF_8
        );

        CsvExpenseRepository repository =
                new CsvExpenseRepository(csvPath);

        assertThrows(
                IOException.class,
                repository::load
        );
    }


    // ========================================
    // TEST 7 - FAILED SAVE PRESERVES DATA
    // ========================================

    @Test
    void shouldPreservePreviousFileWhenSaveFails()
            throws IOException {

        Path csvPath =
                temporaryDirectory.resolve("expenses.csv");

        CsvExpenseRepository repository =
                new CsvExpenseRepository(csvPath);

        Expense originalExpense = new Expense(
                1,
                "Original expense",
                new BigDecimal("30.00"),
                Category.FOOD,
                TEST_DATE
        );

        // Save valid data first.

        repository.save(List.of(originalExpense));

        String originalContent = Files.readString(
                csvPath,
                StandardCharsets.UTF_8
        );

        // This description contains a line break,
        // which our CSV implementation does not allow.

        Expense invalidExpense = new Expense(
                2,
                "Invalid\nDescription",
                new BigDecimal("20.00"),
                Category.OTHER,
                TEST_DATE
        );

        assertThrows(
                IOException.class,
                () -> repository.save(
                        List.of(invalidExpense)
                )
        );

        // The existing file must remain unchanged.

        String currentContent = Files.readString(
                csvPath,
                StandardCharsets.UTF_8
        );

        assertEquals(
                originalContent,
                currentContent
        );

        // The original expense must still be readable.

        List<Expense> loadedExpenses =
                repository.load();

        assertEquals(1, loadedExpenses.size());

        assertEquals(
                "Original expense",
                loadedExpenses.get(0).getDescription()
        );
    }
}