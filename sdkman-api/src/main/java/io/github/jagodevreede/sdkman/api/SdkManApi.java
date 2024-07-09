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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static io.github.jagodevreede.sdkman.api.OsHelper.getGlobalPath;
import static io.github.jagodevreede.sdkman.api.OsHelper.getPlatformName;
import static java.io.File.separator;
import static java.net.http.HttpClient.newHttpClient;

public class SdkManApi {
    private static Logger logger = LoggerFactory.getLogger(SdkManApi.class);

    public static final String BASE_URL = "https://api.sdkman.io/2";
    public static final Duration DEFAUL_CACHE_DURATION = Duration.of(1, ChronoUnit.HOURS);
    public static final String DEFAULT_SDKMAN_HOME = System.getProperty("user.home") + separator + ".sdkman";
    /**
     * Used to extract name and dist from a identifier, first group is the name, second group is the dist
     */
    private static final Pattern IDENTIFIER_PATTERN = Pattern.compile("(.*)-(.+)");

    private final CachedHttpClient client;
    private final String baseFolder;
    private final String httpCacheFolder;
    private Map<String, String> changes = new HashMap<>();
    private Map<String, Boolean> hasEnvironmentConfigured = new HashMap<>();
    private boolean offline;
    private File versionFile;

    public SdkManApi(String baseFolder) {
        this.baseFolder = baseFolder;
        this.versionFile = new File(baseFolder, "ui" + separator + "version.txt");
        this.httpCacheFolder = baseFolder + separator + ".http_cache";
        this.client = new CachedHttpClient(httpCacheFolder, DEFAUL_CACHE_DURATION, newHttpClient());
    }

    public boolean isOffline() {
        return offline;
    }

    public void setOffline(boolean offline) {
        this.offline = offline;
    }

    public String getHttpCacheFolder() {
        return httpCacheFolder;
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

    public void changeLocal(String candidate, String toIdentifier) {
        changes.put(candidate, toIdentifier);
    }

    public void changeGlobal(String candidate, String toIdentifier) throws IOException {
        File toFolder = new File(baseFolder, "candidates" + separator + candidate + separator + toIdentifier);
        if (!toFolder.exists()) {
            throw new IllegalArgumentException("No such identifier for candidate " + candidate + ": " + toIdentifier);
        }
        File currentFolder = new File(baseFolder, "candidates" + separator + candidate + separator + "current");
        if (SdkManUiPreferences.getInstance().canCreateSymlink) {
            if (currentFolder.exists()) {
                currentFolder.delete();
            }
            Files.createSymbolicLink(currentFolder.toPath(), toFolder.toPath());
        } else {
            if (currentFolder.exists()) {
                FileUtil.deleteRecursively(currentFolder);
            }
            FileUtil.copyDirectory(toFolder.getAbsolutePath(), currentFolder.getAbsolutePath());
            writeCurrentVersionToFile(candidate, toIdentifier);
        }
    }

    private void writeCurrentVersionToFile(String candidate, String identifier) {
        File versionFile = new File(baseFolder, "candidates" + separator + candidate + separator + "current" + separator + ".sdkman-version");
        try {
            Files.writeString(versionFile.toPath(), identifier);
        } catch (IOException e) {
            throw new RuntimeException("Unable to write .sdkman-version file: " + e.getMessage(), e);
        }
    }

    public List<String> getLocalInstalledVersions(String candidate) {
        var candidatesFolder = new File(baseFolder + separator + "candidates" + separator + candidate);
        if (!candidatesFolder.exists()) {
            return List.of();
        }
        return List.of(Objects.requireNonNull(candidatesFolder.list((dir, name) ->
                new File(dir, name).isDirectory() && !"current".equals(name))));
    }

    private List<String> getLocalAvailableVersions(String candidate) {
        var archiveFolder = new File(baseFolder + separator + "archives");
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
        var paths = System.getenv("PATH");
        return findCandidateInPath(candidate, paths);
    }

    private String findCandidateInPath(String candidate, String paths) {
        var pathName = baseFolder + separator + "candidates" + separator + candidate;
        return isPathConfigured(pathName, paths);
    }

    private String[] updatePathForCandidate(String candidate, String identifier, String paths[]) {
        var pathName = baseFolder + separator + "candidates" + separator + candidate;
        return  Stream.of(paths).map(path -> {
            if (path.startsWith(pathName)) {
                return getCandidateFolder(candidate, identifier) + separator + "bin";
            }
            return path;
        }).toArray(String[]::new);
    }

    private String pathsToString(String paths[]) {
        return String.join(File.pathSeparator, Stream.of(paths).toList());
    }

    private String getCandidateFolder(String candidate, String identifier) {
        return baseFolder + separator + "candidates" + separator + candidate + separator + identifier;
    }

    private File getExitScriptFile() {
        if (OsHelper.hasShell()) {
            return new File(baseFolder, "tmp/exit-script.sh");
        } else {
            return new File(baseFolder, "tmp\\exit-script.cmd");
        }
    }

    public String resolveCurrentVersion(String candidate) throws IOException {
        var candidatesFolder = new File(baseFolder + separator + "candidates" + separator + candidate);
        File current = new File(candidatesFolder, "current");
        if (current.exists()) {
            String versionFromVersionFile = readCurrentVersionFromFile(candidate);
            if (versionFromVersionFile != null) {
                return versionFromVersionFile;
            }
            var realPath = current.toPath().toRealPath().toString();
            return realPath.substring(candidatesFolder.getAbsolutePath().length() + 1).replace(separator + "bin", "");
        }
        return null;
    }

    private String readCurrentVersionFromFile(String candidate) {
        File versionFile = new File(baseFolder, "candidates" + separator + candidate + separator + "current" + separator + ".sdkman-version");
        if (!versionFile.isFile()) {
            return null;
        }
        try {
            return Files.readString(versionFile.toPath());
        } catch (IOException e) {
            throw new RuntimeException("Unable to read .sdkman-version file: " + e.getMessage(), e);
        }
    }

    /**
     * Always create an exit file, as that is being called after the program exits.
     */
    public void registerShutdownHook() {
        Thread printingHook = new Thread(() -> {
            logger.debug("Creating exit scripts");
            File exitScriptFile = getExitScriptFile();
            try {
                if (!exitScriptFile.exists()) {
                    exitScriptFile.createNewFile();
                }
                final String exportCommand = OsHelper.isWindows() ? "set " : "export ";
                final String newLine = OsHelper.isWindows() ? "\r\n" : "\n";
                String[] paths = System.getenv("PATH").split(File.pathSeparator);
                try (var writer = Files.newBufferedWriter(getExitScriptFile().toPath())) {
                    for (Map.Entry<String, String> changesEntry : changes.entrySet()) {
                        var candidate = changesEntry.getKey();
                        var identifier = changesEntry.getValue();
                        logger.debug("Updating path entry for {}", changesEntry.getKey());
                        paths = updatePathForCandidate(candidate, identifier, paths);
                        writer.write(exportCommand + candidate.toUpperCase(Locale.ROOT) + "_HOME=" + getCandidateFolder(candidate, identifier) + newLine);
                        writer.write("echo Now using " + identifier + " for " + candidate + newLine);
                    }
                    writer.write(exportCommand + "PATH=" + pathsToString(paths) + newLine);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        Runtime.getRuntime().addShutdownHook(printingHook);
    }

    public DownloadTask download(String identifier, String version) {
        File finalArchiveFile = new File(baseFolder, "archives" + separator + identifier + "-" + version + ".zip");
        String url = BASE_URL + "/broker/download/" + identifier + "/" + version + "/" + getPlatformName();
        File tempFile = new File(baseFolder, "tmp" + separator + identifier + "-" + version + ".bin");
        return new DownloadTask(url, tempFile, finalArchiveFile, identifier + "-" + version);
    }

    public void install(String identifier, String version) {
        File archiveFile = new File(baseFolder, "archives" + separator + identifier + "-" + version + ".zip");
        ZipExtractTask.extract(archiveFile, new File(baseFolder, "candidates" + separator + identifier + separator + version));
    }

    public void uninstall(String identifier, String version) {
        File candidateFolder = new File(baseFolder, "candidates" + separator + identifier + separator + version);
        if (candidateFolder.exists()) {
            try {
                FileUtil.deleteRecursively(candidateFolder);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public boolean configureEnvironmentPath() {
        var pathName = baseFolder + separator + "ui";
        // Only a thing on windows (yet)
        if (OsHelper.isWindows()) {
            String globalPath = getGlobalPath();
            if (isPathConfigured(pathName, globalPath) == null) {
                OsHelper.setGlobalPath(pathName + File.pathSeparator + globalPath);
                return true;
            }
        }
        return false;
    }

    private String isPathConfigured(String pathName, String paths) {
        var pathsSplit = paths.split(File.pathSeparator);
        for (var p : pathsSplit) {
            if (p.startsWith(pathName)) {
                if (p.length() == pathName.length()) {
                    return p;
                }
                String nameWithBin = p.substring(pathName.length() + 1);
                int index = nameWithBin.lastIndexOf(File.separator);
                if (index != -1) {
                    return nameWithBin.substring(0, index);
                }
                return nameWithBin;
            }
        }
        return null;
    }

    public boolean hasCandidateEnvironmentPathConfigured(String candidate) {
        return hasEnvironmentConfigured.computeIfAbsent(candidate, (k) -> {
            if (OsHelper.isWindows()) {
                String globalPath = getGlobalPath();
                String candidateInPath = findCandidateInPath(candidate, globalPath);
                return candidateInPath != null;
            }
            // Only a thing on windows  (yet)
            return true;
        });
    }

    public void configureWindowsEnvironment(String candidate) {
        logger.debug("Configuring environment for {}", candidate);
        String path = getGlobalPath();
        path = baseFolder + "\\candidates\\" + candidate + "\\current\\bin;" + path;
        OsHelper.setGlobalPath(path);
        hasEnvironmentConfigured.put(candidate, true);
    }

    public String getBaseFolder() {
        return baseFolder;
    }

    public String getCurrentInstalledUIVersion() {
        if (versionFile.isFile()) {
            try {
                final List<String> lines = Files.readAllLines(versionFile.toPath());
                return lines.get(0);
            } catch (IOException e) {
                return null;
            }
        }
        return null;
    }
}
