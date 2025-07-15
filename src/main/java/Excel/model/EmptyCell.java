package Excel.model;

public class EmptyCell implements Cell {

    public static final String EMPTY_STRING = "";

    @Override
    public String getRawValue() {
        return "";
    }

    @Override
    public String getComputedValue() {
        return EMPTY_STRING;
    }
}
