package com.wallace.javapow.annotations;

import com.wallace.javapow.enums.ColumnTypeEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CsvColumn {
    String name();

    ColumnTypeEnum type() default ColumnTypeEnum.STRING;
}