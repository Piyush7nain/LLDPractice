package Excel;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CellServiceTest {

    CellService cellService = new CellService();

    @Test
    public void getFinalValue() {
        String v1 = "-2";
        String v2 = "=-2+1";
        String v3 = "=-2+1+-1";
        Assertions.assertEquals(-2,cellService.getFinalValue(v1));
        Assertions.assertEquals(-1,cellService.getFinalValue(v2));
        Assertions.assertEquals(-2,cellService.getFinalValue(v3));

    }
}
