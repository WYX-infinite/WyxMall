package com.test.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
public class SellerProductFindVO {
    private Boolean status;
    private Integer id;
    private String name;
    private BigDecimal price;
    private Integer stock;
    private String description;
    private String icon;
    private Map category;
}
