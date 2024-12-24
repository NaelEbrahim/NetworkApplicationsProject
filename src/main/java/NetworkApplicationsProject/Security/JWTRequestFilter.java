package NetworkApplicationsProject.Security;

import NetworkApplicationsProject.Models.UserModel;
import NetworkApplicationsProject.Repositories.TokenRepository;
import NetworkApplicationsProject.Repositories.UserRepository;
import NetworkApplicationsProject.Services.SecurityServices.JWTService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;

@Component
@AllArgsConstructor
public class JWTRequestFilter extends OncePerRequestFilter {

    private final JWTService jwtService;

    private final UserRepository userRepository;

    private final TokenRepository tokenRepository;


    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        String tokenHeader = request.getHeader("Authorization");
        if (tokenHeader != null && tokenHeader.startsWith("Bearer ")) {
            String token = tokenHeader.substring(7);
            try {
                String userEmail = jwtService.getEmail(token);
                if (userEmail != null) {
                    Optional<UserModel> currentUser = userRepository.findByEmail(userEmail);
                    var validToken = tokenRepository.findByServerToken(token);
                    if (currentUser.isPresent() && validToken.isPresent()) {// && !jwtService.isTokenExpired(token)
                        UserModel user = currentUser.get();
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(user, null, new ArrayList<>());
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                }
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
                //throw new CustomException("User Not Found", HttpStatus.FORBIDDEN);
            }
        }
        filterChain.doFilter(request, response);
    }

}