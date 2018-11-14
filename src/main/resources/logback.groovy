appender("System.out", ConsoleAppender) {
    target = "System.out"
    encoder(PatternLayoutEncoder) {
        pattern = "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
    }
}
//logger("io.infinite.blackbox.BlackBoxEngineSequential", ERROR, ["System.out"])
root(INFO, ["System.out"])