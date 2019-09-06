package pricechange.csv.model;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CSVIksora {
    // MAKER_NAME;DETAIL_NUMBER;DETAIL_NAME;QUANTITY;OUTPUT_PRICE;PARTY_COUNT
    String makerName;
    String detailNumber;
    String detailName;
    String quantity;
    String outputPrice;
    String partyCount;
}
