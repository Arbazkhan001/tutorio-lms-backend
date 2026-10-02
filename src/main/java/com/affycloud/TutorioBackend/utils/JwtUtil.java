package com.affycloud.TutorioBackend.utils;

import com.affycloud.TutorioBackend.dtos.student.StudentDetailDto;
import com.affycloud.TutorioBackend.dtos.superAdmin.SuperAdminDetailDto;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;


@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String base64Secret;

    @Value("${jwt.accessToken.expiration}")
    private Long accessTokenExpiration;

    @Value("${jwt.refreshToken.expiration}")
    private Long refreshTokenExpiration;

    @Value("${jwt.expiration}")
    private Long TokenExpiration;

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(base64Secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // New JJWT 0.12+ parser
    private JwtParser jwtParser() {
        return Jwts.parser().verifyWith(getSigningKey()).build();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = jwtParser().parseSignedClaims(token).getPayload();
        return claimsResolver.apply(claims);
    }

    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    private String buildToken(String email, long expiration) {
        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    // Generate access token with student details
    public String generateAccessTokenForAdmin(String email, SuperAdminDetailDto adminDetailDto) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("claims", adminDetailDto);
        return buildTokenWithClaims(email, claims, accessTokenExpiration);
    }


    // Generate refresh token
    public String generateRefreshTokenForAdmin(String email) {
        return buildTokens(email, refreshTokenExpiration);
    }

    private String buildTokens(String email, long expiration) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(SignatureAlgorithm.HS256, base64Secret)
                .compact();
    }
    // Generate refresh token
    public String generateRefreshTokenForStudent(UUID tenantId,String email) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("tenantId", tenantId); // add tenantId
        return buildTokenWithClaims(email, claims, refreshTokenExpiration);
    }

    public String generateAccessTokenForStudent(UUID tenantId,String email, StudentDetailDto studentDetailDto) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("claims", studentDetailDto);
        claims.put("tenantId", tenantId); // add tenantId
        return buildTokenWithClaims(email, claims, accessTokenExpiration);
    }

    public String generateTokenForSuperAdmin(String email, SuperAdminDetailDto superAdminDetail) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("claims", superAdminDetail);
        return buildTokenWithClaims(email, claims, accessTokenExpiration);
    }

    private String buildTokenWithClaims(String email, Map<String, Object> claims, long expiration) {
        return Jwts.builder()
                .claims(claims)
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    public String extractEmail(String token) {
        return extractUsername(token);
    }
    public String extractTenantId(String token) {
        return extractClaim(token, claims -> claims.get("tenantId", String.class));
    }

}


//
//@Component
//public class JwtUtil {
//    @Value("${jwt.secret}")
//    private String secret;
//
//    @Value("${jwt.secret}")
//    private String base64Secret;
//
//    @Value("${jwt.expiration}")
//    private Long expiration;
//    @Value("${jwt.accessToken.expiration}")
//    private Long accessTokenExpiration;
//
//    @Value("${jwt.refreshToken.expiration}")
//    private Long refreshTokenExpiration;
//
//    private SecretKey getSigningKey() {
//        byte[] keyBytes = Decoders.BASE64.decode(base64Secret);
//        return Keys.hmacShaKeyFor(keyBytes);
//    }
//
//    // New JJWT 0.12+ parser
//    private JwtParser jwtParser() {
//        return Jwts.parser().verifyWith(getSigningKey()).build();
//    }
//
//    public String generateToken(String email) {
//        return Jwts.builder()
//                .setSubject(email)
//                .setIssuedAt(new Date())
//                .setExpiration(new Date(System.currentTimeMillis() + expiration))
//                .signWith(SignatureAlgorithm.HS256, secret)
//                .compact();
//    }
//
//    public String extractUsername(String token) {
//        return extractClaim(token, Claims::getSubject);
//    }
//
//
//    private Claims extractAllClaims(String token) {
//        return Jwts.parserBuilder().setSigningKey(secret).build().parseClaimsJws(token).getBody();
//    }
//
//
//    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
//        final Claims claims = extractAllClaims(token);
//        return claimsResolver.apply(claims);
//    }
//
//
//    public Boolean isTokenExpired(String token) {
//        return extractExpiration(token).before(new Date());
//    }
//
//    public Date extractExpiration(String token) {
//        return extractClaim(token, Claims::getExpiration);
//    }
//
//    public String extractPhoneNumber(String token) {
//        return extractClaim(token, Claims::getSubject);
//    }
//    public String extractEmail(String token) {
//        return extractClaim(token, Claims::getSubject);
//    }
//
//    /* Method generates a JWT token with employeeDetails as claim. */
////    public String generateTokenForEmployee(String email, EmployeeDetailDto employeeDetails) {
////
////        /* Storing claim in a hashmap. */
////        Map<String, Object> claims = new HashMap<>() {{
////            put("claims", employeeDetails);
////        }};
////
////        return Jwts.builder()
////                .setClaims(claims)
////                .setSubject(email)
////                .setIssuedAt(new Date())
////                .setExpiration(new Date(System.currentTimeMillis() + expiration))
////                .signWith(SignatureAlgorithm.HS256, secret)
////                .compact();
////    }
//
//
//    /* Method generates a JWT token with companyDetails as claim. */
//    public String generateTokenForSuperAdmin(String email, SuperAdminDetailDto superAdminDetail) {
//
//        /* Storing claim in a hashmap. */
//        Map<String, Object> claims = new HashMap<>() {{
//            put("claims", superAdminDetail);
//        }};
//
//        return Jwts.builder()
//                .setClaims(claims)
//                .setSubject(email)
//                .setIssuedAt(new Date())
//                .setExpiration(new Date(System.currentTimeMillis() + expiration))
//                .signWith(SignatureAlgorithm.HS256, secret)
//                .compact();
//    }
//
//    public String generateAccessToken(String email) {
//        return buildToken(email, accessTokenExpiration);
//    }
//
//    // Generate refresh token
//    public String generateRefreshToken(String email) {
//        return buildToken(email, refreshTokenExpiration);
//    }
//
//    private String buildToken(String email, long expiration) {
//        return Jwts.builder()
//                .setSubject(email)
//                .setIssuedAt(new Date())
//                .setExpiration(new Date(System.currentTimeMillis() + expiration))
//                .signWith(SignatureAlgorithm.HS256, secret)
//                .compact();
//    }
//
//    // Generate access token with student details
//    public String generateAccessTokenForStudent(String email, StudentDetailDto studentDetailDto) {
//        Map<String, Object> claims = new HashMap<>();
//        claims.put("claims", studentDetailDto);
//        return buildTokenWithClaims(email, claims, accessTokenExpiration);
//    }
//
//    private String buildTokenWithClaims(String email, Map<String, Object> claims, long expiration) {
//        return Jwts.builder()
//                .setClaims(claims)
//                .setSubject(email)
//                .setIssuedAt(new Date())
//                .setExpiration(new Date(System.currentTimeMillis() + expiration))
//                .signWith(SignatureAlgorithm.HS256, secret)
//                .compact();
//    }
//
//
//
//
//    public boolean validateToken(String token, UserDetails userDetails) {
//        final String username = extractUsername(token);
//        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
//    }
//
//
//}
