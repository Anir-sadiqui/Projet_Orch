package com.membership.users.security;


import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.List;

@Service
public class JwtService {

    private PrivateKey privateKey;


    private static final String PRIVATE_KEY_PATH = "src/main/resources/keys/private_key.pem";

    @PostConstruct
    public void init() throws Exception {
        byte[] keyBytes = Files.readAllBytes(Paths.get(PRIVATE_KEY_PATH));
        String privateKeyPEM = new String(keyBytes)
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");

        byte[] decoded = Base64.getDecoder().decode(privateKeyPEM);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decoded);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        privateKey = kf.generatePrivate(keySpec);
    }

    /**
     * Génère un JWT signé RS256
     *
     * @param userId  identifiant de l'utilisateur
     * @param email   email de l'utilisateur
     * @param roles   liste des rôles
     * @return token JWT
     */
    public String generateToken(Long userId, String email, List<String> roles) {
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(3600); // 1 heure

        return Jwts.builder()
                .claim("userId", userId)
                .claim("email", email)
                .claim("roles", roles)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiry))
                .signWith(privateKey, SignatureAlgorithm.RS256)
                .compact();
    }
}
