package io.github.jagodevreede.sdkman.api;

import io.github.jagodevreede.sdkman.api.domain.Candidate;
import io.github.jagodevreede.sdkman.api.domain.CandidateVersion;
import io.github.jagodevreede.sdkman.api.domain.Vendor;
import io.github.jagodevreede.sdkman.api.files.FileUtil;
import io.github.jagodevreede.sdkman.api.files.ZipExtractTask;
import io.github.jagodevreede.sdkman.api.http.CachedHttpClient;
import io.github.jagodevreede.sdkman.api.http.DownloadTask;
import io.github.jagodevreede.sdkman.api.parser.CandidateListParser;
import io.github.jagodevreede.sdkman.api.parser.VersionListParser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
    /**
     * Used to extract name and dist from a identifier, first group is the name, second group is the dist
     */
    private static final Pattern IDENTIFIER_PATTERN = Pattern.compile("(.*)-(.+)");

    private final CachedHttpClient client;
    private final String baseFolder;
    private Map<String, String> changes = new HashMap<>();
    private boolean offline;

    public SdkManApi(String baseFolder) {
        this.baseFolder = baseFolder;
        this.client = new CachedHttpClient(baseFolder + "/.http_cache", DEFAUL_CACHE_DURATION, newHttpClient());
    }

    public boolean isOffline() {
        return offline;
    }

    public void setOffline(boolean offline) {
        this.offline = offline;
    }

    public List<Candidate> getCandidates() throws Exception {
        String response = client.get(BASE_URL + "/candidates", offline);
        return CandidateListParser.parse(response);
    }

    public List<CandidateVersion> getVersions(String candidate) throws IOException, InterruptedException {
        String response = client.get(BASE_URL + "/candidates/" + candidate + "/" + getPlatformName() + "/versions/list?installed=", offline);
        var versions = VersionListParser.parse(response);
        var localInstalled = new HashSet<>(getLocalInstalledVersions(candidate));
        var localAvailable = new HashSet<>(getLocalAvailableVersions(candidate));
        var result = new ArrayList<CandidateVersion>();
        var vendors = new HashSet<Vendor>();
        for (var version : versions) {
            var installed = localInstalled.remove(version.identifier());
            var available = localAvailable.remove(version.identifier());
            result.add(new CandidateVersion(version, installed, available));
            vendors.add(new Vendor(version.vendor(), version.dist()));
        }

        for (var identifier : localInstalled) {
            Matcher matcher = IDENTIFIER_PATTERN.matcher(identifier);
            var available = localAvailable.remove(identifier);
            if (matcher.matches()) {
                var dist = matcher.group(2);
                var name = vendors.stream()
                        .filter(v -> Objects.equals(v.dist(), dist))
                        .findFirst()
                        .map(Vendor::vendor)
                        .orElse("Unclassified");
                result.add(new CandidateVersion(name, matcher.group(1), dist, identifier, true, available));
            } else {
                result.add(new CandidateVersion("Unclassified", "", "none", identifier, true, available));
            }
        }

        for (var identifier : localAvailable) {
            Matcher matcher = IDENTIFIER_PATTERN.matcher(identifier);
            if (matcher.matches()) {
                var dist = matcher.group(2);
                var name = vendors.stream()
                        .filter(v -> Objects.equals(v.dist(), dist))
                        .findFirst()
                        .map(Vendor::vendor)
                        .orElse("Unclassified");
                result.add(new CandidateVersion(name, matcher.group(1), dist, identifier, false, true));
            } else {
                result.add(new CandidateVersion("Unclassified", "", "none", identifier, false, true));
            }
        }

        result.sort(CandidateVersion::compareTo);
        return result;
    }

    public void changeGlobal(String candidate, String toIdentifier) throws IOException {
        File toFolder = new File(baseFolder, "candidates/" + candidate + "/" + toIdentifier);
        if (!toFolder.exists()) {
            throw new IllegalArgumentException("No such identifier for candidate " + candidate + ": " + toIdentifier);
        }
        File currentFolder = new File(baseFolder, "candidates/" + candidate + "/current");
        if (SdkManUiPreferences.load().canCreateSymlink) {
            if (currentFolder.exists()) {
                currentFolder.delete();
            }
            Files.createSymbolicLink(currentFolder.toPath(), toFolder.toPath());
        } else {
            if (currentFolder.exists()) {
                FileUtil.deleteRecursively(currentFolder);
            }
            FileUtil.copyDirectory(toFolder.getAbsolutePath(), currentFolder.getAbsolutePath());
        }
    }

    public List<String> getLocalInstalledVersions(String candidate) {
        var candidatesFolder = new File(baseFolder + "/candidates/" + candidate);
        if (!candidatesFolder.exists()) {
            return List.of();
        }
        return List.of(Objects.requireNonNull(candidatesFolder.list((dir, name) ->
                new File(dir, name).isDirectory() && !"current".equals(name))));
    }

    private List<String> getLocalAvailableVersions(String candidate) {
        var archiveFolder = new File(baseFolder + "/archives");
        if (!archiveFolder.exists()) {
            return List.of();
        }
        return Stream.of(Objects.requireNonNull(archiveFolder.list((dir, name) ->
                        new File(dir, name).isFile() && name.startsWith(candidate) && name.endsWith(".zip"))))
                .map(name -> name.substring(candidate.length() + 1, name.length() - 4))
                .toList();
    }

    public String getCurrentCandidateFromPath(String candidate) {
        if (changes.containsKey(candidate)) {
            return changes.get(candidate);
        }
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
        changes.put(candidate, identifier);
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

    /**
     * Always create an exit file, as that is being called after the program exits.
     */
    public void registerShutdownHook() {
        Thread printingHook = new Thread(() -> {
            File exitScriptFile = getExitScriptFile();
            if (!exitScriptFile.exists()) {
                try {
                    exitScriptFile.createNewFile();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

        });
        Runtime.getRuntime().addShutdownHook(printingHook);
    }

    public DownloadTask download(String identifier, String version) {
        File finalArchiveFile = new File(baseFolder, "archives/" + identifier + "-" + version + ".zip");
        String url = BASE_URL + "/broker/download/" + identifier + "/" + version + "/" + getPlatformName();
        File tempFile = new File(baseFolder, "tmp/" + identifier + "-" + version + ".bin");
        return new DownloadTask(url, tempFile, finalArchiveFile, identifier + "-" + version);
    }

    public void install(String identifier, String version) {
        File archiveFile = new File(baseFolder, "archives/" + identifier + "-" + version + ".zip");
        ZipExtractTask.extract(archiveFile, new File(baseFolder, "candidates/" + identifier + "/" + version));
    }

    public void uninstall(String identifier, String version) {
        File candidateFolder = new File(baseFolder, "candidates/" + identifier + "/" + version);
        if (candidateFolder.exists()) {
            try {
                FileUtil.deleteRecursively(candidateFolder);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
