package br.uel.avaliacoes.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder encoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService usuarios(
            PasswordEncoder encoder,
            @Value("${PROFESSOR_PASSWORD}") String senhaProf,
            @Value("${ALUNO_PASSWORD}") String senhaAluno) {

        var professor = User.withUsername("professor")
            .password(encoder.encode(senhaProf))
            .roles("PROFESSOR")
            .build();

        var aluno = User.withUsername("aluno")
            .password(encoder.encode(senhaAluno))
            .roles("ALUNO")
            .build();

        return new InMemoryUserDetailsManager(
            professor, aluno
        );
    }

    @Bean
    public SecurityFilterChain seguranca(
            HttpSecurity http) throws Exception {

        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/", "/index.html", "/css/**",
                    "/js/**"
                ).permitAll()

                .requestMatchers(
                    "/professor.html",
                    "/correcao.html"
                ).hasRole("PROFESSOR")

                .requestMatchers(
                    "/aluno.html"
                ).hasRole("ALUNO")

                .requestMatchers(
                    HttpMethod.POST,
                    "/questoes",
                    "/avaliacoes",
                    "/avaliacoes/*/questoes",
                    "/avaliacoes/*/alunos"
                ).hasRole("PROFESSOR")

                .requestMatchers(
                    HttpMethod.PATCH,
                    "/respostas/*/nota"
                ).hasRole("PROFESSOR")

                .requestMatchers(
                    HttpMethod.POST,
                    "/participacoes/*/respostas"
                ).hasRole("ALUNO")

                .requestMatchers(
                    "/relatorios/**"
                ).hasRole("PROFESSOR")

                .anyRequest().authenticated()
            )
            .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}
