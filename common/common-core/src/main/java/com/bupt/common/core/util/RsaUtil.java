package com.bupt.common.core.util;

import cn.hutool.core.map.MapUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;
import lombok.extern.slf4j.Slf4j;

import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.util.Map;

/**
 * Java RSA 加密工具类。
 *
 * @author zzh
 * @date 2023-08-10
 */
@Slf4j
public class RsaUtil {

    /**
     * 密钥长度 于原文长度对应 以及越长速度越慢
     */
    private static final int KEY_SIZE = 1024;
    /**
     * 用于封装随机产生的公钥与私钥
     */
    private static final Map<Integer, String> KEY_MAP = MapUtil.newHashMap();

    /**
     * 随机生成密钥对。
     */
    public static void genKeyPair() throws NoSuchAlgorithmException {
        // KeyPairGenerator类用于生成公钥和私钥对，基于RSA算法生成对象
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
        // 初始化密钥对生成器
        keyPairGen.initialize(KEY_SIZE, new SecureRandom());
        // 生成一个密钥对，保存在keyPair中
        KeyPair keyPair = keyPairGen.generateKeyPair();
        // 得到私钥
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        // 得到公钥
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        String publicKeyString = Base64.getEncoder().encodeToString(publicKey.getEncoded());
        // 得到私钥字符串
        String privateKeyString = Base64.getEncoder().encodeToString(privateKey.getEncoded());
        // 将公钥和私钥保存到Map
        // 0表示公钥
        KEY_MAP.put(0, publicKeyString);
        // 1表示私钥
        KEY_MAP.put(1, privateKeyString);
    }

    /**
     * RSA公钥加密。
     *
     * @param str       加密字符串
     * @param publicKey 公钥
     * @return 密文
     */
    public static String encrypt(String str, String publicKey) {
        RSA rsa = new RSA(null, publicKey);
        return Base64.getEncoder().encodeToString(rsa.encrypt(str, KeyType.PublicKey));
    }

    /**
     * RSA私钥解密。
     *
     * @param str        加密字符串
     * @param privateKey 私钥
     * @return 明文
     */
    public static String decrypt(String str, String privateKey) {
        RSA rsa = new RSA(privateKey, null);
        // 64位解码加密后的字符串
        return new String(rsa.decrypt(Base64.getDecoder().decode(str), KeyType.PrivateKey));
    }

    public static void main(String[] args) throws Exception {
        long temp = System.currentTimeMillis();
        // 生成公钥和私钥
        genKeyPair();
        // 加密字符串
        log.info("公钥:" + KEY_MAP.get(0));
        log.info("私钥:" + KEY_MAP.get(1));
        log.info("生成密钥消耗时间:" + (System.currentTimeMillis() - temp) / 1000.0 + "秒");
        log.info("生成后的公钥前端使用!");
        log.info("生成后的私钥后台使用!");
        String message = "RSA测试ABCD~!@#$";
        log.info("原文:" + message);
        temp = System.currentTimeMillis();
        String messageEn = encrypt(message, KEY_MAP.get(0));
        log.info("密文:" + messageEn);
        log.info("加密消耗时间:" + (System.currentTimeMillis() - temp) / 1000.0 + "秒");
        temp = System.currentTimeMillis();
        String messageDe = decrypt(messageEn, KEY_MAP.get(1));
        log.info("解密:" + messageDe);
        log.info("解密消耗时间:" + (System.currentTimeMillis() - temp) / 1000.0 + "秒");
    }
}