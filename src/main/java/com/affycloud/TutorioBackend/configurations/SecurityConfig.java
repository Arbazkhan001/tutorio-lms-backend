package com.affycloud.TutorioBackend.configurations;


import com.affycloud.TutorioBackend.filter.CustomAuthenticationEntryPoint;
import com.affycloud.TutorioBackend.filter.JwtRequestFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtRequestFilter jwtRequestFilter;
    private final UserDetailsService userDetailsService;
    private final AccessDeniedHandler accessDeniedHandler; // Inject the custom AccessDeniedHandler
    private final CustomAuthenticationEntryPoint authenticationEntryPoint; // Add this



    public SecurityConfig(JwtRequestFilter jwtRequestFilter, UserDetailsService userDetailsService , AccessDeniedHandler accessDeniedHandler, CustomAuthenticationEntryPoint authenticationEntryPoint ) {
        this.jwtRequestFilter = jwtRequestFilter;
        this.userDetailsService = userDetailsService;
        this.accessDeniedHandler = accessDeniedHandler;
        this.authenticationEntryPoint = authenticationEntryPoint; // Add this


    }

    /*  Spring Security Se-tup role based authentication */

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                )
                .build();
    }


    /*@Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth.requestMatchers("/api/addresses/**","/api/admin/**",
                                "/api/book/**","/api/cart/**","/api/course-bundles/**",
                                "/api/dashboard/**","/api/doubts/**",
                                "/api/orders/**","/api/exams/**","/api/fcm/**","/api/login/**",
                                "/api/notification/**","/api/orders/**","/api/questions/**","/api/coupons/**",
                                "/api/sections/**","/api/student/**","/api/subject/**","/api/chapter/**",
                                "/api/submissions/**","/api/teacher/**","/api/tests/**","/api/streaming/**",
                                "/api/variant-names/**","/api/videos/**","/api/video-progress/**",
                                "/api/proxy/**","/api/zoom/**","/ws/**", "/v3/api-docs/**", "/swagger-ui/**",
                                "api/images/**","/api/**","api/books/**","api/blogs/**","api/tests/**","api/thumbnails/**","thumbnails/**").permitAll()
                        // In Course Only Handling Course ADMIN can make POST, PUT, DELETE requests
                        .requestMatchers(HttpMethod.GET, "/api/course/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/course/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/course/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/course/**").hasAuthority("ADMIN")
                        // In Blog Only  ADMIN can make POST, PUT, DELETE requests
                        .requestMatchers(HttpMethod.GET, "/api/blog/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/blog/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/blog/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/blog/**").hasAuthority("ADMIN")
                        // In Book Only  ADMIN can make POST, PUT, DELETE requests
                        .requestMatchers(HttpMethod.GET, "/api/book/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/book/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/book/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/book/**").hasAuthority("ADMIN")
                        // In E-book Only  ADMIN can make POST, PUT, DELETE requests
                        .requestMatchers(HttpMethod.GET, "/api/ebooks/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/ebooks/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/ebooks/**").hasAnyAuthority("ADMIN","TEACHER")
                        .requestMatchers(HttpMethod.DELETE, "/api/ebooks/**").hasAuthority("ADMIN")
                        // In TestSeries Only  ADMIN can make POST, PUT, DELETE requests
                        .requestMatchers(HttpMethod.GET, "/api/testSeries/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/testSeries/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/testSeries/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/testSeries/**").hasAuthority("ADMIN")
                        .requestMatchers( "/api/logout").hasAuthority("STUDENT")
                        */
    /*.requestMatchers("/api/variant-names/**").hasAnyAuthority("ADMIN", "STUDENT") // Restrict access to admin APIs
                        .requestMatchers("/api/variant-names/**").hasAnyAuthority( "STUDENT") // Restrict access to admin APIs
                        .requestMatchers("/api/variant-names/**").hasAnyAuthority( "ADMIN") // Restrict access to admin APIs
*/
    /*
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler) // Use custom access denied handler
                )
                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }*/


    /*
   "/api/addresses/**","/api/admin/**",
                               "/api/blog/**","/api/book/**","/api/cart/**","/api/course-bundles/**",
                               "/api/course/**","/api/dashboard/**","/api/doubts/**","/api/ebooks/**",
                               "/api/orders/**","/api/exams/**","/api/fcm/**","/api/login/**",
                               "/api/notification/**","/api/orders/**","/api/questions/**",
                               "/api/sections/**","/api/student/**","/api/subject/**","/api/chapter/**",
                               "/api/submissions/**","/api/teacher/**","/api/tests/**","/api/testSeries/**",
                               "/api/variant-names/**","/api/videos/**","/api/video-progress/**",
                               "/api/proxy/**","/api/zoom/**"
    */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }



}

/*
Trash
 */

/* for disable Spring Security*/
    /*@Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll() // Allow all requests without authentication
                )
                .csrf(AbstractHttpConfigurer::disable) // Disable CSRF protection
                .build();
    }*/
                        /*auth
                        .requestMatchers("/api/login/**","/api/addresses/**",
                                "/api/course/**", "/api/ebooks/**","/api/student/**",
                                "/api/teacher/**","/api/tests/**","/api/testSeries/**",
                                "/api/videos/**",
                                "/v3/api-docs/**", "/swagger-ui/**").permitAll()*/ // Allow Swagger UI, login. other end-points
/* Spring Security Se-tup role based authentication*/
    /*
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/login").permitAll()
                        .requestMatchers("/api/admin/**").hasAuthority("SUPER_ADMIN")
                        .requestMatchers("/api/test/**").hasAuthority("ADMIN")
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exception -> exception
                        .accessDeniedHandler(accessDeniedHandler) // Use custom access denied handler
                )
                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class)
                        .csrf(AbstractHttpConfigurer::disable).build();

    }
    */