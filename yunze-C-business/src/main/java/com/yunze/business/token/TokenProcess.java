package com.yunze.business.token;

import org.springframework.stereotype.Component;
import java.util.Base64;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

@Component
public class TokenProcess {

    private TokenProcess() {
    };

    private static final TokenProcess instance = new TokenProcess();

    public static TokenProcess getInstance() {
        return instance;
    }

    /**
     * 生成Token
     * 
     * @return
     */
    public String makeToken() {
        String token = (System.currentTimeMillis() + new Random().nextInt(999999999)) + "";
        try {
            MessageDigest md = MessageDigest.getInstance("md5");
            byte md5[] = md.digest(token.getBytes());
            return Base64.getEncoder().encodeToString(md5);
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }
}
