package io.github.jagodevreede.sdkmanui.controller;

import io.github.jagodevreede.sdkman.api.OsHelper;
import io.github.jagodevreede.sdkman.api.ProgressInformation;
import io.github.jagodevreede.sdkman.api.SdkManApi;
import io.github.jagodevreede.sdkman.api.SdkManUiPreferences;
import io.github.jagodevreede.sdkman.api.domain.CandidateVersion;
import io.github.jagodevreede.sdkman.api.http.DownloadTask;
import io.github.jagodevreede.sdkmanui.ApplicationVersion;
import io.github.jagodevreede.sdkmanui.Main;
import io.github.jagodevreede.sdkmanui.service.ServiceRegistry;
import io.github.jagodevreede.sdkmanui.service.TaskRunner;
import io.github.jagodevreede.sdkmanui.view.PopupView;
import io.github.jagodevreede.sdkmanui.view.VersionView;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

import static io.github.jagodevreede.sdkmanui.view.Images.appIcon;

public class MainScreenController implements Initializable {
    private static MainScreenController INSTANCE = getInstance();
    private static final Logger logger = LoggerFactory.getLogger(MainScreenController.class);
    private final SdkManApi api = ServiceRegistry.INSTANCE.getApi();
    private final PopupView popupView = ServiceRegistry.INSTANCE.getPopupView();
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

    private final PauseTransition searchFieldPause = new PauseTransition(Duration.millis(300));
    private ObservableList<VersionView> tableData;
    private String selectedCandidate;

    public String getSelectedCandidate() {
        return selectedCandidate;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        ServiceRegistry.INSTANCE.setProgressIndicator(progressSpinner);
        final SdkManUiPreferences sdkManUiPreferences = ServiceRegistry.INSTANCE.getSdkManUiPreferences();
        showInstalledOnly.setSelected(sdkManUiPreferences.showInstalled);
        showAvailableOnly.setSelected(sdkManUiPreferences.showAvailable);
        table.getColumns().clear();

        javaSelected();
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
                List<CandidateVersion> updatedVersions = api.getVersions(selectedCandidate).stream().filter(j -> !showInstalledOnly.isSelected() || j.installed()).filter(j -> !showAvailableOnly.isSelected() || j.available()).filter(j -> {
                    if (searchField == null || searchField.getText() == null || searchField.getText().isBlank()) {
                        return true;
                    } else {
                        final boolean vendorMatchesSearch = Pattern.compile(Pattern.quote(searchField.getText()), Pattern.CASE_INSENSITIVE).matcher(j.vendor()).find();
                        final boolean identifierMatchesSearch = Pattern.compile(Pattern.quote(searchField.getText()), Pattern.CASE_INSENSITIVE).matcher(j.identifier()).find();
                        return vendorMatchesSearch || identifierMatchesSearch;
                    }
                }).toList();
                Platform.runLater(() -> {
                    if (tableData == null || tableData.size() != updatedVersions.size()) {
                        tableData = FXCollections.observableArrayList(updatedVersions.stream().map(j -> new VersionView(j, globalVersionInUse, pathVersionInUse, thiz)).toList());
                        table.setItems(tableData);
                    } else {
                        tableData.forEach(oldData -> {
                            var found = updatedVersions.stream().filter(j -> j.identifier().equals(oldData.getIdentifier())).findFirst();
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
            table.getColumns().addAll(vendorCol, versionCol, identifierCol, installedCol, availableCol, actionCol);
        } else {
            table.getColumns().addAll(versionCol, installedCol, availableCol, actionCol);
        }
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
                global_version_label.setText(globalVersionInUse);
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

    public void javaSelected() {
        switchCandidate("java");
    }

    public void mavenSelected() {
        switchCandidate("maven");
    }

    public void openConfig() throws IOException {
        URL configFxml = Main.class.getClassLoader().getResource("config.fxml");
        Parent root = FXMLLoader.load(configFxml);

        Scene scene = new Scene(root, 600, 400);
        Stage stage = new Stage();

        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(table.getScene().getWindow());
        stage.setScene(scene);
        stage.setTitle("SDKMAN UI - Configuration");
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
        ProgressInformation progressInformation = current -> {
            if (current > 0) {
                Platform.runLater(() -> progressWindow.progressBar().setProgress(current / 100.0));
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

                stage.setTitle("SDKMAN UI - " + ApplicationVersion.INSTANCE.getVersion());
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.show();
                stage.getIcons().add(appIcon);
                INSTANCE = controller;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return INSTANCE;
    }

}
