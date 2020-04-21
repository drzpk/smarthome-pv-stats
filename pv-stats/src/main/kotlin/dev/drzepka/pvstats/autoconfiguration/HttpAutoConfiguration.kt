package dev.drzepka.pvstats.autoconfiguration

import org.springframework.boot.autoconfigure.http.HttpMessageConverters
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter

@Configuration
class HttpAutoConfiguration {

    @Bean
    fun httpMessageConverters() = HttpMessageConverters(MappingJackson2HttpMessageConverter())
}