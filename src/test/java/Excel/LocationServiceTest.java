package Excel;

import org.junit.Test;
import org.junit.jupiter.api.Assertions;

public class LocationServiceTest {

    LocationService locationService = new LocationService();

    @Test
    public void parseLocationTest() {

        String l1 = "A1";
        Location L1 = new Location("A1", 1, 1);
        Assertions.assertEquals(L1, locationService.parseLocation(l1));
    }
}
