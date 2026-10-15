package io.github.frachiaradia03.expensetracker;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class CsvExpenseRepository {

    private static final String HEADER =
            "id,description,amount,category,date";

    private final Path filePath;


    // ========================================
    // CONSTRUCTOR
    // ========================================

    public CsvExpenseRepository(Path filePath) {

        this.filePath = Objects.requireNonNull(
                filePath,
                "CSV file path cannot be null."
        ).toAbsolutePath();

    }


    // ========================================
    // SAVE EXPENSES
    // ========================================

    public void save(List<Expense> expenses)
            throws IOException {

        Objects.requireNonNull(expenses);

        Path directory = filePath.getParent();

        Files.createDirectories(directory);

        // Create a temporary file in the same directory.

        Path temporaryFile = Files.createTempFile(
                directory,
                "expenses-",
                ".tmp"
        );

        try {

            try (BufferedWriter writer =
                         Files.newBufferedWriter(
                                 temporaryFile,
                                 StandardCharsets.UTF_8
                         )) {

                writer.write(HEADER);
                writer.newLine();

                for (Expense expense : expenses) {

                    String description =
                            expense.getDescription();

                    if (description.contains("\n")
                            || description.contains("\r")) {

                        throw new IOException(
                                "Descriptions cannot contain line breaks."
                        );
                    }

                    String row =
                            expense.getId() + "," +
                            escapeCsv(description) + "," +
                            expense.getAmount().toPlainString() + "," +
                            expense.getCategory().name() + "," +
                            expense.getDate();

                    writer.write(row);
                    writer.newLine();
                }
            }

            // Replace the previous CSV only after
            // the temporary file has been written.

            try {

                Files.move(
                        temporaryFile,
                        filePath,
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE
                );

            } catch (AtomicMoveNotSupportedException e) {

                Files.move(
                        temporaryFile,
                        filePath,
                        StandardCopyOption.REPLACE_EXISTING
                );
            }

        } finally {

            Files.deleteIfExists(temporaryFile);
        }
    }


    // ========================================
    // LOAD EXPENSES
    // ========================================

    public List<Expense> load() throws IOException {

        List<Expense> expenses = new ArrayList<>();

        if (Files.notExists(filePath)) {

            return expenses;
        }

        Set<Integer> usedIds = new HashSet<>();

        try (BufferedReader reader =
                     Files.newBufferedReader(
                             filePath,
                             StandardCharsets.UTF_8
                     )) {

            String header = reader.readLine();

            if (!HEADER.equals(header)) {

                throw new IOException(
                        "Invalid CSV header."
                );
            }

            String line;
            int lineNumber = 1;

            while ((line = reader.readLine()) != null) {

                lineNumber++;

                if (line.isBlank()) {
                    continue;
                }

                List<String> fields;

                try {

                    fields = parseCsvLine(line);

                } catch (IOException e) {

                    throw new IOException(
                            "Invalid CSV syntax at line "
                                    + lineNumber,
                            e
                    );
                }

                if (fields.size() != 5) {

                    throw new IOException(
                            "Invalid number of fields at line "
                                    + lineNumber
                    );
                }

                try {

                    int id = Integer.parseInt(
                            fields.get(0)
                    );

                    String description = fields.get(1);

                    BigDecimal amount = new BigDecimal(
                            fields.get(2)
                    );

                    Category category = Category.valueOf(
                            fields.get(3)
                    );

                    LocalDate date = LocalDate.parse(
                            fields.get(4)
                    );

                    Expense expense = new Expense(
                            id,
                            description,
                            amount,
                            category,
                            date
                    );

                    if (!usedIds.add(id)) {

                        throw new IllegalArgumentException(
                                "Duplicate expense ID: " + id
                        );
                    }

                    expenses.add(expense);

                } catch (RuntimeException e) {

                    throw new IOException(
                            "Invalid expense data at line "
                                    + lineNumber + ": "
                                    + e.getMessage(),
                            e
                    );
                }
            }
        }

        return expenses;
    }


    // ========================================
    // ESCAPE CSV TEXT
    // ========================================

    private static String escapeCsv(String value) {

        return "\"" + value.replace("\"", "\"\"") + "\"";

    }


    // ========================================
    // PARSE A CSV LINE
    // ========================================

    private static List<String> parseCsvLine(String line)
            throws IOException {

        List<String> fields = new ArrayList<>();

        StringBuilder current = new StringBuilder();

        boolean insideQuotes = false;
        boolean closedQuote = false;

        for (int i = 0; i < line.length(); i++) {

            char character = line.charAt(i);

            if (insideQuotes) {

                if (character == '"') {

                    if (i + 1 < line.length()
                            && line.charAt(i + 1) == '"') {

                        current.append('"');
                        i++;

                    } else {

                        insideQuotes = false;
                        closedQuote = true;
                    }

                } else {

                    current.append(character);
                }

            } else if (closedQuote) {

                if (character == ',') {

                    fields.add(current.toString());
                    current.setLength(0);
                    closedQuote = false;

                } else {

                    throw new IOException(
                            "Unexpected character after quoted field."
                    );
                }

            } else if (character == ',') {

                fields.add(current.toString());
                current.setLength(0);

            } else if (character == '"') {

                if (current.length() != 0) {

                    throw new IOException(
                            "Unexpected quotation mark."
                    );
                }

                insideQuotes = true;

            } else {

                current.append(character);
            }
        }

        if (insideQuotes) {

            throw new IOException(
                    "Unclosed quotation mark."
            );
        }

        fields.add(current.toString());

        return fields;
    }
}