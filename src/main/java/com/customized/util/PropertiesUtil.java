package com.customized.util;

import java.io.IOException;
import java.util.Properties;

/**
 * PropertiesUtil
 *
 * @author liangpei
 * @desc 读取属性文件工具
 */
public class PropertiesUtil {

    public static Properties loadProperties(String file) {

        Properties properties = new Properties();
        try {
            properties.load(Thread.currentThread().getContextClassLoader()
                    .getResourceAsStream(file));
        } catch (IOException e) {
            throw new IllegalArgumentException(
                    "The consumer properties file is not loaded.", e);
        }
        return properties;
    }

}
