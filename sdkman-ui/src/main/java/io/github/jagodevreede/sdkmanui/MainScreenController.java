package io.github.jagodevreede.sdkmanui;

import io.github.jagodevreede.sdkman.api.SdkManApi;
import io.github.jagodevreede.sdkmanui.service.ServiceRegistry;
import io.github.jagodevreede.sdkmanui.service.TaskRunner;
import io.github.jagodevreede.sdkmanui.view.JavaVersionView;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainScreenController implements Initializable {
    @FXML
    TableView<JavaVersionView> table;
    @FXML
    Label selected_item_label;
    @FXML
    Label global_version_label;
    @FXML
    CheckBox showInstalledOnly;
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
        TableColumn<JavaVersionView, String> actionCol = getTableColumn("actions", "actions");

        table.getColumns().addAll(vendorCol, lastNameCol, emailCol, identifierCol, installedCol, actionCol);
        loadData();
        showInstalledOnly.selectedProperty().addListener((observable, oldValue, newValue) -> loadData());
    }

    public void loadData() {
        if (tableData != null) {
            tableData.clear();
        }
        progressSpinner.setVisible(true);
        MainScreenController thiz = this;
        TaskRunner.run(new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    String javaGlobalVersionInUse = api.resolveCurrentVersion("java");
                    String javaPathVersionInUse = api.getCurrentCandidateFromPath("java");
                    setGlobalVersionLabel(javaGlobalVersionInUse);
                    setPathVersionLabel(javaPathVersionInUse);
                    tableData = FXCollections.observableArrayList(
                            api.getJavaVersions().stream()
                                    .filter(j -> !showInstalledOnly.isSelected() || j.installed())
                                    .map(j -> new JavaVersionView(j, javaGlobalVersionInUse, javaPathVersionInUse, thiz)).toList()
                    );

                    table.setItems(tableData);
                    progressSpinner.setVisible(false);
                } catch (IOException | InterruptedException e) {
                    ServiceRegistry.INSTANCE.getPopupView().showError(e);
                }
                return null;
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

}
