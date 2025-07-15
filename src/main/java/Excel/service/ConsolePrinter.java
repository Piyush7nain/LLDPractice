package Excel.service;

import Excel.model.Cell;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConsolePrinter implements Printer {
    @Override
    public void print(Map<String, Cell> cells) {
        System.out.println("\n--- Spreadsheet Contents ---");
        // Determine the maximum row and column to print a grid
        int maxRow = 0;
        int maxCol = 0;

        for (String cellName : cells.keySet()) {
            Matcher matcher = Pattern.compile("([A-Z]+)(\\d+)").matcher(cellName);
            if (matcher.matches()) {
                String colStr = matcher.group(1);
                int rowNum = Integer.parseInt(matcher.group(2));

                maxRow = Math.max(maxRow, rowNum);
                maxCol = Math.max(maxCol, columnNameToIndex(colStr));
            }
        }

        // Print header row
        System.out.print("      |");
        for (int c = 0; c <= maxCol; c++) {
            System.out.printf("%-15s|", indexToColumnName(c) + "   "); // Column name with padding
        }
        System.out.println("\n------+----------------------------------------------------------"); // Separator

        for (int r = 1; r <= maxRow; r++) {
            System.out.printf("R%-4d|", r); // Row number
            for (int c = 0; c <= maxCol; c++) {
                String cellName = indexToColumnName(c) + r;
                Cell cell = cells.get(cellName);

                String raw = (cell != null) ? cell.getRawValue() : "";
                String computed = (cell != null) ? cell.getComputedValue() : "";

                // Format for "raw/computed"
                String displayValue = String.format("%s/%s", raw, computed);
                System.out.printf("%-15s|", displayValue);
            }
            System.out.println();
        }
        System.out.println("----------------------------");
    }

    private int columnNameToIndex(String colName) {
        int index = 0;
        for (char c : colName.toCharArray()) {
            index = index * 26 + (c - 'A' + 1);
        }
        return index - 1; // Convert to 0-based index
    }

    // Helper to convert column index (0, 1, 2...) to name (A, B, C...)
    private String indexToColumnName(int index) {
        StringBuilder sb = new StringBuilder();
        do {
            sb.append((char) ('A' + (index % 26)));
            index /= 26;
        } while (index > 0);
        return sb.reverse().toString();
    }
}
