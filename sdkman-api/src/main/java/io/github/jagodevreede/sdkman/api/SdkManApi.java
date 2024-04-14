package io.github.jagodevreede.sdkman.api;

import io.github.jagodevreede.sdkman.api.domain.Candidate;
import io.github.jagodevreede.sdkman.api.domain.JavaVersion;
import io.github.jagodevreede.sdkman.api.domain.Vendor;
import io.github.jagodevreede.sdkman.api.http.CachedHttpClient;
import io.github.jagodevreede.sdkman.api.parser.CandidateListParser;
import io.github.jagodevreede.sdkman.api.parser.VersionListParser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.net.http.HttpClient.newHttpClient;

public class SdkManApi {

    public static final String BASE_URL = "https://api.sdkman.io/2";
    public static final Duration DEFAUL_CACHE_DURATION = Duration.of(1, ChronoUnit.HOURS);
    public static final String DEFAULT_SDKMAN_HOME = System.getProperty("user.home") + "/.sdkman";
    private static final Pattern VERSION_PATTERN = Pattern.compile("(.*)-(.+)");

    private final CachedHttpClient client;
    private final String baseFolder;

    public SdkManApi(String baseFolder) {
        this.baseFolder = baseFolder;
        this.client = new CachedHttpClient(baseFolder + "/.http_cache", DEFAUL_CACHE_DURATION, newHttpClient());
    }

    public List<Candidate> getCandidates() throws Exception {
        String response = client.get(BASE_URL + "/candidates");
        return CandidateListParser.parse(response);
    }

    public List<JavaVersion> getJavaVersions() throws IOException, InterruptedException {
        String response = client.get(BASE_URL + "/candidates/java/" + getPlatformName() + "/versions/list?installed=");
        var versions = VersionListParser.parseJava(response);
        var localInstalled = new HashSet<>(getLocalInstalledVersions("java"));
        var result = new ArrayList<JavaVersion>();
        var vendors = new HashSet<Vendor>();
        for (var version : versions) {
            if (localInstalled.contains(version.identifier())) {
                result.add(new JavaVersion(version, true));
                localInstalled.remove(version.identifier());
            } else {
                result.add(new JavaVersion(version, false));
            }
            vendors.add(new Vendor(version.vendor(), version.dist()));
        }

        for (var version : localInstalled) {
            Matcher matcher = VERSION_PATTERN.matcher(version);
            if (matcher.matches()) {
                var dist = matcher.group(2);
                var name = vendors.stream()
                        .filter(v -> v.dist().equals(dist))
                        .findFirst()
                        .map(Vendor::vendor)
                        .orElse("Unclassified");
                result.add(new JavaVersion(name, matcher.group(1), dist, version, true));
            } else {
                result.add(new JavaVersion("Unclassified", "", "none", version, true));
            }
        }

        result.sort(JavaVersion::compareTo);
        return result;
    }

    public void changeGlobal(String candidate, String toIdentifier) throws IOException {
        File toFolder = new File(baseFolder, "candidates/" + candidate + "/" + toIdentifier);
        if (!toFolder.exists()) {
            throw new IllegalArgumentException("No such identifier for candidate " + candidate + ": " + toIdentifier);
        }
        File currentFolder = new File(baseFolder, "candidates/" + candidate + "/current");
        if (currentFolder.exists()) {
            currentFolder.delete();
        }
        Files.createSymbolicLink(currentFolder.toPath(), toFolder.toPath());
    }

    public List<String> getLocalInstalledVersions(String candidate) {
        var candidatesFolder = new File(baseFolder + "/candidates/" + candidate);
        if (!candidatesFolder.exists()) {
            return List.of();
        }
        return List.of(Objects.requireNonNull(candidatesFolder.list((dir, name) ->
                new File(dir, name).isDirectory() && !"current".equals(name))));
    }

    public String getCurrentCandidateFromPath(String candidate) {
        var paths = System.getenv("PATH").split(OsHelper.getPathSeparator());
        var pathName = baseFolder + "/candidates/" + candidate;
        for (var path : paths) {
            if (path.startsWith(pathName)) {
                return path.substring(pathName.length() + 1).replace("/bin", "");
            }
        }
        return null;
    }

    private String updatePathForCandidate(String candidate, String identifier) {
        var paths = System.getenv("PATH").split(OsHelper.getPathSeparator());
        var pathName = baseFolder + "/candidates/" + candidate;
        return Stream.of(paths).map(path -> {
            if (path.startsWith(pathName)) {
                return baseFolder + "/candidates/" + candidate + "/" + identifier + "/bin";
            }
            return path;
        }).toList().stream().collect(Collectors.joining(OsHelper.getPathSeparator()));
    }

    public File getExitScriptFile() {
        if (OsHelper.hasShell()) {
            return new File(baseFolder, "tmp/exit-script.sh");
        } else {
            return new File(baseFolder, "tmp/exit-script.cmd");
        }
    }

    public void createExitScript(String candidate, String identifier) throws IOException {
        if (OsHelper.hasShell()) {
            try (var writer = Files.newBufferedWriter(getExitScriptFile().toPath())) {
                writer.write("export PATH=" + updatePathForCandidate(candidate, identifier));
            }
        } else {
            try (var writer = Files.newBufferedWriter(getExitScriptFile().toPath())) {
                writer.write("set PATH=" + updatePathForCandidate(candidate, identifier));
            }
        }
    }

    public String getPlatformName() {
        return OsHelper.getPlatformName();
    }

    public String resolveCurrentVersion(String candidate) throws IOException {
        var candidatesFolder = new File(baseFolder + "/candidates/" + candidate);
        File current = new File(candidatesFolder, "current");
        if (current.exists()) {
            var realPath = current.toPath().toRealPath().toString();
            return realPath.substring(candidatesFolder.getAbsolutePath().length() + 1).replace("/bin", "");
        }
        return null;
    }

    /** Always create an exit file, as that is being called after the program exits. */
    public void registerShutdownHook() {
        Thread printingHook = new Thread(() -> {
            File exitScriptFile = getExitScriptFile();
            if (!exitScriptFile.exists()) {
                try {
                    exitScriptFile.createNewFile();
                } catch (IOException e) {
                    throw new RuntimeException(e);}
            }

        });
        Runtime.getRuntime().addShutdownHook(printingHook);
    }

}
