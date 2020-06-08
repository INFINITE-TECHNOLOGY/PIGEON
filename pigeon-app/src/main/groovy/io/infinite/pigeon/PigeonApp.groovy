package io.infinite.pigeon

import groovy.util.logging.Slf4j
import io.infinite.blackbox.BlackBox
import io.infinite.carburetor.CarburetorLevel
import io.infinite.pigeon.services.PigeonService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.hateoas.config.EnableHypermediaSupport

@EnableHypermediaSupport(type = EnableHypermediaSupport.HypermediaType.HAL)
@SpringBootApplication
@BlackBox(level = CarburetorLevel.METHOD)
@Slf4j
class PigeonApp implements CommandLineRunner {

    @Autowired
    PigeonService pigeonService

    static void main(String[] args) {
        SpringApplication.run(PigeonApp.class, args)
    }

    @Override
    void run(String... args) throws Exception {
        log.debug("pigeonService", pigeonService)
        log.info("Pigeon started.")
    }

}
