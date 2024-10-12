package io.github.jagodevreede.sdkmanui;

import com.google.gson.Gson;
import io.github.jagodevreede.sdkmanui.updater.TestHelper;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class NativeImageFileCheckerTest {

    /**
     * Compares the native-image resource-config.json files for the different OS, they should be the same except some
     * specific files
     */
    @ParameterizedTest
    @ValueSource(strings = {"mac"})
    void checkResourceFilesSame(String toCheck) throws IOException {
        try (var windowsResouce = TestHelper.class.getClassLoader().getResourceAsStream("META-INF/native-image-windows/resource-config.json"); var toTestResouce = TestHelper.class.getClassLoader().getResourceAsStream("META-INF/native-image-" + toCheck + "/resource-config.json")) {
            var windowsMap = new Gson().fromJson(new String(windowsResouce.readAllBytes(), StandardCharsets.UTF_8), Map.class);
            var tetsMap = new Gson().fromJson(new String(toTestResouce.readAllBytes(), StandardCharsets.UTF_8), Map.class);

            var resourcesList = getResourceToInclude(windowsMap);
            var resourcesTestList = getResourceToInclude(tetsMap);

            assertThat(resourcesList).containsExactlyInAnyOrder(resourcesTestList.toArray(new String[0]));
        }
    }

    private static List<String> getResourceToInclude(Map windowsMap) {
        Map<String, Object> resources = (Map<String, Object>) windowsMap.get("resources");
        List<Map<String, String>> includes = (List<Map<String, String>>) resources.get("includes");
        return includes.stream().flatMap(include -> include.values().stream()).filter(s -> !s.contains(".zip")) // We don't expect zip to be in all dists
                .filter(s -> !s.contains(".dll")) // dll is only for windows
                .filter(s -> !s.contains(".cmd")) // cmd is only for windows
                .filter(s -> !s.contains(".sh")) // sh is only for linux and mac
                .toList();
    }

    @ParameterizedTest
    @ValueSource(strings = {"mac"})
    void checkReflectFilesSame(String toCheck) throws IOException {
        try (var windowsResouce = TestHelper.class.getClassLoader().getResourceAsStream("META-INF/native-image-windows/reflect-config.json"); var toTestResouce = TestHelper.class.getClassLoader().getResourceAsStream("META-INF/native-image-" + toCheck + "/reflect-config.json")) {
            var windowsMap = new Gson().fromJson(new String(windowsResouce.readAllBytes(), StandardCharsets.UTF_8), List.class);
            var testsMap = new Gson().fromJson(new String(toTestResouce.readAllBytes(), StandardCharsets.UTF_8), List.class);

            var reflectNameAndMethods = getReflectNameAndMethods(windowsMap);
            var reflectNameAndMethodsToTest = getReflectNameAndMethods(testsMap);

            assertThat(reflectNameAndMethods).hasSizeGreaterThan(1).containsExactlyInAnyOrder(reflectNameAndMethodsToTest.toArray(new ReflectNameAndMethods[0]));
        }
    }

    /**
     * example:
     * {
     * "name":"java.lang.Character",
     * "methods":[{"name":"isIdeographic","parameterTypes":["int"] }]
     * },
     */
    private List<ReflectNameAndMethods> getReflectNameAndMethods(List<Map<String, Object>> windowsList) {
        return windowsList.stream().filter(i -> i.get("name") != null).filter(i -> ((String) i.get("name")).startsWith("io.github.jagodevreede")).map(i -> {
            var name = (String) i.get("name");
            var methodsMap = (List<Map<String, String>>) i.get("methods");
            List<String> methods = new ArrayList<>();
            if (methodsMap != null) {
                methods = methodsMap.stream().map(j -> j.get("name")).toList();
            }
            return new ReflectNameAndMethods(name, methods);
        }).toList();
    }

    record ReflectNameAndMethods(String name, List<String> methods) {
    }

}
