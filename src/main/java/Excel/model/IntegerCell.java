package Excel.model;

public class IntegerCell implements Cell {
    private final String value;

    public IntegerCell(String value) {
        if (!value.matches("-?\\d{1,3}")) { // Allows negative, up to 3 digits
            throw new IllegalArgumentException("Invalid integer cell value: " + value);
        }
        this.value = value;
    }

    @Override
    public String getRawValue() {
        return value;
    }

    @Override
    public String getComputedValue() {
        return value;
    }
}
