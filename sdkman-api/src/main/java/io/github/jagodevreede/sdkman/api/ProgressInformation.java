package io.github.jagodevreede.sdkman.api;

public interface ProgressInformation {
    void publishProgress(int current);

    void publishState(String state);
}
