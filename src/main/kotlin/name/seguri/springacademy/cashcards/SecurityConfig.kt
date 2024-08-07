package name.seguri.springacademy.cashcards

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.provisioning.InMemoryUserDetailsManager
import org.springframework.security.web.SecurityFilterChain

@Configuration
class SecurityConfig {

  @Bean
  @Throws(Exception::class)
  fun filterChain(http: HttpSecurity): SecurityFilterChain =
    http
      .authorizeHttpRequests { it.requestMatchers("/cashcards/**").hasRole("CARD-OWNER") }
      .httpBasic(Customizer.withDefaults())
      .csrf { it.disable() }
      .build()

  @Bean fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

  @Bean
  fun testOnlyUsers(passwordEncoder: PasswordEncoder): UserDetailsService {
    val users = User.builder()
    val sarah: UserDetails =
      users
        .username("sarah1")
        .password(passwordEncoder.encode("abc123"))
        .roles("CARD-OWNER")
        .build()
    val hankOwnsNoCards: UserDetails =
      users
        .username("hank-owns-no-cards")
        .password(passwordEncoder.encode("qrs456"))
        .roles("NON-OWNER")
        .build()
    val kumar: UserDetails =
      users
        .username("kumar2")
        .password(passwordEncoder.encode("xyz789"))
        .roles("CARD-OWNER")
        .build()
    return InMemoryUserDetailsManager(sarah, hankOwnsNoCards, kumar)
  }
}
