package com.tms.spring.hashing;

import java.io.*;
import org.apache.commons.codec.digest.DigestUtils;

public class HashingMachine {
    String input;

    public HashingMachine() {}

    public HashingMachine(String input) {
        this.input = input;
    }

    public String hashingSha1() {
        String userToken = DigestUtils.sha1Hex(this.input);
        return userToken;
    }

    public String hashingSha1(String data) {
        String userToken = DigestUtils.sha1Hex(data);
        return userToken;
    }

    public String hashingSha3() {
        String hashedPassword = DigestUtils.sha3_256Hex(this.input);
        return hashedPassword;
    }

    public String createAuthToken(String email, String password) {
        String hashedToken = hashingSha1(email + "ujedxgtv5frjoegd4rt@#%&^#^(0agt5r4" + password);
        return hashedToken;
    }
}
