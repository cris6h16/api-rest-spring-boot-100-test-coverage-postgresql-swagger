package org.cris6h16.apirestspringboot.Config.Security;

import lombok.extern.slf4j.Slf4j;
import org.cris6h16.apirestspringboot.Config.Security.UserDetailsService.UserDetailsServiceImpl;
import org.cris6h16.apirestspringboot.Repositories.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.expression.WebExpressionAuthorizationManager;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

import static org.cris6h16.apirestspringboot.Constants.Cons.Note.Controller.Path.NOTE_PATH;
import static org.cris6h16.apirestspringboot.Constants.Cons.User.Controller.Path.*;
import static org.springframework.security.config.Customizer.withDefaults;

/**
 * Custom Security configuration class.
 *
 * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
 * @since 1.0
 */
@Configuration
@Slf4j
//@EnableMethodSecurity
@EnableWebSecurity
public class SecurityConfig {

    private final WebSecurity webSecurity;

    public SecurityConfig(WebSecurity webSecurity) {
        this.webSecurity = webSecurity;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(withDefaults()) // use a bean known as corsConfigurationSource
                .httpBasic(withDefaults())
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers(HttpMethod.GET, USER_PATH).hasRole("ADMIN")// page of users
                        .requestMatchers(HttpMethod.POST, USER_PATH).permitAll()                       // create a user
                        .requestMatchers(NOTE_PATH + "/**").hasAnyRole("ADMIN", "USER")       // all note endpoints
                        .requestMatchers(getAllUserPathsThatCanOperateJustTheOwners()).access((authentication, request) -> {
                            String userId = request.getVariables().get("id");
                            boolean granted = webSecurity.checkIfIsAdminOrUserAndHasThisIdAsPrincipalId(authentication, userId);
                            return new AuthorizationDecision(granted);
                        })
                        .requestMatchers("/docs/**").permitAll()
                        .anyRequest().access(new WebExpressionAuthorizationManager("hasRole('ADMIN')"))
                )
//                .sessionManagement(
//                        sm -> sm
//                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
//                )
        ;
        return http.build();
    }

    private String[] getAllUserPathsThatCanOperateJustTheOwners() {
        return new String[]{
                USER_PATH + COMPLEMENT_PATCH_USERNAME + "/{id}",
                USER_PATH + COMPLEMENT_PATCH_EMAIL + "/{id}",
                USER_PATH + COMPLEMENT_PATCH_PASSWORD + "/{id}",
                USER_PATH + "/{id}" // get the user by id
        };
    }


    @Bean
    public static PasswordEncoder passwordEncoder() {
        PasswordEncoder dpe = PasswordEncoderFactories.createDelegatingPasswordEncoder();
//        dpe.upgradeEncoding("noop");
        return dpe;
    }

    @Bean
    UserDetailsService userDetailsService(UserRepository ur, PasswordEncoder pe) {
        return new UserDetailsServiceImpl(ur, pe);
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration conf = new CorsConfiguration();
        conf.setAllowedOrigins(Arrays.asList("https://example.com:8080"));
        conf.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE"));
        conf.setAllowedHeaders(List.of("Authorization", "Content-Type"));
//        conf.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", conf);
        return source;
    }
}
