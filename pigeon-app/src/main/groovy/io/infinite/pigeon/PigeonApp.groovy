package io.infinite.pigeon


import io.infinite.blackbox.BlackBox
import io.infinite.carburetor.CarburetorLevel
import io.infinite.pigeon.threads.PigeonThread
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.hateoas.config.EnableHypermediaSupport

@EnableHypermediaSupport(type = EnableHypermediaSupport.HypermediaType.HAL)
@SpringBootApplication
@BlackBox(level = CarburetorLevel.METHOD)
class PigeonApp implements CommandLineRunner {

    @Autowired
    PigeonThread pigeonThread

    static void main(String[] args) {
        SpringApplication.run(PigeonApp.class, args)
    }

    @Override
    void run(String... args) throws Exception {
        pigeonThread.start()
    }

}
