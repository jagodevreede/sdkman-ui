package io.github.jagodevreede.sdkmanui;

import io.github.jagodevreede.sdkman.api.ProgressInformation;
import io.github.jagodevreede.sdkman.api.SdkManApi;
import io.github.jagodevreede.sdkman.api.domain.CandidateVersion;
import io.github.jagodevreede.sdkman.api.http.DownloadTask;
import io.github.jagodevreede.sdkmanui.service.ServiceRegistry;
import io.github.jagodevreede.sdkmanui.service.TaskRunner;
import io.github.jagodevreede.sdkmanui.view.PopupView;
import io.github.jagodevreede.sdkmanui.view.VersionView;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class MainScreenController implements Initializable {
    private static final Logger log = LoggerFactory.getLogger(MainScreenController.class);
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
    ProgressIndicator progressSpinner;

    ObservableList<VersionView> tableData;

    SdkManApi api;

    private String selectedCandidate;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        ServiceRegistry.INSTANCE.setProgressIndicator(progressSpinner);
        table.getColumns().clear();
        api = ServiceRegistry.INSTANCE.getApi();

        javaSelected();
        loadData();
        showInstalledOnly.selectedProperty().addListener((observable, oldValue, newValue) -> loadData());
        showAvailableOnly.selectedProperty().addListener((observable, oldValue, newValue) -> loadData());
    }

    public void loadData() {
        progressSpinner.setVisible(true);
        MainScreenController thiz = this;
        TaskRunner.run(() -> {
            try {
                String globalVersionInUse = api.resolveCurrentVersion(selectedCandidate);
                String pathVersionInUse = api.getCurrentCandidateFromPath(selectedCandidate);
                setGlobalVersionLabel(globalVersionInUse);
                setPathVersionLabel(pathVersionInUse);
                List<CandidateVersion> updatedVersions = api.getVersions(selectedCandidate).stream()
                        .filter(j -> !showInstalledOnly.isSelected() || j.installed())
                        .filter(j -> !showAvailableOnly.isSelected() || j.available())
                        .toList();
                if (tableData == null || tableData.size() != updatedVersions.size()) {
                    tableData = FXCollections.observableArrayList(
                            updatedVersions.stream()
                                    .map(j -> new VersionView(j, globalVersionInUse, pathVersionInUse, thiz)).toList()
                    );
                    Platform.runLater(() -> table.setItems(tableData));
                } else {
                    tableData.forEach(oldData -> {
                        var found = updatedVersions.stream().filter(j -> j.identifier().equals(oldData.getIdentifier())).findFirst();
                        if (found.isPresent()) {
                            oldData.update(found.get(), globalVersionInUse, pathVersionInUse);
                        } else {
                            log.error("Could not find version {}", oldData.getIdentifier());
                        }
                    });
                }
            } catch (IOException | InterruptedException e) {
                ServiceRegistry.INSTANCE.getPopupView().showError(e);
            }
        });
    }

    private void createColumns() {
        TableColumn<VersionView, String> vendorCol = getTableColumn("Vendor", "vendor");
        TableColumn<VersionView, String> versionCol = getTableColumn("Version", "version");
        TableColumn<VersionView, String> distCol = getTableColumn("Dist", "dist");
        TableColumn<VersionView, String> identifierCol = getTableColumn("Identifier", "identifier");
        TableColumn<VersionView, String> installedCol = getTableColumn("installed", "installed");
        TableColumn<VersionView, String> availableCol = getTableColumn("available", "available");
        TableColumn<VersionView, String> actionCol = getTableColumn("actions", "actions");

        table.getColumns().clear();
        if ("java".equals(selectedCandidate)) {
            table.getColumns().addAll(vendorCol, versionCol, distCol, identifierCol, installedCol, availableCol, actionCol);
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
        vendorCol.setCellValueFactory(
                new PropertyValueFactory<>(property)
        );
        return vendorCol;
    }

    public void javaSelected() {
        selectedCandidate = "java";
        if (tableData != null) {
            tableData.clear();
        }
        createColumns();
        loadData();
    }

    public void mavenSelected() {
        selectedCandidate = "maven";
        if (tableData != null) {
            tableData.clear();
        }
        createColumns();
        loadData();
    }

    public void downloadAndInstall(String identifier, String version) {
        download(identifier, version, true);
    }

    private void install(String identifier, String version) {
        PopupView.ProgressWindow progressWindow = ServiceRegistry.INSTANCE.getPopupView().showProgress("Extraction of " + identifier + " " + version + " in progress", null);
        TaskRunner.run(() -> {
            ServiceRegistry.INSTANCE.getApi().install(identifier, version);
            Platform.runLater(() -> {
                progressWindow.alert().close();
                loadData();
            });
        });
    }

    private void download(String identifier, String version, boolean install) {
        DownloadTask downloadTask = ServiceRegistry.INSTANCE.getApi().download(identifier, version);
        PopupView.ProgressWindow progressWindow = ServiceRegistry.INSTANCE.getPopupView().showProgress("Download of " + identifier + " " + version + " in progress", downloadTask);
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
        ServiceRegistry.INSTANCE.getApi().uninstall(identifier, version);
        Platform.runLater(this::loadData);
    }
}
