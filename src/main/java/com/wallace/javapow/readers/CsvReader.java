package com.wallace.javapow.readers;

import com.wallace.javapow.annotations.Csv;
import com.wallace.javapow.annotations.CsvBooleanColumn;
import com.wallace.javapow.annotations.CsvCollectionColumn;
import com.wallace.javapow.annotations.CsvColumn;
import com.wallace.javapow.annotations.CsvDateColumn;
import com.wallace.javapow.enums.ColumnTypeEnum;
import com.wallace.javapow.exceptions.InvalidCsvClassException;
import com.wallace.javapow.exceptions.InvalidDelimiterException;
import com.wallace.javapow.exceptions.InvalidPathException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class CsvReader<T> {
    private Class<T> genericType;

    public CsvReader(Class<T> genericType) {
        this.genericType = genericType;
    }

    public List<T> read() {
        final String path = this.getPathAnnotationValue();
        return read(path);
    }

    public List<T> read(String csvFilePath) {
        final List<T> objectList = new ArrayList<>();

        this.validateClass();

        final String delimiter = this.getDelimiter();

        final Map<String, Integer> headersMap = this.getHeaders(csvFilePath, delimiter);

        String line = "";

        int row = 0;

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(csvFilePath))) {
            while (Objects.nonNull(line = bufferedReader.readLine())) {
                T object = genericType.getDeclaredConstructor().newInstance();

                if (row == 0) {
                    row++;
                    continue;
                }

                final String[] values = line.split(delimiter);
                final Field[] fields = genericType.getDeclaredFields();

                for (Field field : fields) {
                    Annotation[] annotations = field.getDeclaredAnnotations();
                    for (Annotation annotation : annotations) {
                        if (annotation instanceof CsvColumn) {
                            String name = ((CsvColumn) annotation).name();
                            ColumnTypeEnum columnTypeEnum = ((CsvColumn) annotation).type();
                            int column = Objects.nonNull(headersMap) ? headersMap.get(name) : 0;
                            if (values.length > column) {
                                Object value = columnTypeEnum.transform(values[column]);
                                field.setAccessible(true);
                                field.set(object, value);
                            }
                        } else if (annotation instanceof CsvBooleanColumn) {
                            String name = ((CsvBooleanColumn) annotation).name();
                            boolean isCaseSensitive = ((CsvBooleanColumn) annotation).isCaseSensitive();
                            String[] trueValues = ((CsvBooleanColumn) annotation).trueValues();
                            int column = Objects.nonNull(headersMap) ? headersMap.get(name) : 0;
                            if (values.length > column) {
                                String value = values[column];
                                Object val;
                                if (isCaseSensitive) {
                                    val = Arrays.asList(trueValues).contains(value);
                                } else {
                                    val = Arrays.stream(trueValues).anyMatch(value::equalsIgnoreCase);
                                }

                                field.setAccessible(true);
                                field.set(object, val);
                            }
                        } else if (annotation instanceof CsvCollectionColumn) {
                            final String name = ((CsvCollectionColumn) annotation).name();
                            int column = Objects.nonNull(headersMap) ? headersMap.get(name) : 0;
                            if (values.length > column) {
                                final String columnDelimiter = ((CsvCollectionColumn) annotation).collectionDelimiterRegex();
                                final Class<?> collectionClass = field.getType();
                                final Object value = this.transformObject(values[column], columnDelimiter, field, collectionClass);
                                field.setAccessible(true);
                                field.set(object, value);
                            }
                        } else if (annotation instanceof CsvDateColumn) {
                            final String name = ((CsvDateColumn) annotation).name();
                            final String pattern = ((CsvDateColumn) annotation).pattern();

                            final Class<?> fieldClass = ((Class<?>) field.getGenericType());

                            int column = Objects.nonNull(headersMap) ? headersMap.get(name) : 0;

                            if (values.length > column) {

                                final String value = values[column];
                                Object ret = null;


                                if (fieldClass.isInstance(new Date())) {
                                    try {
                                        ret = new SimpleDateFormat(pattern).parse(value);
                                    } catch (Exception e) {
                                        ret = null;
                                    }
                                } else if (fieldClass.isInstance(LocalDateTime.now())) {
                                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
                                    ret = LocalDateTime.parse(value, formatter);
                                } else if (fieldClass.isInstance(LocalDate.now())) {
                                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
                                    ret = LocalDate.parse(value, formatter);
                                }

                                field.setAccessible(true);
                                field.set(object, ret);
                            }
                        }
                    }
                }
                objectList.add(object);
            }
        } catch (IOException | IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return objectList;
    }

    private Object transformObject(String value, String delimiter, Field field, Class<?> collectionClass) {
        final String[] valores = value.split(delimiter);

        if (collectionClass.isInstance(new String[]{})) {
            return valores;
        } else if (collectionClass.isInstance(new Integer[]{})) {
            return Arrays.stream(valores).map(Integer::valueOf).toArray(Integer[]::new);
        } else if (collectionClass.isInstance(new Long[]{})) {
            return Arrays.stream(valores).map(Long::valueOf).toArray(Long[]::new);
        } else if (collectionClass.isInstance(new Boolean[]{})) {
            return Arrays.stream(valores).map(Boolean::valueOf).toArray(Boolean[]::new);
        } else if (collectionClass.isInstance(new ArrayList<>())) {
            return Arrays.stream(valores).map(val -> this.transformValue(field, val)).collect(Collectors.toList());
        } else if (collectionClass.isInstance(new HashSet<>())) {
            return Arrays.stream(valores).map(val -> this.transformValue(field, val)).collect(Collectors.toSet());
        } else {
            return null;
        }
    }

    private Object transformValue(Field field, String value) {
        final ParameterizedType integerListType = (ParameterizedType) field.getGenericType();
        final Class<?> type = (Class<?>) integerListType.getActualTypeArguments()[0];

        if (type.isInstance("")) {
            return value;
        } else if (type.isInstance(Integer.valueOf("0"))) {
            return Integer.valueOf(value);
        } else if (type.isInstance(Long.valueOf("0"))) {
            return Long.valueOf(value);
        } else if (type.isInstance(Boolean.valueOf("0"))) {
            return Boolean.valueOf(value);
        }
        return null;
    }

    private Map<String, Integer> getHeaders(final String csvFilePath, final String delimiter) {
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(csvFilePath))) {
            final String line = bufferedReader.readLine();
            if (Objects.nonNull(line)) {
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

    private String getPathAnnotationValue() {
        return Arrays.stream(genericType.getAnnotations())
                .filter(annotation -> annotation instanceof Csv)
                .findFirst()
                .map(annotation -> ((Csv) annotation).path())
                .orElseThrow(InvalidPathException::new);
    }

    private String getDelimiter() {
        return Arrays.stream(genericType.getAnnotations())
                .filter(annotation -> annotation instanceof Csv)
                .findFirst()
                .map(annotation -> ((Csv) annotation).delimiter())
                .orElseThrow(InvalidDelimiterException::new);
    }

    private void validateClass() {
        if (Arrays.stream(genericType.getAnnotations())
                .noneMatch(annotation -> annotation instanceof Csv)) {
            throw new InvalidCsvClassException();
        }
    }

}