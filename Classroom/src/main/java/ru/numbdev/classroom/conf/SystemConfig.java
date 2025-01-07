package ru.numbdev.classroom.conf;

import com.google.gson.Gson;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class SystemConfig {

    @Bean
    public Gson gson() {
        return new Gson();
    }
}
