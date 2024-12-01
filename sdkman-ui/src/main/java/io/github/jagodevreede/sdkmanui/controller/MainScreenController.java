package io.github.jagodevreede.sdkmanui.controller;

import io.github.jagodevreede.sdkman.api.OsHelper;
import io.github.jagodevreede.sdkman.api.ProgressInformation;
import io.github.jagodevreede.sdkman.api.SdkManApi;
import io.github.jagodevreede.sdkman.api.SdkManUiPreferences;
import io.github.jagodevreede.sdkman.api.domain.Candidate;
import io.github.jagodevreede.sdkman.api.domain.CandidateVersion;
import io.github.jagodevreede.sdkman.api.http.DownloadTask;
import io.github.jagodevreede.sdkmanui.ApplicationVersion;
import io.github.jagodevreede.sdkmanui.Main;
import io.github.jagodevreede.sdkmanui.service.ServiceRegistry;
import io.github.jagodevreede.sdkmanui.service.TaskRunner;
import io.github.jagodevreede.sdkmanui.updater.AutoUpdater;
import io.github.jagodevreede.sdkmanui.view.PopupView;
import io.github.jagodevreede.sdkmanui.view.VersionView;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

import static io.github.jagodevreede.sdkmanui.view.Images.appIcon;

public class MainScreenController implements Initializable {
    private static MainScreenController INSTANCE;
    private static final Logger logger = LoggerFactory.getLogger(MainScreenController.class);
    private final SdkManApi api = ServiceRegistry.INSTANCE.getApi();
    private final PopupView popupView = ServiceRegistry.INSTANCE.getPopupView();
    private final SdkManUiPreferences sdkManUiPreferences = ServiceRegistry.INSTANCE.getSdkManUiPreferences();
    @FXML
    TableView<VersionView> table;
    @FXML
    Label selected_item_label;
    @FXML
    Label global_version_label;
    @FXML
    CheckBox showInstalledOnly;
    @FXML
    CheckBox showAvailableOnly;
    @FXML
    TextField searchField;
    @FXML
    ProgressIndicator progressSpinner;
    @FXML
    Pane updatePane;
    @FXML
    Label updateLabel;
    @FXML
    AnchorPane candidateListPane;
    @FXML
    Pane toastBar;
    @FXML
    Label toastLabel;

    private final PauseTransition searchFieldPause = new PauseTransition(Duration.millis(300));
    private ObservableList<VersionView> tableData;
    private String selectedCandidate;
    private Stage stage;

    public String getSelectedCandidate() {
        return selectedCandidate;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        ServiceRegistry.INSTANCE.setProgressIndicator(progressSpinner);
        showInstalledOnly.setSelected(sdkManUiPreferences.showInstalled);
        showAvailableOnly.setSelected(sdkManUiPreferences.showAvailable);
        table.setPlaceholder(new Label("No versions found"));
        table.getColumns().clear();

        switchCandidate("java");
        showInstalledOnly.selectedProperty().addListener((observable, oldValue, newValue) -> {
            sdkManUiPreferences.showInstalled = newValue;
            sdkManUiPreferences.saveQuite();
            loadData();
        });
        showAvailableOnly.selectedProperty().addListener((observable, oldValue, newValue) -> {
            sdkManUiPreferences.showAvailable = newValue;
            sdkManUiPreferences.saveQuite();
            loadData();
        });
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            searchFieldPause.setOnFinished(event -> loadData());
            searchFieldPause.playFromStart();
        });
    }

    public void loadData() {
        loadData(null);
    }

    public void loadData(Runnable onLoaded) {
        progressSpinner.setVisible(true);
        MainScreenController thiz = this;
        TaskRunner.run(() -> {
            try {
                String globalVersionInUse = api.resolveCurrentVersion(selectedCandidate);
                String pathVersionInUse = api.getCurrentCandidateFromPath(selectedCandidate);
                setGlobalVersionLabel(globalVersionInUse);
                setPathVersionLabel(pathVersionInUse);
                List<CandidateVersion> updatedVersions = api.getVersions(selectedCandidate)
                        .stream()
                        .filter(j -> !showInstalledOnly.isSelected() || j.installed())
                        .filter(j -> !showAvailableOnly.isSelected() || j.available())
                        .filter(this::isCandidateVersionIncludedInSearch)
                        .toList();
                Platform.runLater(() -> {
                    showAvailableOnly.setVisible(sdkManUiPreferences.keepDownloadsAvailable);
                    if (tableData == null || tableData.size() != updatedVersions.size()) {
                        tableData = FXCollections.observableArrayList(updatedVersions.stream()
                                .map(j -> new VersionView(j, globalVersionInUse, pathVersionInUse, thiz))
                                .toList());
                        table.setItems(tableData);
                    } else {
                        tableData.forEach(oldData -> {
                            var found = updatedVersions.stream()
                                    .filter(j -> j.identifier().equals(oldData.getIdentifier()))
                                    .findFirst();
                            if (found.isPresent()) {
                                oldData.update(found.get(), globalVersionInUse, pathVersionInUse);
                            } else {
                                logger.error("Could not find version {}", oldData.getIdentifier());
                            }
                        });
                    }
                    if (onLoaded != null) {
                        onLoaded.run();
                    }
                });
            } catch (IOException | InterruptedException e) {
                popupView.showError(e);
            }
        });
    }


    boolean isCandidateVersionIncludedInSearch(CandidateVersion j) {
        if (searchField == null) {
            return true;
        }
        return isCandidateVersionIncludedInSearch(searchField.getText(), j);
    }

    // Default scope so we can unit test this more easily
    boolean isCandidateVersionIncludedInSearch(String searchTerm, CandidateVersion j) {
        if (searchTerm == null || searchTerm.isBlank()) {
            return true;
        } else {
            String[] searchStrings = searchTerm.trim().split("\\s");
            return Arrays.stream(searchStrings).allMatch(s -> {
                Pattern searchPattern = Pattern.compile(Pattern.quote(s), Pattern.CASE_INSENSITIVE);
                final boolean vendorMatchesSearch = j.vendor() != null && searchPattern
                        .matcher(j.vendor())
                        .find();
                final boolean identifierMatchesSearch = j.identifier() != null && searchPattern
                        .matcher(j.identifier())
                        .find();
                final boolean versionMatchesSearch = j.version() != null && searchPattern
                        .matcher(j.version())
                        .find();
                return vendorMatchesSearch || identifierMatchesSearch || versionMatchesSearch;
            });
        }
    }

    private void createColumns() {
        TableColumn<VersionView, String> vendorCol = getTableColumn("Vendor", "vendor");
        vendorCol.setPrefWidth(105.0);
        TableColumn<VersionView, String> versionCol = getTableColumn("Version", "version");
        versionCol.setPrefWidth(80.0);
        TableColumn<VersionView, String> identifierCol = getTableColumn("Identifier", "identifier");
        identifierCol.setPrefWidth(120.0);
        TableColumn<VersionView, String> installedCol = getTableColumn("Installed", "installed");
        installedCol.setPrefWidth(70.0);
        TableColumn<VersionView, String> availableCol = getTableColumn("Available", "available");
        availableCol.setPrefWidth(70.0);
        TableColumn<VersionView, String> actionCol = getTableColumn("Actions", "actions");
        actionCol.setPrefWidth(110.0);

        table.getColumns().clear();
        if ("java".equals(selectedCandidate)) {
            table.getColumns().addAll(vendorCol, versionCol, identifierCol, installedCol);
        } else {
            table.getColumns().addAll(versionCol, installedCol);
        }
        if (sdkManUiPreferences.keepDownloadsAvailable) {
            table.getColumns().add(availableCol);
        }
        table.getColumns().add(actionCol);
    }

    private void setPathVersionLabel(String pathVersionInUse) {
        Platform.runLater(() -> {
            if (pathVersionInUse != null) {
                if ("current".equals(pathVersionInUse)) {
                    selected_item_label.setText("Using global version in shell");
                } else {
                    selected_item_label.setText("Using " + pathVersionInUse + " in shell");
                }
            } else {
                selected_item_label.setText("No path version in use");
            }
        });
    }

    private void setGlobalVersionLabel(String globalVersionInUse) {
        Platform.runLater(() -> {
            if (globalVersionInUse != null) {
                global_version_label.setText("Global version: " + globalVersionInUse);
            } else {
                global_version_label.setText("No global version in use");
            }
        });
    }

    private static TableColumn<VersionView, String> getTableColumn(String title, String property) {
        TableColumn<VersionView, String> vendorCol = new TableColumn<>(title);
        vendorCol.setCellValueFactory(new PropertyValueFactory<>(property));
        return vendorCol;
    }

    public void openConfig() throws IOException {
        URL configFxml = Main.class.getClassLoader().getResource("config.fxml");
        Parent root = FXMLLoader.load(configFxml);

        Scene scene = new Scene(root, 600, 400);
        Stage stage = new Stage();

        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(table.getScene().getWindow());
        stage.setScene(scene);
        stage.setTitle("SDKman UI - Configuration");
        stage.setResizable(false);
        stage.show();
    }

    private void switchCandidate(String candidate) {
        selectedCandidate = candidate;
        if (tableData != null) {
            tableData.clear();
        }
        createColumns();
        loadData(() -> checkIfEnvironmentIsConfigured(selectedCandidate));
    }

    private void checkIfEnvironmentIsConfigured(String candidate) {
        // Only on windows, check if the environment is configured
        if (OsHelper.isWindows() && hasInstalledVersion()) {
            if (!api.hasCandidateEnvironmentPathConfigured(candidate)) {
                Platform.runLater(() -> popupView.showConfirmation("Configure environment for " + candidate, candidate + " is not in the environment (path variable) yet, do you want to add it?", () -> {
                    api.configureWindowsEnvironment(candidate);
                }));
            }
            if (!api.hasCandidateEnvironmentHomeConfigured(candidate)) {
                Platform.runLater(() -> popupView.showConfirmation("Configure environment for " + candidate, candidate.toUpperCase() + "_HOME is not in the environment yet, do you want to add it?", () -> {
                    api.configureEnvironmentHome(candidate);
                }));
            }
        }
    }

    private boolean hasInstalledVersion() {
        if (tableData == null) {
            return false;
        }
        return this.tableData.stream().anyMatch(VersionView::isInstalled);
    }

    private void install(String identifier, String version) {
        PopupView.ProgressWindow progressWindow = popupView.showProgress("Extraction of " + identifier + " " + version + " in progress", null);
        TaskRunner.run(() -> {
            api.install(identifier, version);
            Platform.runLater(() -> {
                progressWindow.alert().close();
                loadData(() -> checkIfEnvironmentIsConfigured(selectedCandidate));
            });
        });
    }

    public void download(String identifier, String version, boolean install) {
        DownloadTask downloadTask = api.download(identifier, version);
        PopupView.ProgressWindow progressWindow = popupView.showProgress("Download of " + identifier + " " + version + " in progress", downloadTask);
        ProgressInformation progressInformation = new ProgressInformation() {
            @Override
            public void publishProgress(int current) {
                if (current > 0) {
                    Platform.runLater(() -> progressWindow.progressBar().setProgress(current / 100.0));
                } else {
                    Platform.runLater(() -> progressWindow.progressBar().setProgress(-1));
                }
            }

            @Override
            public void publishState(String state) {
                Platform.runLater(() -> progressWindow.alert().setHeaderText(state));
            }
        };
        downloadTask.setProgressInformation(progressInformation);
        TaskRunner.run(() -> {
            downloadTask.download();

            Platform.runLater(() -> {
                progressWindow.alert().close();
                if (install && !downloadTask.isCancelled()) {
                    install(identifier, version);
                } else {
                    loadData();
                }
            });
        });
    }

    public void uninstall(String identifier, String version) {
        api.uninstall(identifier, version);
        Platform.runLater(this::loadData);
    }

    public void setUpdateAvailable(String latestRelease) {
        Platform.runLater(() -> {
            updatePane.setVisible(true);
            updateLabel.setText("Update available: " + latestRelease);
        });
    }

    public void setCandidates(List<Candidate> candidateList) {
        List<Candidate> candidates = new ArrayList<>(candidateList);
        final double prefHeight = 30.0;
        final double prefPadding = 5.0;
        int count = 0;
        candidateListPane.setPrefHeight((prefHeight + prefPadding) * candidateList.size() + 5.0);
        candidateListPane.getChildren().clear();
        List<String> localInstalledCandidates = api.getLocalInstalledCandidates();
        for (String candidateId : localInstalledCandidates) {
            Candidate candidate = candidates.stream()
                    .filter(c -> c.id().equals(candidateId))
                    .findFirst()
                    .orElse(new Candidate(candidateId, candidateId, ""));
            addCandateToScrollPane(candidate, count, prefHeight, prefPadding);
            count++;
        }

        for (Candidate candidate : candidates) {
            if (localInstalledCandidates.contains(candidate.id())) {
                // We already added it
                continue;
            }
            addCandateToScrollPane(candidate, count, prefHeight, prefPadding);
            count++;
        }
    }

    private void addCandateToScrollPane(Candidate candidate, int count, double prefHeight, double prefPadding) {
        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER_LEFT);
        hBox.setLayoutX(4.0);
        hBox.setLayoutY(count * (prefHeight + prefPadding));
        hBox.setPrefHeight(prefHeight);
        hBox.setPrefWidth(175.0);
        hBox.getStyleClass().add("sidebar-button");
        hBox.setCursor(Cursor.HAND);
        hBox.setPadding(new Insets(5.0));

        Label label = new Label();
        label.setPrefHeight(17.0);
        label.setPrefWidth(115.0);
        label.setTranslateX(5.0);
        label.setStyle("-fx-text-fill: white;");
        label.setText(candidate.name());
        label.setTextFill(Color.WHITE);
        hBox.getChildren().add(label);

        EventHandler<MouseEvent> mouseEventConsumer = (MouseEvent event) -> {
            switchCandidate(candidate.id());
            event.consume();
        };
        hBox.setOnMouseClicked(mouseEventConsumer);
        candidateListPane.getChildren().add(hBox);
    }

    public void showToast(String message) {
        Platform.runLater(() -> {
            toastBar.setVisible(true);
            toastLabel.setText(message);
            new Thread(() -> {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Platform.runLater(() -> {
                    toastBar.setVisible(false);
                });
            }).start();
        });
    }

    public static synchronized MainScreenController getInstance() {
        if (INSTANCE == null) {
            try {
                URL mainFxml = MainScreenController.class.getClassLoader().getResource("main.fxml");
                FXMLLoader loader = new FXMLLoader(mainFxml);
                Parent root = loader.load();
                MainScreenController controller = loader.getController();
                Scene scene = new Scene(root, 800, 580);
                Stage stage = new Stage();
                stage.setScene(scene);
                stage.setResizable(false);
                ApplicationVersion applicationVersion = ApplicationVersion.INSTANCE;
                stage.setTitle("SDKman UI - " + applicationVersion.getVersion() + " (" + applicationVersion.getCommitHash() + ")");
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.show();
                stage.getIcons().add(appIcon);
                controller.setStage(stage);
                INSTANCE = controller;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return INSTANCE;
    }

    private void setStage(Stage stage) {
        this.stage = stage;
        stage.addEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST, this::closeWindowEvent);
    }

    private void closeWindowEvent(WindowEvent windowEvent) {
        api.createExitScripts();
    }

    public void startUpdate() {
        AutoUpdater.getInstance().ifPresent(AutoUpdater::runUpdate);
    }

    public void exitApplication() {
        // stage close will not fire close event so we need to fire it manually
        closeWindowEvent(null);
        // Don't use Platform.exit() as in native the shutdown hook will not fire (on osx)
        Platform.runLater(() -> stage.close());
    }
}
