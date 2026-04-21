package cm.agribind.marketweather;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class
MarketWeatherServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(MarketWeatherServiceApplication.class, args);
    }
}
