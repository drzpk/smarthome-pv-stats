package dev.drzepka.pvstats.autoconfiguration

import org.springframework.boot.autoconfigure.http.HttpMessageConverters
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.web.servlet.config.annotation.EnableWebMvc
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver

@Configuration
@EnableWebMvc
class WebAutoConfiguration : WebMvcConfigurer {

    @Bean
    fun httpMessageConverters() = HttpMessageConverters(MappingJackson2HttpMessageConverter())

    @Bean
    fun resolver(): ClassLoaderTemplateResolver {
        val resolver = ClassLoaderTemplateResolver()
        resolver.prefix = "virtual-term/"
        resolver.suffix = ".html"
        resolver.order = 1
        return resolver
    }

    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        super.addResourceHandlers(registry)
        registry.addResourceHandler("/assets/*.js", "/assets/*.css")
                .addResourceLocations("classpath:/virtual-term/")

    }

}