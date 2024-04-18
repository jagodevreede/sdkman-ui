package io.github.jagodevreede.sdkmanui;

import io.github.jagodevreede.sdkman.api.ProgressInformation;
import io.github.jagodevreede.sdkman.api.SdkManApi;
import io.github.jagodevreede.sdkman.api.http.DownloadTask;
import io.github.jagodevreede.sdkman.api.http.UnzipTask;
import io.github.jagodevreede.sdkmanui.service.ServiceRegistry;
import io.github.jagodevreede.sdkmanui.service.TaskRunner;
import io.github.jagodevreede.sdkmanui.view.JavaVersionView;
import io.github.jagodevreede.sdkmanui.view.PopupView;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainScreenController implements Initializable {
    private static final Logger log = LoggerFactory.getLogger(MainScreenController.class);
    @FXML
    TableView<JavaVersionView> table;
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

    ObservableList<JavaVersionView> tableData;

    SdkManApi api;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        ServiceRegistry.INSTANCE.setProgressIndicator(progressSpinner);
        table.getColumns().clear();
        api = ServiceRegistry.INSTANCE.getApi();

        TableColumn<JavaVersionView, String> vendorCol = getTableColumn("Vendor", "vendor");
        TableColumn<JavaVersionView, String> lastNameCol = getTableColumn("Version", "version");
        TableColumn<JavaVersionView, String> emailCol = getTableColumn("Dist", "dist");
        TableColumn<JavaVersionView, String> identifierCol = getTableColumn("Identifier", "identifier");
        TableColumn<JavaVersionView, String> installedCol = getTableColumn("installed", "installed");
        TableColumn<JavaVersionView, String> availableCol = getTableColumn("available", "available");
        TableColumn<JavaVersionView, String> actionCol = getTableColumn("actions", "actions");

        table.getColumns().addAll(vendorCol, lastNameCol, emailCol, identifierCol, installedCol, availableCol, actionCol);
        loadData();
        showInstalledOnly.selectedProperty().addListener((observable, oldValue, newValue) -> loadData());
        showAvailableOnly.selectedProperty().addListener((observable, oldValue, newValue) -> loadData());
    }

    public void loadData() {
        double currentScroll = 0;
        if (tableData != null) {
            ScrollBar verticalBar = (ScrollBar) table.lookup(".scroll-bar:vertical");
            if (verticalBar != null) {
                currentScroll = verticalBar.valueProperty().get();
            }
            tableData.clear();
        }
        progressSpinner.setVisible(true);
        MainScreenController thiz = this;
        final double finalCurrentScroll = currentScroll;
        log.info("current scroll {}", currentScroll);
        TaskRunner.run(() -> {
            try {
                String javaGlobalVersionInUse = api.resolveCurrentVersion("java");
                String javaPathVersionInUse = api.getCurrentCandidateFromPath("java");
                setGlobalVersionLabel(javaGlobalVersionInUse);
                setPathVersionLabel(javaPathVersionInUse);
                tableData = FXCollections.observableArrayList(
                        api.getJavaVersions().stream()
                                .filter(j -> !showInstalledOnly.isSelected() || j.installed())
                                .filter(j -> !showAvailableOnly.isSelected() || j.available())
                                .map(j -> new JavaVersionView(j, javaGlobalVersionInUse, javaPathVersionInUse, thiz)).toList()
                );
                Platform.runLater(() -> {
                    table.setItems(tableData);
                    ScrollBar verticalBar = (ScrollBar) table.lookup(".scroll-bar:vertical");
                    if (verticalBar != null) {
                        log.info(" scrollbar");
                        if (verticalBar.getMax() < finalCurrentScroll) {
                            log.info("scrolling to {}", finalCurrentScroll);
                            Platform.runLater(() -> verticalBar.setValue(finalCurrentScroll));
                        }
                    }
                });
            } catch (IOException | InterruptedException e) {
                ServiceRegistry.INSTANCE.getPopupView().showError(e);
            }
        });
    }

    private void setPathVersionLabel(String javaPathVersionInUse) {
        Platform.runLater(() -> {
            if (javaPathVersionInUse != null) {
                if ("current".equals(javaPathVersionInUse)) {
                    selected_item_label.setText("Using global version in shell");
                } else {
                    selected_item_label.setText("Using " + javaPathVersionInUse + " in shell");
                }
            } else {
                selected_item_label.setText("No path version in use");
            }
        });
    }

    private void setGlobalVersionLabel(String javaGlobalVersionInUse) {
        Platform.runLater(() -> {
            if (javaGlobalVersionInUse != null) {
                global_version_label.setText(javaGlobalVersionInUse);
            } else {
                global_version_label.setText("No global version in use");
            }
        });
    }

    private static TableColumn<JavaVersionView, String> getTableColumn(String title, String property) {
        TableColumn<JavaVersionView, String> vendorCol = new TableColumn<>(title);
        vendorCol.setCellValueFactory(
                new PropertyValueFactory<>(property)
        );
        return vendorCol;
    }

    public void javaSelected(ActionEvent event) {
        //  table.set
    }

    public void mavenSelected(ActionEvent event) {

    }

    public void downloadAndInstall(String identifier, String version) {
        download(identifier, version, true);
    }

    private void install(String identifier, String version) {
        UnzipTask unzipTask = ServiceRegistry.INSTANCE.getApi().install(identifier, version);
        PopupView.ProgressWindow progressWindow = ServiceRegistry.INSTANCE.getPopupView().showProgress("Extraction of " + identifier + " " + version + " in progress", unzipTask);
        ProgressInformation progressInformation = current -> {
            if (current > 0) {
                Platform.runLater(() -> progressWindow.progressBar().setProgress(current / 100.0));
            }
        };
        unzipTask.setProgressInformation(progressInformation);
        TaskRunner.run(() -> {
            unzipTask.unzip();
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
                if (install) {
                    install(identifier, version);
                }
            });
        });
    }

    public void uninstall(String identifier, String version) {
        ServiceRegistry.INSTANCE.getApi().uninstall(identifier, version);
        Platform.runLater(this::loadData);
    }
}
