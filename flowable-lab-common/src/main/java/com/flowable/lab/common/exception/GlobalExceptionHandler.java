package com.flowable.lab.common.exception;

import com.flowable.lab.common.response.Result;
import org.flowable.common.engine.api.FlowableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private static final Pattern FLOWABLE_VAR_PATTERN =
            Pattern.compile("Unknown property used in expression: \\$\\{([^}]+)\\}");

    @ExceptionHandler(BizException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleBizException(BizException e) {
        log.warn("BizException: code={}, message={}", e.getCode(), e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(FlowableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleFlowableException(FlowableException e) {
        log.warn("FlowableException: {}", e.getMessage());
        String message = e.getMessage();
        Matcher m = FLOWABLE_VAR_PATTERN.matcher(message);
        if (m.find()) {
            return Result.error("流程变量缺失或无效: " + m.group(1) + "，请检查启动参数");
        }
        return Result.error("流程引擎错误: " + message);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleException(Exception e) {
        log.error("Unexpected error", e);
        return Result.error("服务器内部错误: " + e.getMessage());
    }
}
