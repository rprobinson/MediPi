package uk.gov.nhs.digital.telehealth.clinician.web.configurations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.WebMvcAutoConfiguration.WebMvcAutoConfigurationAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

@Configuration
@EnableWebMvc
public class WebApplicationConfig extends WebMvcAutoConfigurationAdapter {

	@Autowired
	private HandlerInterceptor sessionValidationInterceptor;

	@Autowired
	private HandlerInterceptor contextInfoInterceptor;

	@Override
	public void addResourceHandlers(final ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/js/*");
		super.addResourceHandlers(registry);
	}

	/*@Override
	public void addViewControllers(ViewControllerRegistry registry) {
		registry.addViewController("/home").setViewName("misc/home");
		registry.addViewController("/").setViewName("misc/home");
		registry.addViewController("/hello").setViewName("misc/hello");
		registry.addViewController("/login").setViewName("misc/login");
	}*/

	@Override
	public void addInterceptors(final InterceptorRegistry registry) {
		registry.addInterceptor(this.sessionValidationInterceptor);
		registry.addInterceptor(this.contextInfoInterceptor);
	}

	@Bean
	public InternalResourceViewResolver viewResolver() {
		final InternalResourceViewResolver resolver = new InternalResourceViewResolver();
		resolver.setPrefix("/WEB-INF/pages/");
		resolver.setSuffix(".jsp");
		return resolver;
	}

	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}
}