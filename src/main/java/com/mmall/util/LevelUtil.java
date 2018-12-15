package com.mmall.util;

import org.apache.commons.lang3.StringUtils;

/**
 * 数据库字段level 的计算
 */
public class LevelUtil {
    public final static String SEPARATOR = ".";

    public final static String ROOT = "0";

    /**
     * \
     * @param parentLevel 父类层级
     * @param parentId 父类Id
     * @return 当前level字符串
     */
    public static String calculateLevel(String parentLevel, int parentId) {
        if (StringUtils.isBlank(parentLevel)) {
            return ROOT;
        } else {
            //字符串的拼接
            return StringUtils.join(parentLevel, SEPARATOR, parentId);
        }
    }
}
