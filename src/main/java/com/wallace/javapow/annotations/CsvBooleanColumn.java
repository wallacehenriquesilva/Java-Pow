package com.wallace.javapow.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CsvBooleanColumn {
    String name();

    boolean isCaseSensitive() default false;

    String[] trueValues();
}
