package com.work.utils;

import com.alibaba.fastjson.JSONObject;
import com.work.dto.OrderPayDTO;
import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.util.Base64Utils;

import javax.crypto.Cipher;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Map;
import java.util.TreeMap;

/**
 * @Description RSA java默认的填充模式为 RSA/None/PKCS1Padding 最大加密大小需要减去11字节
 * @Author ys
 * @Date 2023/3/1 10:49
 */
public class RSAUtil {

    private static final int KEY_SIZE = 2048;

    public static final String KEY_RSA_TYPE = "RSA";

    private static final String ALGORITHM="RSA/ECB/OAEPWithMD5AndMGF1Padding";

    /**
     * RSA最大加密明文大小 KEY_SIZE/8 - 11  转为字节减去11
     */
    private static final int MAX_ENCRYPT_BLOCK = (KEY_SIZE >> 3) - 11;

    /**
     * RSA最大解密密文大小  KEY_SIZE/8  转为字节
     */
    private static final int MAX_DECRYPT_BLOCK = KEY_SIZE >> 3;
    /**
     * 编码
     */
    private static String charset = "utf-8";

    /**
     * 获取密钥对
     *
     * @return 密钥对
     */
    public static KeyPair getKeyPair() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance(ALGORITHM);
        generator.initialize(KEY_SIZE);
        return generator.generateKeyPair();
    }

    /**
     * 解码Base64获取私钥
     *
     * @param privateKey 私钥字符串
     * @return
     */
    public static PrivateKey getPrivateKey(String privateKey) throws Exception {
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_RSA_TYPE);
        byte[] decodedKey = Base64.decodeBase64(privateKey.getBytes(charset));
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decodedKey);
        return keyFactory.generatePrivate(keySpec);
    }

    /**
     * 解码Base64获取公钥
     *
     * @param publicKey 公钥字符串
     * @return
     */
    public static PublicKey getPublicKey(String publicKey) throws Exception {
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_RSA_TYPE);
        byte[] decodedKey = Base64.decodeBase64(publicKey.getBytes(charset));
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decodedKey);
        return keyFactory.generatePublic(keySpec);
    }

    /**
     * RSA加密
     *
     * @param data      待加密数据
     * @param publicKey 公钥
     * @return
     */
    public static String encrypt(String data, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        int inputLen = data.getBytes(charset).length;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int offset = 0;
        byte[] cache;
        int i = 0;
        // 对数据分段加密
        while (inputLen - offset > 0) {
            if (inputLen - offset > MAX_ENCRYPT_BLOCK) {
                cache = cipher.doFinal(data.getBytes(charset), offset, MAX_ENCRYPT_BLOCK);
            } else {
                cache = cipher.doFinal(data.getBytes(charset), offset, inputLen - offset);
            }
            out.write(cache, 0, cache.length);
            i++;
            offset = i * MAX_ENCRYPT_BLOCK;
        }
        byte[] encryptedData = out.toByteArray();
        out.close();
        // 获取加密内容使用base64进行编码,并以UTF-8为标准转化成字符串
        // 加密后的字符串
        return Base64.encodeBase64String(encryptedData);
    }

    /**
     * RSA解密
     *
     * @param data       待解密数据
     * @param privateKey 私钥
     * @return
     */
    public static String decrypt(String data, PrivateKey privateKey) throws Exception {
        byte[] dataBytes = Base64.decodeBase64(data);
        return decrypt(dataBytes,privateKey);
    }

    public static String decrypt(byte[] dataBytes, PrivateKey privateKey) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        int inputLen = dataBytes.length;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int offset = 0;
        byte[] cache;
        int i = 0;
        // 对数据分段解密
        while (inputLen - offset > 0) {
            if (inputLen - offset > MAX_DECRYPT_BLOCK) {
                cache = cipher.doFinal(dataBytes, offset, MAX_DECRYPT_BLOCK);
            } else {
                cache = cipher.doFinal(dataBytes, offset, inputLen - offset);
            }
            out.write(cache, 0, cache.length);
            i++;
            offset = i * MAX_DECRYPT_BLOCK;
        }
        byte[] decryptedData = out.toByteArray();
        out.close();
        // 解密后的内容
        return new String(decryptedData, StandardCharsets.UTF_8);
    }

    /**
     * 签名
     *
     * @param data       待签名数据
     * @param privateKey 私钥
     * @return 签名
     */
    public static String sign(String data, PrivateKey privateKey) throws Exception {
        byte[] keyBytes = privateKey.getEncoded();
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_RSA_TYPE);
        PrivateKey key = keyFactory.generatePrivate(keySpec);
        Signature signature = Signature.getInstance("MD5withRSA");
        signature.initSign(key);
        signature.update(data.getBytes(charset));
        return new String(Base64.encodeBase64(signature.sign()), charset);
    }

    /**
     * 验签
     *
     * @param srcData   原始字符串
     * @param publicKey 公钥
     * @param sign      签名
     * @return 是否验签通过
     */
    public static boolean verify(String srcData, PublicKey publicKey, String sign) throws Exception {
        byte[] keyBytes = publicKey.getEncoded();
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_RSA_TYPE);
        PublicKey key = keyFactory.generatePublic(keySpec);
        Signature signature = Signature.getInstance("MD5withRSA");
        signature.initVerify(key);
        signature.update(srcData.getBytes(charset));
        return signature.verify(Base64.decodeBase64(sign.getBytes(charset)));
    }

    //将key按字典升序排序 （只排序 字符串、数字）
    public static String dictSortASC(Map<String, Object> map) throws Exception {
        Map<String, Object> treeMap = new TreeMap<>(map);
        StringBuilder signStr = new StringBuilder();
        treeMap.forEach((k, v) -> {

            if (v instanceof String) {
                if (StringUtils.isNotEmpty((String) v)) {
                    signStr.append("|").append((String) v);
                }
            }

            if (v instanceof Long) {
                signStr.append("|").append(v);
            }

            if (v instanceof Integer) {
                signStr.append("|").append(v);
            }

            if (v instanceof Float) {
                signStr.append("|").append(v);
            }

            if (v instanceof Double) {
                signStr.append("|").append(v);
            }

        });

        return signStr.toString().replaceFirst("\\|", "");
    }

    public static void main(String[] args) throws Exception {
        KeyPair keyPair = getKeyPair();
        String publicKey = new String(Base64.encodeBase64(keyPair.getPublic().getEncoded()));
        String privateKey = new String(Base64.encodeBase64(keyPair.getPrivate().getEncoded()));
        System.out.println("公钥：" + publicKey);
        System.out.println("私钥：" + privateKey);
//        String publicKeyStr = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA3ycmK7vyazlJiVMNGa2CicZLbLkgSUAdVZWAQXpRAesM0WPCS7NflNnJn9ZbItV7JxOSk/+rPfhXqb3UYPEg0pED1iLzLHIFsd7fWqYX2cORSFvpZwGFFNbwyFF8ttnQ4mOz/gnEApCWgEwvSiIGIMvQhFF1k+IE8bICLn9FddUNfYP59E/VdRZ0DpXvW13Mb4EMpIsfOo68kxDV6vXk0elCN9H6A4wrMp166bQLhuvPXsi3snBgbvEGQ2nQ8gHq5Sz0RyGEVGfOybMiio2rq1mGaUPyoDTk1+dLY+paQNypkDVLgUrVZJ5812Q4D1Zfykz2NGlT0JRoauHz9pIBTQIDAQAB";
//        String privateKeyStr = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDfJyYru/JrOUmJUw0ZrYKJxktsuSBJQB1VlYBBelEB6wzRY8JLs1+U2cmf1lsi1XsnE5KT/6s9+FepvdRg8SDSkQPWIvMscgWx3t9aphfZw5FIW+lnAYUU1vDIUXy22dDiY7P+CcQCkJaATC9KIgYgy9CEUXWT4gTxsgIuf0V11Q19g/n0T9V1FnQOle9bXcxvgQykix86jryTENXq9eTR6UI30foDjCsynXrptAuG689eyLeycGBu8QZDadDyAerlLPRHIYRUZ87JsyKKjaurWYZpQ/KgNOTX50tj6lpA3KmQNUuBStVknnzXZDgPVl/KTPY0aVPQlGhq4fP2kgFNAgMBAAECggEAJ3v52QssXaSJSm8ThlOZIV+gwOAQwmVBy37kbrGB2+EHJ5WLL3HRm7jdYBbBkWxMmiFwchdu6Uwj1rjr9yszL47RKvaMKHUUZOCWjKeO2M0ty6vXpI40SfeuKwOgGYp411Xk5zI7KGkE+DaCt49Fnk/x2TfAoiX/0fQ5RpjmZW60bl5BYCcYQvLx4UgSkOn59eke6n/C2Ioz8JTWxCY8U15YM6XyeE9Ls+24Pcij8i0AS3nA+OnLEAZSeXn0+Q05KUDkX0Pd0zr4/5DfcnR7qMGU3BI9xm25vWAzy7C+ZjuqMstIsTPtwIH6uxmpPSdHsqNXUIOYRJ3LdIJDd6vQgQKBgQD+6bXjZ9PhYXv36iZ9SGiv+njhmnGaop5EkcpGLA7dmnPjIpCXhzijygOSf9OmoI5ASiy6lhHu4BZ6ast7O9rpKlzA7ArWWAuoqWsaT8H6wgo4QUeVLveHMgF5GvZonqmAwb3gCK8eeG8pBtsYiMBBnVp2T7ghtEh3b0kAWp3HtQKBgQDgGsQdeMgQd1GdKfSdaSHoRUdJ9H5sIELZFJj3OAlebYgDdyPBXXfpLkmiOJUH2eSzil1eoWKuzW6rSaQb5I2R4RfH+Mxj1BMdU+Y15hyIcJFSn5KIhWJzZCesDPsjjJuHJtQQgT+iVmxdJrbFIFD0inwgQsMxZkB1vqDPttCiOQKBgQDEYNe28O3R23/dZY1xRq8fox4s7wZF0rGKsaXamWZ6YdqVSLOLiKihxacq3T74RwhPjtUWiTYXiJrIhC1uKpZiujfqh+1xYzLYJGxWQMt3mrS00dWLoDsg8+0elx7tSg3bG2ZjiU0fdDhuqtFu/SrfYZGnQ5LepDyPhjP/CdAzYQKBgQDE4p8TqW5TERDxu899j8TLVDoaXiPHiNxR7UeqQl/wxtfs+3m9Qwm7IgBs9brwx9ymY9Q99BlHq5IkCDGWk8hQtmn3NL0Uo1gbtv8UsVK2B/eUmD7blBv3drFn9YuaIsjtBw7eZT7rt3PCruV58DdPcG82xJDKUKQnDdAoTNuk+QKBgBfYpw3mVqNTN6NSKXPoSBSL/QLOffg/XHTbgjZS+eDDpIbjAszSSm1E7cm4mpvbqmtE8js8SKVnV3jcx9lCjkYZD1WoEpwdv8xXB+hUNwS3sJbox2yDCmKhaeNAZx1EbaPP27VHc2sWGf4kqZJRYPtw+8tWy7xBFB4ppO0r3TjU";
//        OrderPayDTO orderPayDTO = OrderPayDTO
//                .builder()
//                .id(9876871212L).code("2016776492553692732")
//                .prePayId("2303011340P000003003")
//                .build();
//        String orderPayData = JSONObject.toJSONString(orderPayDTO);
//        //RSA数据加解密  防止数据泄露
//        PublicKey publicKey = getPublicKey(publicKeyStr);
//        PrivateKey privateKey = getPrivateKey(privateKeyStr);
//        //公钥加密
//        String encryptData = RSAUtil.encrypt(orderPayData, publicKey);
//        System.out.println("加密后：" + encryptData);
//        //私钥解密
//        String decryptData = RSAUtil.decrypt(encryptData, privateKey);
//        System.out.println("解密后：" + decryptData);
//
////        RSA数据签名 防止数据篡改
//        JSONObject jsonObject = JSONObject.parseObject(new String(orderPayData.getBytes(), StandardCharsets.UTF_8));
//        //排序后的字符串
//        String sortASCStr = dictSortASC(jsonObject);
//        //私钥加签
//        String sign = sign(sortASCStr, privateKey);
//        System.out.println("签名："+sign);
//        //公钥验签
//        boolean verify = verify(sortASCStr, publicKey, sign);
//        System.out.println("验签结果："+verify);

    }


}
