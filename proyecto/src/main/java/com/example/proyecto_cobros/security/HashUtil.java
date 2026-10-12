package com.example.proyecto_cobros.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class HashUtil {

    // =========================================
    // GENERAR HASH SHA-256
    // =========================================

    public static String sha256(String texto) {

        try {

            MessageDigest md =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash =
                    md.digest(
                            texto.getBytes(
                                    StandardCharsets.UTF_8
                            )
                    );

            StringBuilder hex =
                    new StringBuilder();

            for(byte b : hash){

                String h =
                        Integer.toHexString(
                                0xff & b
                        );

                if(h.length() == 1){
                    hex.append('0');
                }

                hex.append(h);
            }

            return hex.toString();

        }catch(Exception e){

            throw new RuntimeException(
                    "Error generando hash SHA-256",
                    e
            );
        }
    }
}