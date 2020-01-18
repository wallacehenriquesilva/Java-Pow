package com.wallace.javapow.readers;

import com.wallace.javapow.annotations.Csv;
import com.wallace.javapow.annotations.CsvBooleanColumn;
import com.wallace.javapow.annotations.CsvCollectionColumn;
import com.wallace.javapow.annotations.CsvColumn;
import com.wallace.javapow.enums.ColumnTypeEnum;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class CsvReader<T> {
    private Class<T> genericType;

    public CsvReader(Class<T> genericType) {
        this.genericType = genericType;
    }

    public List<T> read(String csvFilePath) {
        List<T> objectList = new ArrayList<>();
        boolean isValid = Arrays.stream(genericType.getAnnotations())
                .anyMatch(annotation -> annotation instanceof Csv);

        if (!isValid) {
            return Collections.emptyList();
        }

        final String delimiter = Arrays.stream(genericType.getAnnotations())
                .filter(annotation -> annotation instanceof Csv)
                .findFirst()
                .map(annotation -> ((Csv) annotation).delimiter())
                .orElse(";");

        Map<String, Integer> headersMap = this.getHeaders(csvFilePath, delimiter);

        String line = "";
        int row = 0;

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(csvFilePath))) {
            while (Objects.nonNull(line = bufferedReader.readLine())) {
                T object = genericType.newInstance();
                if (row == 0) {
                    row++;
                    continue;
                }
                String[] values = line.split(delimiter);
                Field[] fields = genericType.getDeclaredFields();

                for (Field field : fields) {
                    Annotation[] annotations = field.getDeclaredAnnotations();
                    for (Annotation annotation : annotations) {
                        if (annotation instanceof CsvColumn) {
                            String name = ((CsvColumn) annotation).name();
                            ColumnTypeEnum columnTypeEnum = ((CsvColumn) annotation).type();
                            int column = headersMap.get(name);
                            if (values.length > column) {
                                Object value = columnTypeEnum.transform(values[column]);
                                boolean accessible = field.isAccessible();
                                field.setAccessible(true);
                                field.set(object, value);
                                field.setAccessible(accessible);
                            }
                        } else if (annotation instanceof CsvBooleanColumn) {
                            String name = ((CsvBooleanColumn) annotation).name();
                            boolean isCaseSensitive = ((CsvBooleanColumn) annotation).isCaseSensitive();
                            String[] trueValues = ((CsvBooleanColumn) annotation).trueValues();
                            int column = headersMap.get(name);
                            if (values.length > column) {
                                String value = values[column];
                                Object val;
                                if (isCaseSensitive) {
                                    val = Arrays.asList(trueValues).contains(value);
                                } else {
                                    val = Arrays.stream(trueValues).anyMatch(value::equalsIgnoreCase);
                                }
                                boolean accessible = field.isAccessible();
                                field.setAccessible(true);
                                field.set(object, val);
                                field.setAccessible(accessible);
                            }
                        } else if (annotation instanceof CsvCollectionColumn) {
                            String name = ((CsvCollectionColumn) annotation).name();
                            int column = headersMap.get(name);
                            if (values.length > column) {
                                String columnDelimiter = ((CsvCollectionColumn) annotation).collectionDelimiterRegex();
                                ParameterizedType pt = (ParameterizedType) field.getGenericType();
                                Class clazz = (Class) pt.getActualTypeArguments()[0];
                                Object value = this.transformObject(clazz, values[column], name, columnDelimiter);
                                boolean accessible = field.isAccessible();
                                field.setAccessible(true);
                                field.set(object, value);
                                field.setAccessible(accessible);
                            }
                        }
                    }
                }
                objectList.add(object);
            }
        } catch (IOException | IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        }
        return objectList;
    }

    private Collection<?> transformObject(Class clazz, String value, String columnName, String delimiter) {
        String[] valores = value.split(delimiter);
        Set<Object> objectsList = new HashSet<>();
        try {
            for (String val : valores) {
                val = val.trim();
                Object object = clazz.newInstance();
                Field[] fields = clazz.getDeclaredFields();
                for (Field field : fields) {
                    Annotation[] annotations = field.getDeclaredAnnotations();
                    for (Annotation annotation : annotations) {
                        if (annotation instanceof CsvColumn) {
                            String name = ((CsvColumn) annotation).name();
                            if (columnName.equals(name)) {
                                boolean accessible = field.isAccessible();
                                field.setAccessible(true);
                                field.set(object, val);
                                field.setAccessible(accessible);
                            }
                        }
                    }
                }
                objectsList.add(object);
            }
        } catch (IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        }
        return objectsList;
    }

    private Map<String, Integer> getHeaders(final String csvFilePath, final String delimiter) {
        String line = "";
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(csvFilePath))) {
            if (Objects.nonNull(line = bufferedReader.readLine())) {
                String[] data = line.split(delimiter);
                AtomicInteger atomicInteger = new AtomicInteger(0);
                return Arrays.stream(data)
                        .map(String::trim)
                        .collect(Collectors.toMap(value -> value,
                                val -> atomicInteger.getAndIncrement()
                        ));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}