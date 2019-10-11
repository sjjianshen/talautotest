package com.tal.autotest.example;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RoundUtils {
    public static double round(double value) {
        return new BigDecimal(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    /**
     * 是否为英文
     * @param charaString
     * @return
     */
    public static boolean isEnglish(String charaString){
        return charaString.matches("^[a-zA-Z]*");
    }

    /**
     * 是否为中文
     * @param str
     * @return
     */
    public static boolean isChinese(String str){

        String regEx = "[\\u4e00-\\u9fa5]+";

        Pattern p = Pattern.compile(regEx);

        Matcher m = p.matcher(str);

        if(m.find()){
            return true;
        }else{
            return false;
        }
    }
}
