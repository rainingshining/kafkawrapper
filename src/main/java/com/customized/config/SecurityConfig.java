package com.customized.config;

import com.customized.util.PropertiesUtil;
import com.customized.util.security.SecurityUtil;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Properties;

/**
 * SecurityConfig
 *
 * @author liangpei
 * @desc 对接安全认证配置类
 */
public class SecurityConfig {

    private static Logger logger;

    private static final String COMMON_CONFIG_FILE = "common.properties";
    private static Properties properties;

    static {
        logger = Logger.getLogger(SecurityConfig.class);
        properties = PropertiesUtil.loadProperties(COMMON_CONFIG_FILE);
        try {
            initSecurityAuth();
            properties.setProperty("security.auth", "success");
        } catch (IOException e) {
            logger.error("fail to configure security auth infos, non-authentication mode is activated.", e);
            properties.setProperty("security.auth", "failure");
        }
    }

    public static String getProperty(String key){
        return properties.getProperty(key);
    }

    public static Properties getProperties(){
        return properties;
    }

    public static boolean isSecurityMode(){
        return Boolean.valueOf(properties.getProperty("security.mode.on"));
    }

    /**
     * 供创建生产与消费者实例前调用，若开启认证模式但操作失败，则使用备用地址
     * @param pro 待修改的properties实例
     */
    public static void resetBrokerAddress(Properties pro){
        boolean value = Boolean.valueOf(properties.getProperty("unsecure.mode.allow"));
        if (!value){
            return;
        }
        if (isSecurityMode() && !securityAuthStatus()){
            pro.setProperty("bootstrap.servers", properties.getProperty("backup.bootstrap.servers"));
        }
    }

    /**
     * 查询安全认证开启状态，true：开启成功， false： 开启失败
     * @return
     */
    public static boolean securityAuthStatus(){
        String property = properties.getProperty("security.auth");
        return (null ==  property || property.length() == 0) ? false
                : (property.equalsIgnoreCase("success") ? true
                : false);
    }

    private static void initSecurityAuth() throws IOException {
        boolean securityMode = Boolean.valueOf(properties.getProperty("security.mode.on"));
        if (!securityMode){
            return;
        }

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        String conf = classLoader.getResource(properties.getProperty("security.krb5.conf.file")).getPath();
        String keytab = classLoader.getResource(properties.getProperty("security.user.keytab.file")).getPath();

        SecurityUtil.setKrb5Config(properties.getProperty("security.krb5.conf.file"));
        SecurityUtil.setZookeeperServerPrincipal(properties.getProperty("security.zookeeper.server.principal"));
        SecurityUtil.setJaasFile(
                properties.getProperty("security.user.principal"),
                properties.getProperty("security.user.keytab.file"),
                properties.getProperty("security.jaas.tmp.folder")
        );
    }
}
