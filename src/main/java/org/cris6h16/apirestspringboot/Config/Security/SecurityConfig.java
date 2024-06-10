package org.cris6h16.apirestspringboot.Config.Security;

import org.cris6h16.apirestspringboot.Config.Security.UserDetailsService.UserDetailsServiceImpl;
import org.cris6h16.apirestspringboot.Repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

/**
 * Custom Security configuration class.
 *
 * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
 * @since 1.0
 */
@Configuration
@EnableMethodSecurity
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
//               .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(withDefaults());
        return http.build();
    }

    /**
     * Create a delegating password encoder bean. The default encoder is {@code bcrypt}.
     *
     * @return A new instance of {@link DelegatingPasswordEncoder}.
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    @Bean
    public static PasswordEncoder passwordEncoder() {
        PasswordEncoder dpe = PasswordEncoderFactories.createDelegatingPasswordEncoder();
//        dpe.upgradeEncoding("noop");
        return dpe;
    }

    /**
     * Create a new instance of {@link UserDetailsServiceImpl} to load the user from the database.
     * This bean is used by the {@link DaoAuthenticationProvider} to authenticate the user.
     *
     * @param ur The {@link UserRepository} to load the user from the database.
     * @param pe The {@link PasswordEncoder} to encode the password.
     * @return A new instance of {@link UserDetailsServiceImpl}.
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    @Bean
    UserDetailsService userDetailsService(UserRepository ur, PasswordEncoder pe) {
        return new UserDetailsServiceImpl(ur, pe);
    }
}
