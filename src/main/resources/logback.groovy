appender("System.err", ConsoleAppender) {
    target = System.out
    encoder(PatternLayoutEncoder) {
        pattern = "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
    }
}
logger("io.infinite.blackbox", ERROR, ["System.err"])
//root(ALL, ["System.out"])