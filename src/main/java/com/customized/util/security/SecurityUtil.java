package com.customized.util.security;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * SecurityUtil
 *
 * @author liangpei
 * @desc 开启安全认证后，如sasl，ssl等，安全工具类，该工具类将对应的认证信息写入到系统变量中，共生产和消费使用
 */
public class SecurityUtil {

    public enum Module{
        STORM("StormClient"), KAFKA("KafkaClient"), ZOOKEEPER("Client");

        private String name;

        private Module(String name){
            this.name = name;
        }

        public String getName(){
            return name;
        }
    }

    private static final String LINE_SEPARATOR = System.getProperty("line.separator");
    private static final String JAAS_POSTFIX = ".jaas.conf";
    private static final boolean IS_IBM_JDK = System.getProperty("java.vendor").contains("IBM");
    private static final String IBM_LOGIN_MODULE = "com.ibm.security.auth.module.Krb5LoginModule required";
    private static final String SUN_LOGIN_MODULE = "com.sun.security.auth.module.Krb5LoginModule required";
    public static final String ZOOKEEPER_AUTH_PRINCIPAL = "zookeeper.server.principal";
    public static final String JAVA_SECURITY_KRB5_CONF = "java.security.krb5.conf";
    public static final String JAVA_SECURITY_LOGIN_CONF = "java.security.auth.login.config";

    public static void setJaasFile(String principal, String keytabPath, String tempFolder) throws IOException{
        String folder;
        if (null == tempFolder || tempFolder.length() == 0) {
            folder = System.getProperty("java.io.tmpdir");
        }else{
            folder = tempFolder;
        }
        String jaasPath = new File(tempFolder) + File.separator + System.getProperty("user.name") + JAAS_POSTFIX;
        jaasPath = jaasPath.replace("\\", "\\\\");
        deleteJassFile(jaasPath);
        writeJaasFile(jaasPath, principal, keytabPath);
        System.setProperty(JAVA_SECURITY_LOGIN_CONF, jaasPath);
    }

    public static void setZookeeperServerPrincipal(String zkServerPrincipal) throws IOException {
        System.setProperty(ZOOKEEPER_AUTH_PRINCIPAL, zkServerPrincipal);
        String principal = System.getProperty(ZOOKEEPER_AUTH_PRINCIPAL);
        if (null == principal) {
            throw new IOException(ZOOKEEPER_AUTH_PRINCIPAL + " is null.");
        }
        if (!principal.equals(zkServerPrincipal)) {
            throw new IOException(ZOOKEEPER_AUTH_PRINCIPAL + " is " + principal + " is not " + zkServerPrincipal + ".");
        }
    }

    public static void setKrb5Config(String krb5ConfFile) throws IOException{
        System.setProperty(JAVA_SECURITY_KRB5_CONF, krb5ConfFile);
        String file = System.getProperty(JAVA_SECURITY_KRB5_CONF);
        if (null == file) {
            throw new IOException(ZOOKEEPER_AUTH_PRINCIPAL + " is null.");
        }
        if (!file.equals(krb5ConfFile)) {
            throw new IOException(ZOOKEEPER_AUTH_PRINCIPAL + " is " + file + " is not " + krb5ConfFile + ".");
        }
    }

    private static void writeJaasFile(String jaasPath, String principal, String keytabPath) throws IOException{
        FileWriter writer = new FileWriter(new File(jaasPath));
        try {
            writer.write(getJaasConfContext(principal, keytabPath));
            writer.flush();
        }catch (IOException e){
            throw new IOException("Fail to create jass.conf file");
        }finally {
            writer.close();
        }
    }

    private static void deleteJassFile(String jaasPath) throws IOException{
        File jaasFile = new File(jaasPath);
        if (jaasFile.exists()) {
            if (!jaasFile.delete()) {
                throw new IOException("Fail to delete existed temp jaas file.");
            }
        }
    }

    private static String getJaasConfContext(String principal, String keytabPath){
        Module[] modules = Module.values();
        StringBuffer buffer = new StringBuffer();
        for (Module module : modules) {
            buffer.append(getModuleContext(principal, keytabPath, module));
        }
        return buffer.toString();
    }

    private static String getModuleContext(String userPrincipal, String keytabPaht, Module module){
        StringBuffer buffer = new StringBuffer();
        buffer.append(module.getName()).append(" {").append(LINE_SEPARATOR);
        if (IS_IBM_JDK) {
            buffer.append(IBM_LOGIN_MODULE).append(LINE_SEPARATOR);
            buffer.append("credsType=both").append(LINE_SEPARATOR);
            buffer.append("principal=\"").append(userPrincipal).append("\"").append(LINE_SEPARATOR);
            buffer.append("useKeytab=\"").append(keytabPaht).append("\"").append(LINE_SEPARATOR);
        }else {
            buffer.append(SUN_LOGIN_MODULE).append(LINE_SEPARATOR);
            buffer.append("useKeyTab=true").append(LINE_SEPARATOR);
            buffer.append("keyTab=\"").append(keytabPaht).append("\"").append(LINE_SEPARATOR);
            buffer.append("principal=\"").append(userPrincipal).append("\"").append(LINE_SEPARATOR);
            buffer.append("useTicketCache=false").append(LINE_SEPARATOR);
            buffer.append("storeKey=true").append(LINE_SEPARATOR);
        }
        buffer.append("debug=true;").append(LINE_SEPARATOR);
        buffer.append("};").append(LINE_SEPARATOR);

        return buffer.toString();
    }




















}


