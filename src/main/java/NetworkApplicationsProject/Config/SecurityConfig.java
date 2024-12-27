package NetworkApplicationsProject.Config;

import NetworkApplicationsProject.Security.JWTRequestFilter;
import lombok.AllArgsConstructor;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@AllArgsConstructor
public class SecurityConfig {

    private final JWTRequestFilter filter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:8989")); // Allow all origins
        configuration.setAllowedMethods(List.of("*")); // Allow all HTTP methods
        configuration.setAllowedHeaders(List.of("*")); // Allow all headers
        configuration.setAllowCredentials(false); // Adjust based on your requirements

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        httpSecurity.cors(cors -> cors.configurationSource(source)).csrf(AbstractHttpConfigurer::disable)
                .addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("api/auth/register","api/auth/login", "/swagger-ui/**",
                                "/v3/api-docs/**","/testNotification.html","api/files/downloadFile","api/auth/refreshAccessToken")
                        .permitAll()
                        .anyRequest()
                        .authenticated());
        return httpSecurity.build();
    }

}