package com.work.dto;

import com.work.annotation.ExcelField;
import com.work.annotation.ExcelTransForm;
import com.work.annotation.transform.DataTransForm;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * @Description
 * @Author ys
 * @Date 2023/3/1 13:57
 */
@Getter
@Setter
@Builder
@ExcelTransForm(use = DataTransForm.class)
public class OrderPayDTO {

    @ExcelField(columnName = "id",sort = 1)
    private Long id;

    @ExcelField(columnName = "编号",sort = 3)
    private String code;

    @ExcelField(columnName = "预支付id",sort = 2)
    private String prePayId;


}
