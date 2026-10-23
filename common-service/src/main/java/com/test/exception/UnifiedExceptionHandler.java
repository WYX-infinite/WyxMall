package com.test.exception;

import com.test.util.ResultVOUtil;
import com.test.vo.ResultVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class UnifiedExceptionHandler {
    @ExceptionHandler(value = Exception.class)
    public ResultVO handleException(Exception e){
        log.info("服务器内部异常,{}",e);
        return ResultVOUtil.fail(e.getMessage());
    }
}
