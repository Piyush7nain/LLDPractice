package Excel.service;

import Excel.model.Cell;
import Excel.model.EmptyCell;
import Excel.model.FormulaCell;
import Excel.model.IntegerCell;

public class CellFactory {

    public static Cell createCell(String value) {
        if (value == null || value.isEmpty()) {
            return new EmptyCell();
        } else if (value.startsWith("=")) {
            // It's a formula
            return new FormulaCell(value);
        } else if (value.matches("-?\\d{1,3}")) {
            // It's an integer (handles negative and up to 3 digits)
            return new IntegerCell(value);
        } else {
            // For now, treat unknown types as empty or throw an error.
            // Based on the prompt, only integer and formula are expected.
            // For robustness, we can decide to either ignore, return an EmptyCell, or throw.
            // Throwing an exception is better for clear error handling.
            throw new IllegalArgumentException("Unsupported cell value format: " + value);
        }

    }


}
