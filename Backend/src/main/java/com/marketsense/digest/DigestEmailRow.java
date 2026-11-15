package com.marketsense.digest;

import lombok.Value;

import java.util.List;

@Value
public class DigestEmailRow {
    String symbol;
    String name;
    String stockUrl;
    String priceText;
    String changeText;
    String changeColor;
    String sentiment;
    String sentimentColor;
    String confidenceText;
    String catalyst;
    List<StockDigestItem.Headline> headlines;
}
