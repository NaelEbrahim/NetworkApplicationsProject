package NetworkApplicationsProject.Services.SecurityServices;

import NetworkApplicationsProject.Models.UserModel;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.function.Function;

@Service
public class JWTService {

    public String getEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }


    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(SECRET_KEY())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String generateJWT(UserModel user) {
        Date now = new Date();
        final long EXPIRATION_TIME = 24 * 60 * 60 * 1000; // 24 hours in milliseconds
        return Jwts
                .builder()
                .setSubject(user.getEmail())
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + EXPIRATION_TIME))
                .signWith(SECRET_KEY(), SignatureAlgorithm.HS256)
                .compact();
    }

    private Key SECRET_KEY() {
        String SECRETKEY = "qskjI5fsW9xMmdc66GkcUoljioMnnksyP9wfA8Rjdwvb9zi7YcHpNVnDp2bFMu3jilQliYvKKW47dl/dYWtyWQ==";
        byte[] keybytes = Decoders.BASE64.decode(SECRETKEY);
        return Keys.hmacShaKeyFor(keybytes);
    }

    private Date extractExpirationDate(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public boolean isTokenExpired(String token) {
        return extractExpirationDate(token).before(new Date());
    }

}