package com.test.entity;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductInfo {
    private Integer productId;
    private String productName;
    private BigDecimal productPrice;
    private String productIcon;
}
