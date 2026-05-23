package com.marketsense.common.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CompanyProfileDto {
    String name;
    String ticker;
    String logo;

    @JsonAlias(value = "finnhubIndustry")
    String industry;

    String phone;

    String description;
    Double marketCapitalization;

    @JsonAlias(value = "weburl")
    String webUrl;



    String ipo;
}
