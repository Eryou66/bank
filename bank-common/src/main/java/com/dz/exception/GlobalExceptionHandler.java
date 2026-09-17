package com.dz.exception;

import com.dz.api.ErrorCode;
import com.dz.api.R;
import com.dz.context.RequestContext;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 业务异常：预期内的流程分支，warn 级即可 */
    @ExceptionHandler(BizException.class)
    public ResponseEntity<R<Void>> handleBizException(BizException e){
        log.warn("[biz] traceId={} code={} msg={}",
                RequestContext.getTraceId(), e.getErrorCode().getCode(), e.getMessage());
        return ResponseEntity.status(e.getErrorCode().getHttpStatus())
                .body(R.fail(e.getErrorCode(), e.getMessage()));
    }

    /** @RequestBody 上的 @Valid 校验失败 */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<R<Void>> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String message = fieldError == null ? ErrorCode.PARAM_INVALID.getMessage() : fieldError.getDefaultMessage();
        log.warn("[param] traceId={} msg={}", RequestContext.getTraceId(), message);
        return ResponseEntity.status(ErrorCode.PARAM_INVALID.getHttpStatus())
                .body(R.fail(ErrorCode.PARAM_INVALID, message));
    }

    /** 方法参数级校验失败（@Validated 在类上时触发） */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<R<Void>> handleConstraintViolation(ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
                .findFirst().map(ConstraintViolation::getMessage)
                .orElse(ErrorCode.PARAM_INVALID.getMessage());
        log.warn("[param] traceId={} msg={}", RequestContext.getTraceId(), message);
        return ResponseEntity.status(ErrorCode.PARAM_INVALID.getHttpStatus())
                .body(R.fail(ErrorCode.PARAM_INVALID, message));
    }

    /** 兜底：未预期异常必须打全栈，但对外不暴露细节 */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<R<Void>> handleUnknownException(Exception e) {
        log.error("[system] traceId={} 未捕获异常", RequestContext.getTraceId(), e);
        return ResponseEntity.status(ErrorCode.SYSTEM_ERROR.getHttpStatus())
                .body(R.fail(ErrorCode.SYSTEM_ERROR));
    }
}
