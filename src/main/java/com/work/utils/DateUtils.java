package com.work.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @Description
 * @Author ys
 * @Date 2023/4/11 9:56
 */
public class DateUtils {


    public static String dateToStrByDateFormat(Date dateDate, String dateFormat) {
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
        return formatter.format(dateDate);
    }


}
