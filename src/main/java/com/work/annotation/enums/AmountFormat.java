package com.work.annotation.enums;

import lombok.Getter;

import java.math.RoundingMode;


/**
 * @Description
 * @Author ys
 * @Date 2023/4/11 19:14
 */
@Getter
public enum AmountFormat {

    TWO_DOWN(2,RoundingMode.DOWN),

    THREE_DOWN(3,RoundingMode.DOWN),

    NOT_FORMAT(-1,null);

    private int scale;
    private RoundingMode roundingMode;

    AmountFormat(int scale,RoundingMode roundingMode){
        this.scale=scale;
        this.roundingMode=roundingMode;
    }

}
