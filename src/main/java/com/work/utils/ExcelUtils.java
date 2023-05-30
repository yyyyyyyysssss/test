package com.work.utils;

import com.work.annotation.ExcelField;
import com.work.annotation.ExcelTransForm;
import com.work.annotation.enums.AmountFormat;
import com.work.annotation.transform.ExcelExportSort;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.MediaType;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @description:
 * @author: Yuan
 * @time: 2022/7/13 21:22
 */
@Slf4j
public class ExcelUtils {

    public static <T> void exportExcel(HttpServletResponse response, List<T> dataList, Class<T> c, String fileName) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Row row;
            Cell cell;

            Sheet sheet = workbook.createSheet();
            // 设置列宽度大小
            sheet.setDefaultColumnWidth((short) 15);
            // 设置行高
            sheet.setDefaultRowHeight((short) (15 * 20));
            if (CollectionUtils.isEmpty(dataList)){
                response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
                response.setHeader("Content-Disposition",
                        "attachment;filename=" + URLEncoder.encode(fileName, String.valueOf(StandardCharsets.UTF_8)));
                response.flushBuffer();
                workbook.write(response.getOutputStream());
                return;
            }
            // 获取字段转换器
            ExcelTransForm excelTansForm = c.getAnnotation(ExcelTransForm.class);
            Class<?> tansFormClass = null;
            Object tansFormBean = null;
            if (excelTansForm != null) {
                tansFormClass = excelTansForm.use();
                tansFormBean = SpringUtils.getBean(tansFormClass);
            }

            int indexRow = 0;
            int startIndex = 0;
            // 创建表头
            row = sheet.createRow(indexRow++);
            Field[] fields = c.getDeclaredFields();
            List<ExcelExportSort> exportSorts = new ArrayList<>();
            Set<String> exportFieldNames=new HashSet<>();
            T first = dataList.get(0);
            for (Field field : fields) {
                field.setAccessible(true);
                ExcelField excelField = field.getAnnotation(ExcelField.class);
                if (excelField == null)
                    continue;

                ExcelExportSort excelExportSort = new ExcelExportSort();
                excelExportSort.setFieldName(field.getName());
                excelExportSort.setSort(excelField.sort());

                //根据条件忽略某个字段
                String[] conditionIgnore = excelField.conditionIgnore();
                if (conditionIgnore !=null && conditionIgnore.length > 0){
                    Field declaredField = first.getClass().getDeclaredField(conditionIgnore[0]);
                    declaredField.setAccessible(true);
                    Object o = declaredField.get(first);
                    String typeValue = o.toString();
                    Object b = transform(String.class, typeValue, conditionIgnore[1], tansFormClass, tansFormBean);
                    Boolean ignore=(Boolean) b;
                    if (ignore){
                        continue;
                    }
                }

                String columnName = null;
                String[] dynamicColumn = excelField.dynamicColumn();
                //列名为空的忽略
                if (dynamicColumn != null && dynamicColumn.length > 0) {
                    Field declaredField = first.getClass().getDeclaredField(dynamicColumn[0]);
                    declaredField.setAccessible(true);
                    Object o = declaredField.get(first);
                    String typeValue = o.toString();
                    columnName = (String) transform(String.class, typeValue, dynamicColumn[1], tansFormClass, tansFormBean);
                } else {
                    columnName = excelField.columnName();

                }
                if (StringUtils.isEmpty(columnName)) {
                    continue;
                }
                exportFieldNames.add(field.getName());
                excelExportSort.setColumnName(excelField.columnName());
                exportSorts.add(excelExportSort);

            }
            if (CollectionUtils.isEmpty(exportSorts)) {
                return;
            }
            //排序后的字段
            exportSorts.sort(Comparator.comparing(ExcelExportSort::getSort));
            for (ExcelExportSort excelExportSort : exportSorts) {
                cell = row.createCell(startIndex++);
                cell.setCellValue(excelExportSort.getColumnName());
            }

            // 设置内容数据
            for (T t : dataList) {
                row = sheet.createRow(indexRow++);
                Class<?> tClass = t.getClass();
                Field[] declaredFields = tClass.getDeclaredFields();
                Map<String, Field> mapFields = Arrays.stream(declaredFields).collect(Collectors.toMap(Field::getName, f -> f, (k1, k2) -> k1));
                int tmp = 0;
                for (ExcelExportSort excelExportSort : exportSorts) {
                    Field f = mapFields.get(excelExportSort.getFieldName());
                    boolean containsField = exportFieldNames.contains(f.getName());
                    if (!containsField){
                        continue;
                    }
                    f.setAccessible(true);
                    ExcelField annotation = f.getAnnotation(ExcelField.class);
                    if (annotation == null) {
                        continue;
                    }
                    cell = row.createCell(tmp++);
                    Object v = null;
                    try {
                        v = f.get(t);
                    } catch (IllegalAccessException e) {
                        log.error(e.getMessage());
                    }
                    setValue(cell, v, annotation, tansFormClass, tansFormBean);
                }
            }

            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            response.setHeader("Content-Disposition",
                    "attachment;filename=" + URLEncoder.encode(fileName, String.valueOf(StandardCharsets.UTF_8)));
            response.flushBuffer();
            workbook.write(response.getOutputStream());
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }

    }

    private static void setValue(Cell cell, Object value, ExcelField annotation, Class<?> tansFormClass,
                                 Object tansFormBean) {
        if (value == null) {
            cell.setCellValue("");
            return;
        }
        String qualifiedByName = annotation.qualifiedByName();
        Object transform = null;
        if (value instanceof String) {
            transform = transform(String.class, (String) value, qualifiedByName, tansFormClass, tansFormBean);

        } else if (value instanceof Integer) {
            transform = transform(Integer.class, (Integer) value, qualifiedByName, tansFormClass, tansFormBean);

        } else if (value instanceof Double) {
            transform = transform(Double.class, (Double) value, qualifiedByName, tansFormClass, tansFormBean);

        } else if (value instanceof Boolean) {
            transform = transform(Boolean.class, (Boolean) value, qualifiedByName, tansFormClass, tansFormBean);

        } else if (value instanceof Float) {
            transform = transform(Float.class, (Float) value, qualifiedByName, tansFormClass, tansFormBean);

        } else if (value instanceof Short) {
            transform = transform(Short.class, (Short) value, qualifiedByName, tansFormClass, tansFormBean);

        } else if (value instanceof Long) {
            transform = transform(Long.class, (Long) value, qualifiedByName, tansFormClass, tansFormBean);

        } else if (value instanceof Character) {
            transform = transform(Character.class, (Character) value, qualifiedByName, tansFormClass, tansFormBean);

        } else if (value instanceof Date) {
            transform = transform(Date.class, (Date) value, qualifiedByName, tansFormClass, tansFormBean);
        }

        setValue(cell, transform, annotation);
    }

    private static void setValue(Cell cell, Object value, ExcelField annotation) {
        if (value == null) {
            cell.setCellValue("");
            return;
        }
        AmountFormat amountFormat = annotation.amountFormat();
        if (value instanceof String) {
            String v=(String)value;
            if (StringUtils.isEmpty(v)){
                cell.setCellValue("");
                return;
            }
            if (amountFormat != AmountFormat.NOT_FORMAT){
                boolean b = NumberUtil.checkNumber(v);
                if (!b){
                    cell.setCellValue(v);
                    return;
                }
                BigDecimal bigDecimal = new BigDecimal(v);
                BigDecimal divide = bigDecimal.divide(new BigDecimal("100"), amountFormat.getScale(), amountFormat.getRoundingMode());
                cell.setCellValue(divide.toString());
            }else {
                cell.setCellValue(v);
            }

        } else if (value instanceof Integer) {
            int v=(Integer) value;
            if (amountFormat != AmountFormat.NOT_FORMAT){
                BigDecimal bigDecimal = new BigDecimal(v);
                BigDecimal divide = bigDecimal.divide(new BigDecimal("100"), amountFormat.getScale(), amountFormat.getRoundingMode());
                cell.setCellValue(divide.toString());
            }else {
                cell.setCellValue(v);
            }

        } else if (value instanceof Double) {
            cell.setCellValue((Double) value);

        } else if (value instanceof Boolean) {
            cell.setCellValue((Boolean) value);

        } else if (value instanceof Float) {
            cell.setCellValue((Float) value);

        } else if (value instanceof Short) {
            cell.setCellValue((Short) value);

        } else if (value instanceof Long) {
            long v=(Long) value;
            if (amountFormat != AmountFormat.NOT_FORMAT){
                BigDecimal bigDecimal = new BigDecimal(v);
                BigDecimal divide = bigDecimal.divide(new BigDecimal("100"), amountFormat.getScale(), amountFormat.getRoundingMode());
                cell.setCellValue(divide.toString());
            }else {
                cell.setCellValue(v);
            }

        } else if (value instanceof Character) {
            cell.setCellValue((Character) value);

        } else if (value instanceof Date) {
            cell.setCellValue(DateUtils.dateToStrByDateFormat((Date) value, annotation.dateFormat()));
        }
    }

    private static <T> Object transform(Class<T> t, T param, String methodName, Class<?> tansFormClass,
                                        Object tansFormBean) {
        if (StringUtils.isNotEmpty(methodName) && tansFormClass != null && tansFormBean != null) {
            try {
                Method method = tansFormClass.getMethod(methodName, t);
                return method.invoke(tansFormBean, param);
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }
        return param;
    }

}

