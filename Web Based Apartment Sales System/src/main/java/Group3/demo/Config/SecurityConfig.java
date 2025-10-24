package Group3.demo.Config;

import Group3.demo.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import jakarta.servlet.DispatcherType;  // 👈 Add this import for DispatcherType

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserRepository userRepository;

    // Load user details from DB
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> userRepository.findByUsername(username)
                .map(u -> {
                    var authorities = u.getRoles().stream()
                            .map(r -> new SimpleGrantedAuthority("ROLE_" + r.getName().name()))
                            .toList();
                    return new org.springframework.security.core.userdetails.User(
                            u.getUsername(), u.getPassword(), u.isEnabled(),
                            true, true, true, authorities
                    );
                })
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    // Password encoder
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Authentication provider
    @Bean
    public DaoAuthenticationProvider authProvider(PasswordEncoder encoder, UserDetailsService uds) {
        var provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(uds);
        provider.setPasswordEncoder(encoder);
        return provider;
    }

    // Security filter chain
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, CustomLoginSuccessHandler successHandler) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // 👈 Fix: Permit ERROR dispatches (key for /error forwards) and explicitly /error
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                        .requestMatchers("/error").permitAll()

                        // Public pages
                        .requestMatchers("/", "/home", "/apartment/**", "/promotion/**",
                                "/customer/auth", "/customer/register", "/customer/forgot-password", "/customer/reset-password",
                                "/css/**", "/js/**", "/images/**").permitAll()

                        .requestMatchers("/customer/support/**").hasRole("CUSTOMER")

                        // Dashboards (need login)
                        .requestMatchers("/customer/**").hasRole("CUSTOMER")
                        .requestMatchers("/admin/**").hasRole("ADMIN_OFFICER")
                        .requestMatchers("/it/**").hasRole("IT_TECHNICIAN")
                        .requestMatchers("/sales/**").hasRole("SALES_MANAGER")
                        .requestMatchers("/finance/**").hasRole("FINANCE_ASSISTANT")
                        .requestMatchers("/marketing/**").hasRole("MARKETING_EXECUTIVE")
                        .requestMatchers("/support/**").hasRole("CUSTOMER_SUPPORT")

                        // Allow everything else too
                        .anyRequest().permitAll()
                )
                .formLogin(form -> form
                        .loginPage("/customer/auth")
                        .loginProcessingUrl("/login")
                        .successHandler(successHandler)
                        .failureUrl("/customer/auth?error=true")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/home")
                        .permitAll()
                )
                .csrf(csrf -> csrf.disable());

        return http.build();
    }
}