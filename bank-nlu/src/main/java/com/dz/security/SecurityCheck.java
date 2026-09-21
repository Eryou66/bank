package com.dz.security;

import java.lang.annotation.*;

/** 标注在 Controller 方法上，表示进入业务前先过七步安检流水线 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SecurityCheck {
}
