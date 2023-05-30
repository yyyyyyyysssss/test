package com.work.utils;


import java.util.regex.Pattern;

public class NumberUtil {

    //检查是否为整数
    public static boolean checkNumber(String num){
        Pattern pattern=Pattern.compile("^(-|[1-9])\\d*?");
        return pattern.matcher(num).matches();
    }

}
