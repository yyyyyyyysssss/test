package com.work.annotation;

import com.work.annotation.enums.AmountFormat;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ExcelField {

    String columnName() default "未知列名";

    String[] dynamicColumn() default {};

    String[] conditionIgnore() default {};

    AmountFormat amountFormat() default AmountFormat.NOT_FORMAT;

    String dateFormat() default "yyyy-MM-dd HH:mm:ss";

    String qualifiedByName() default "";

    int sort() default -1;
}

