package com.anan.springboot.core.util;

import com.anan.springboot.core.enums.CodeEnum;

/**
 * @author anan
 * Created by anan on 2018/8/8.
 */
public class EnumUtil {

    public static <T extends CodeEnum> T getByCode(Integer code, Class<T> enumClass) {
        for (T each: enumClass.getEnumConstants()) {
            if (code.equals(each.getCode())) {
                return each;
            }
        }
        return null;
    }
}