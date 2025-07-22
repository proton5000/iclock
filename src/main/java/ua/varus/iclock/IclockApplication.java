package ua.varus.iclock;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@RequiredArgsConstructor
@SpringBootApplication
public class IclockApplication {

    public static void main(String[] args) {
        SpringApplication.run(IclockApplication.class, args);
    }
}
