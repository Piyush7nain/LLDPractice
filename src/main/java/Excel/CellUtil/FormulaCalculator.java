package Excel.CellUtil;

public class FormulaCalculator {

    public static String calculateFormula(String formula) {
        String[] parts = formula.substring(1).split("\\+");
//        if (parts.length != 2) {
//            throw new IllegalArgumentException("Invalid formula format. Expected 'num1+num2': " + formula);
//        }

        try {
            return String.valueOf(addElements(parts, parts.length-1));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid numbers in formula: " + formula, e);
        }
    }
    private static int addElements(String[] nums, int index){
        if(index == 0){
            return Integer.parseInt(nums[0]);
        }
        return Integer.parseInt(nums[index]) + addElements(nums, index - 1);

    }
}
