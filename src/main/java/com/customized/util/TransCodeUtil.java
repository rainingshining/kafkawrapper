package com.customized.util;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * 字符串加解密工具类
 * @author liangpei
 * @desc
 */
public class TransCodeUtil {

    private static Logger logger = Logger.getLogger(TransCodeUtil.class);

    private static final BASE64Encoder BASE_64_ENCODER = new BASE64Encoder();
    private static final BASE64Decoder BASE_64_DECODER = new BASE64Decoder();

    private static final String SECURITY_KEY = "IRCP-ENC-DEC-KEY";

    private static final String AES_CBC_KEY = "9230967890982316";

    /**
     * encrypt source with base64 algorithm
     * @param source 明文字符串
     * @return 密文字符串
     */
    public static String encWithBase64(String source){
        return BASE_64_ENCODER.encode(source.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * decrypted unreadable string into readable string with base64 algorithm
     * @param encryptedStr 密文字符串
     * @return 明文字符串
     */
    public static String decWithBase64(String encryptedStr){
        try {
            return new String(BASE_64_DECODER.decodeBuffer(encryptedStr), "UTF-8");
        } catch (IOException e) {
            logger.error("Error transfer the encrypted source to readable string", e);
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 自定义加密方式
     * @param data
     * @return
     * @throws UnsupportedEncodingException
     */
    public static String encode(String data) throws UnsupportedEncodingException {
        //把字符串转为字节数组
        byte[] b = data.getBytes("UTF-8");
        //遍历
        for(int i=0;i<b.length;i++) {
            b[i] += 1;//在原有的基础上+1
        }
        return new String(b, "UTF-8");
    }

    /**
     * 自定义对应的解密方式
     * @param data
     * @return
     * @throws UnsupportedEncodingException
     */
    public static String decode(String data) throws UnsupportedEncodingException {
        //把字符串转为字节数组
        byte[] b = data.getBytes("UTF-8");
        //遍历
        for(int i=0;i<b.length;i++) {
            b[i] -= 1;//在原有的基础上-1
        }
        return new String(b, "UTF-8");
    }

    /**
     * 使用AES CBC模式加密，password内置，不允许设置
     * @param content
     * @return
     */
    public static String encryptWithAESCBCMode(String content){
        return AESCBC.encrypt(content, AESCBC.byte2hex(AES_CBC_KEY.getBytes()));
    }

    /**
     * 使用AES CBC模式解密，password内齿，不允许设置
     * @param content
     * @return
     */
    public static String decryptWithAESCBCMode(String content){
        return AESCBC.decrypt(content, AESCBC.byte2hex(AES_CBC_KEY.getBytes()));
    }

    /**
     * 使用AES ECB模式加密， password内置，不允许设置
     * @param content
     * @return
     */
    public static String encryptWithAESECBMode(String content){
        return AESECB.encrypt(content, SECURITY_KEY);
    }

    /**
     * 使用AES ECB模式解密， password内置，不允许设置
     * @param content
     * @return
     */
    public static String decryptWithAESECBMode(String content){
        return AESECB.decrypt(content, SECURITY_KEY);
    }

    /**
     * AES CBC
     */
    private static class AESCBC{

        private static final String ENCODING = "UTF-8";

        private static final String KEY_ALGORITHM = "AES";
        /**
         * 加解密算法/工作模式/填充方式
         */
        private static final String DEFAULT_CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding";
        /**
         * 填充向量
         */
        private static final String FILL_VECTOR = "1234560405060708";

        /**
         * 加密字符串
         *
         * @param content  字符串
         * @param password 密钥KEY
         * @return
         * @throws Exception
         */
        public static String encrypt(String content, String password) {
            if (StringUtils.isAnyEmpty(content, password)) {
                logger.error("AES encryption params is null");
                return null;
            }

            byte[] raw = hex2byte(password);
            SecretKeySpec skeySpec = new SecretKeySpec(raw, KEY_ALGORITHM);
            Cipher cipher = null;
            try {
                cipher = Cipher.getInstance(DEFAULT_CIPHER_ALGORITHM);
                IvParameterSpec iv = new IvParameterSpec(FILL_VECTOR.getBytes());
                cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
                byte[] anslBytes = content.getBytes(ENCODING);
                byte[] encrypted = cipher.doFinal(anslBytes);
                return byte2hex(encrypted).toUpperCase();
            } catch (Exception e) {
                logger.error("AES encryption operation has exception,content: " + content+ ",password: " + password, e);
            }
            return null;
        }

        /**
         * 解密
         *
         * @param content  解密前的字符串
         * @param password 解密KEY
         * @return
         * @throws Exception
         * @author cdduqiang
         * @date 2014年4月3日
         */
        public static String decrypt(String content, String password) {
            if (StringUtils.isAnyEmpty(content, password)) {
                logger.error("AES decryption params is null");
                return null;
            }

            try {
                byte[] raw = hex2byte(password);
                SecretKeySpec skeySpec = new SecretKeySpec(raw, KEY_ALGORITHM);
                Cipher cipher = Cipher.getInstance(DEFAULT_CIPHER_ALGORITHM);
                IvParameterSpec iv = new IvParameterSpec(FILL_VECTOR.getBytes());
                cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
                byte[] encrypted1 = hex2byte(content);
                byte[] original = cipher.doFinal(encrypted1);
                return new String(original, ENCODING);
            } catch (Exception e) {
                logger.error("AES decryption operation has exception,content:" +  content + ",password: " + password, e);
            }
            return null;
        }

        public static byte[] hex2byte(String strhex) {
            if (strhex == null) {
                return null;
            }
            int l = strhex.length();
            if (l % 2 == 1) {
                return null;
            }
            byte[] b = new byte[l / 2];
            for (int i = 0; i != l / 2; i++) {
                b[i] = (byte) Integer.parseInt(strhex.substring(i * 2, i * 2 + 2), 16);
            }
            return b;
        }

        public static String byte2hex(byte[] b) {
            String hs = "";
            String stmp = "";
            for (int n = 0; n < b.length; n++) {
                stmp = (java.lang.Integer.toHexString(b[n] & 0XFF));
                if (stmp.length() == 1) {
                    hs = hs + "0" + stmp;
                } else {
                    hs = hs + stmp;
                }
            }
            return hs.toUpperCase();
        }
    }

    /**
     * AES ECB
     */
    private static class AESECB{

        private static final String KEY_ALGORITHM = "AES";
        private static final String CHAR_SET = "UTF-8";
        /**
         * AES的密钥长度
         */
        private static final Integer SECRET_KEY_LENGTH = 128;
        /**
         * 加解密算法/工作模式/填充方式
         */
        private static final String DEFAULT_CIPHER_ALGORITHM = "AES/ECB/PKCS5Padding";

        /**
         * AES加密操作
         *
         * @param content  待加密内容
         * @param password 加密密码
         * @return 返回Base64转码后的加密数据
         */
        public static String encrypt(String content, String password) {
            if (StringUtils.isAnyEmpty(content, password)) {
                logger.error("AES encryption params is null");
                return null;
            }
            try {
                //创建密码器
                Cipher cipher = Cipher.getInstance(DEFAULT_CIPHER_ALGORITHM);
                byte[] byteContent = content.getBytes(CHAR_SET);
                //初始化为加密密码器
                cipher.init(Cipher.ENCRYPT_MODE, getSecretKey(password));
                byte[] encryptByte = cipher.doFinal(byteContent);
                return Base64.encodeBase64String(encryptByte);
            } catch (Exception e) {
                logger.error("AES encryption operation has exception,content:"+ content +",password: " + password, e);
            }
            return null;
        }

        /**
         * AES解密操作
         *
         * @param encryptContent 加密的密文
         * @param password       解密的密钥
         * @return
         */
        public static String decrypt(String encryptContent, String password) {
            if (StringUtils.isAnyEmpty(encryptContent, password)) {
                logger.error("AES decryption params is null");
                return null;
            }
            Cipher cipher = null;
            try {
                cipher = Cipher.getInstance(DEFAULT_CIPHER_ALGORITHM);
                //设置为解密模式
                cipher.init(Cipher.DECRYPT_MODE, getSecretKey(password));
                //执行解密操作
                byte[] result = cipher.doFinal(Base64.decodeBase64(encryptContent));
                return new String(result, CHAR_SET);
            } catch (Exception e) {
                logger.error("AES decryption operation has exception,content:"+ encryptContent +",password: " + password, e);
            }
            return null;
        }

        private static SecretKeySpec getSecretKey(final String password) throws NoSuchAlgorithmException {
            //生成指定算法密钥的生成器
            KeyGenerator keyGenerator = KeyGenerator.getInstance(KEY_ALGORITHM);
            keyGenerator.init(SECRET_KEY_LENGTH, new SecureRandom(password.getBytes()));
            //生成密钥
            SecretKey secretKey = keyGenerator.generateKey();
            //转换成AES的密钥
            return new SecretKeySpec(secretKey.getEncoded(), KEY_ALGORITHM);
        }
    }

//    public static void main(String[] args) throws UnsupportedEncodingException {
//        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><Application name=\"rob\">I have a name<Application/>";
//        String content  = xml + xml + xml + xml + xml;
//        System.out.println("Before: " + content);
//        String encWithBase64 = encWithBase64(content);
//        System.out.println("ENC: " + encWithBase64);
//        System.out.println("DEC: " + decWithBase64(encWithBase64));
//
//        String cus = encode(content);
//        System.out.println(cus);
//        System.out.println(decode(cus));
//
//        String encrypt = encryptWithAESCBCSMode(content);
//        System.out.println(encrypt);
//        System.out.println(decryptWithAESCBCMode(encrypt));
//
//        String ecb = encryptWithAESECBMode(content);
//        System.out.println(ecb);
//        System.out.println(decryptWithAESECBMode(ecb));
//    }
}
