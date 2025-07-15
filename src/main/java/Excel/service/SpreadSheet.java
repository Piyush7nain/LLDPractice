package Excel.service;

import Excel.model.Cell;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class SpreadSheet {

    private final Map<String, Cell> sheet;

    private static final Pattern CELL_NAME_PATTERN = Pattern.compile("^[A-Z]+\\d+$");
    private final Printer printer;
    public SpreadSheet(Printer printer) {
        this.printer = printer;
        this.sheet = new HashMap<>();
    }

    public void createOrUpdateCell(String cellName, String cellValue) {
        if(!isValidCellName(cellName)) {
            throw new IllegalArgumentException("Invalid cell name format: " + cellName);
        }

        Cell cell = CellFactory.createCell(cellValue);
        sheet.put(cellName, cell);
        System.out.println("Modified " + cellName + " to: " + cellValue); // For debugging/trace
    }

    public void print(){
        printer.print(sheet);
    }

    private boolean isValidCellName(String cellName) {
        return CELL_NAME_PATTERN.matcher(cellName).matches();
    }


}
