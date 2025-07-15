package Excel.service;

import Excel.model.Cell;

import java.util.Map;

public interface Printer {

    void print(Map<String, Cell> spreadSheet);
}
