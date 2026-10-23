package com.test.service.impl;

import com.test.mapper.ProductInfoMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProductInfoServiceImplTest {
    private ProductInfoMapper mapper;
    private ProductInfoServiceImpl service;

    @BeforeEach
    void setUp() {
        mapper = mock(ProductInfoMapper.class);
        service = new ProductInfoServiceImpl();
        ReflectionTestUtils.setField(service, "productInfoMapper", mapper);
    }

    @Test
    void decreasesStockWithOneAtomicStatement() {
        when(mapper.decreaseStock(79, 2)).thenReturn(1);
        assertTrue(service.subStockById(79, 2));
        verify(mapper).decreaseStock(79, 2);
    }

    @Test
    void restoresStockWithOneAtomicStatement() {
        when(mapper.increaseStock(79, 2)).thenReturn(1);
        assertTrue(service.subStockById(79, -2));
        verify(mapper).increaseStock(79, 2);
    }
}
