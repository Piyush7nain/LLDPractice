package Excel;

import Excel.service.ConsolePrinter;
import Excel.service.Printer;
import Excel.service.SpreadSheet;

public class SpreadSheetRunner {

    public static void main(String[] args) {
        Printer printer = new ConsolePrinter();
        SpreadSheet sheet = new SpreadSheet(printer);


        System.out.println("--- Initializing Spreadsheet ---");
        sheet.createOrUpdateCell("A1", "5");
        sheet.createOrUpdateCell("B1", "=20+1");
        sheet.createOrUpdateCell("A2", "-9");
        sheet.createOrUpdateCell("A3", ""); // Reset A3
        sheet.createOrUpdateCell("B3", "1");
        sheet.createOrUpdateCell("C3", "100");
        sheet.createOrUpdateCell("D3", "=2+8");


        sheet.print();

        System.out.println("\n--- Modifying some cells ---");
        sheet.createOrUpdateCell("A1", "=5+5"); // Change A1 to a formula
        sheet.createOrUpdateCell("A2", "");     // Empty A2
        sheet.createOrUpdateCell("B1", "21");   // Change B1 to an integer
        sheet.createOrUpdateCell("E5", "77");   // Add a cell further down

        sheet.print();

        System.out.println("\n--- Testing Edge Cases / Invalid Inputs (will throw exceptions) ---");
        try {
            sheet.createOrUpdateCell("Z100", "invalid-value"); // Unsupported format
        } catch (IllegalArgumentException e) {
            System.err.println("Error: " + e.getMessage());
        }

        try {
            sheet.createOrUpdateCell("A1", "=2+8+5"); // Invalid formula format
        } catch (IllegalArgumentException e) {
            System.err.println("Error: " + e.getMessage());
        }

        try {
            sheet.createOrUpdateCell("1A", "10"); // Invalid cell name
        } catch (IllegalArgumentException e) {
            System.err.println("Error: " + e.getMessage());
        }

        sheet.print(); // Print after errors to show current stat
    }

}
