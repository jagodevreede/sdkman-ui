module sdkmanapi {
    requires java.net.http;
    requires org.slf4j;
    exports io.github.jagodevreede.sdkman.api;
    exports io.github.jagodevreede.sdkman.api.domain;
    exports io.github.jagodevreede.sdkman.api.http;
}