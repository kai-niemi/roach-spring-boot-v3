package io.roach.spring.blob;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableHypermediaSupport(type = EnableHypermediaSupport.HypermediaType.HAL)
@EnableTransactionManagement
@EnableJpaRepositories(basePackageClasses = BlobApplication.class, enableDefaultTransactions = false)
@EnableAspectJAutoProxy(proxyTargetClass = true)
@SpringBootApplication
@Configuration
public class BlobApplication {
    public static void main(String[] args) {
        new SpringApplicationBuilder(BlobApplication.class)
                .web(WebApplicationType.SERVLET)
                .run(args);
    }
}