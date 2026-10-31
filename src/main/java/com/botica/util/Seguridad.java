package com.botica.util;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

public final class Seguridad {
    private static final int ITERACIONES = 65536;
    private static final int BITS_CLAVE = 256;
    private static final SecureRandom ALEATORIO = new SecureRandom();

    private Seguridad() {
    }

    public static String hashear(String contrasena) {
        byte[] sal = new byte[16];
        ALEATORIO.nextBytes(sal);
        byte[] hash = pbkdf2(contrasena.toCharArray(), sal);
        return Base64.getEncoder().encodeToString(sal) + ":" + Base64.getEncoder().encodeToString(hash);
    }

    public static boolean verificar(String contrasena, String guardada) {
        if (guardada == null || !guardada.contains(":")) {
            return false;
        }
        String[] partes = guardada.split(":", 2);
        byte[] sal = Base64.getDecoder().decode(partes[0]);
        byte[] esperado = Base64.getDecoder().decode(partes[1]);
        return MessageDigest.isEqual(esperado, pbkdf2(contrasena.toCharArray(), sal));
    }

    private static byte[] pbkdf2(char[] contrasena, byte[] sal) {
        try {
            PBEKeySpec spec = new PBEKeySpec(contrasena, sal, ITERACIONES, BITS_CLAVE);
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("No se pudo calcular el hash", e);
        }
    }
}
