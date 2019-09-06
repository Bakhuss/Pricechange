package pricechange.csv.model;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CSVPrice4KITAIAVTORUS {
    String number;
    String model;
    String name;
    String field4;
    String field5;
    String field6;
    String field7;
}
