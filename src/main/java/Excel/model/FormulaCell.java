package Excel.model;

import Excel.CellUtil.FormulaCalculator;

public class FormulaCell implements Cell {

    private final String rawValue;
    private final String computedValues;
    public FormulaCell(String rawValue) {
        if (!rawValue.startsWith("=")) {
            throw new IllegalArgumentException("Formula must start with '=': " + rawValue);
        }
        this.rawValue = rawValue;
        this.computedValues = FormulaCalculator.calculateFormula(rawValue);
    }

    @Override
    public String getRawValue() {
        return rawValue;
    }

    @Override
    public String getComputedValue() {
        return computedValues;
    }
}
