package com.work.annotation.transform;

import lombok.Getter;
import lombok.Setter;

/**
 * @Description
 * @Author ys
 * @Date 2023/4/11 10:37
 */
@Getter
@Setter
public class ExcelExportSort {

    private String fieldName;

    private String columnName;

    private Integer sort;

}
