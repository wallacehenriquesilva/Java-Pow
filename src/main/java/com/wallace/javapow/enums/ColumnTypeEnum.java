package com.wallace.javapow.enums;

import com.wallace.javapow.enums.interfaces.ColumnTypeInterface;

public enum ColumnTypeEnum implements ColumnTypeInterface {
    STRING {
        @Override
        public Object transform(String value) {
            return value;
        }
    },
    INTEGER {
        @Override
        public Object transform(String value) {
            return Integer.parseInt(value);
        }
    },
    BOOLEAN{
        @Override
        public Object transform(String value) {
            return value.equals(value);
        }
    }
}
