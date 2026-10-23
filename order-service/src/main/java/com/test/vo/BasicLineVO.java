package com.test.vo;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class BasicLineVO {
    private List<String> names;
    private List<Integer> values;
}
