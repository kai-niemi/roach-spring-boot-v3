package io.roach.spring.sandbox;

import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.Banner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.shell.jline.PromptProvider;

@SpringBootApplication(exclude = {
})
public class Application implements ApplicationRunner, PromptProvider {
    @Override
    public void run(ApplicationArguments args) {
    }

    @Override
    public AttributedString getPrompt() {
        return new AttributedString("sandbox:$ ",
                AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN));
    }

    public static void main(String[] args) {
        new SpringApplicationBuilder(Application.class)
                .web(WebApplicationType.NONE)
                .bannerMode(Banner.Mode.CONSOLE)
                .logStartupInfo(true)
                .run(args);
    }
}
