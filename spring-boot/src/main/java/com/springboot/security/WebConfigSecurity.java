package com.springboot.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class WebConfigSecurity extends WebSecurityConfigurerAdapter {

	@Autowired
	private ImplementacaoUserDetailsService implementacaoUserDetailsService;
	
	
	/*
	 * Configura as solicitações de acesso por HTTP
	 * */
	@Override 
	protected void configure(HttpSecurity http) throws Exception {
		http.csrf()
			.disable() //Desabilita as configurações padrão de memoria
			.authorizeRequests() //permitir restringir acessos
			.antMatchers(HttpMethod.GET, "/").permitAll() //Qualquer usuário acessa a página inicial
			.antMatchers("/materialize/**").permitAll()
			.antMatchers(HttpMethod.GET, "/cadastropessoa").hasAnyRole("ADMIN")
			.anyRequest().authenticated()
			.and().formLogin().permitAll() //permite qualquer usuário
			.loginPage("/login")
			.defaultSuccessUrl("/cadastropessoa")
			.failureUrl("/login?error=true")
			.and().logout().logoutSuccessUrl("/login") // Mapeia URL de logout e invalida usupario autenticado
			.logoutRequestMatcher(new AntPathRequestMatcher("/logout"));
		
	}
	
	
	/*
	 * Cria autenticação do usuário com banco de dados ou em memória
	 * */
	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		
		auth.userDetailsService(implementacaoUserDetailsService)
			.passwordEncoder(new BCryptPasswordEncoder());
		
		
		
		
		/* Usar em MEMORIA
		auth.inMemoryAuthentication().passwordEncoder(new BCryptPasswordEncoder())
		.withUser("marwam")
		.password("$2a$10$VIbhtmo2H2wvKTiZ8XK.ye0GB5tWuzDLv6rj6IGoXdZYmPHxJEUvG")
		.roles("ADMIN");
		*/
	}
	
	
	/*
	 * Ignora URL especificas
	 * */
	@Override
	public void configure(WebSecurity web) throws Exception {
		web.ignoring().antMatchers("/materialize/**") //Ignora o recurso materialize no sentido de poder utilizar a ferramenta css
		.antMatchers(HttpMethod.GET, "/resource/**", "/static/**", "/materialize/**", "**/materialize/**");
		
	}
	
}
