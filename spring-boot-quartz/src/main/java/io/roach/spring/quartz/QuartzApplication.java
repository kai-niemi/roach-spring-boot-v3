package io.roach.spring.quartz;

import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;
import org.slf4j.LoggerFactory;
import org.springframework.boot.Banner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.shell.jline.PromptProvider;

import ch.qos.logback.classic.Level;

@Configuration
@EnableAutoConfiguration
@EnableConfigurationProperties
@ComponentScan(basePackageClasses = QuartzApplication.class)
public class QuartzApplication implements PromptProvider {
    public static void main(String[] args) {
        new SpringApplicationBuilder(QuartzApplication.class)
                .logStartupInfo(true)
                .web(WebApplicationType.NONE)
                .bannerMode(Banner.Mode.CONSOLE)
                .run(args);
    }

    @Override
    public AttributedString getPrompt() {
        ch.qos.logback.classic.LoggerContext loggerContext = (ch.qos.logback.classic.LoggerContext) LoggerFactory
                .getILoggerFactory();
        ch.qos.logback.classic.Logger logger = loggerContext.getLogger("io.roach");
        int fg =
                switch (logger.getLevel().toInt()) {
                    case Level.TRACE_INT -> AttributedStyle.MAGENTA;
                    case Level.DEBUG_INT -> AttributedStyle.CYAN;
                    case Level.INFO_INT -> AttributedStyle.GREEN;
                    case Level.WARN_INT -> AttributedStyle.YELLOW;
                    case Level.ERROR_INT -> AttributedStyle.RED;
                    default -> AttributedStyle.GREEN;
                };
        return new AttributedString("quartz:$ ",
                AttributedStyle.DEFAULT.foreground(fg));
    }
}

