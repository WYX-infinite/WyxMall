package com.test.form;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
public class ProductForm {
    private Boolean status;
    private Integer id;
    private String name;
    private BigDecimal price;
    private Integer stock;
    private String description;
    private String icon;
    private ProductInnerForm category;
}
