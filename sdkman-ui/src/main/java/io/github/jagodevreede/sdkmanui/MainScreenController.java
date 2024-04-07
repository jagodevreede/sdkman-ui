package io.github.jagodevreede.sdkmanui;

import io.github.jagodevreede.sdkman.api.SdkManApi;
import io.github.jagodevreede.sdkmanui.service.ServiceRegistry;
import io.github.jagodevreede.sdkmanui.view.JavaVersionView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
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

    ObservableList<JavaVersionView> tableData;

    SdkManApi api;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        table.getColumns().clear();

        api = ServiceRegistry.INSTANCE.getApi();
        try {
            String javaGlobalVersionInUse = api.resolveCurrentVersion("java");
            setGlobalVersionLabel(javaGlobalVersionInUse);
            tableData = FXCollections.observableArrayList(
                    api.getJavaVersions().stream().map(j -> new JavaVersionView(j, javaGlobalVersionInUse)).toList()
            );

            TableColumn<JavaVersionView, String> vendorCol = getTableColumn("Vendor", "vendor");
            TableColumn<JavaVersionView, String> lastNameCol = getTableColumn("Version", "version");
            TableColumn<JavaVersionView, String> emailCol = getTableColumn("Dist", "dist");
            TableColumn<JavaVersionView, String> identifierCol = getTableColumn("Identifier", "identifier");
            TableColumn<JavaVersionView, String> installedCol = getTableColumn("installed", "installed");
            TableColumn<JavaVersionView, String> actionCol = getTableColumn("actions", "actions");

            table.getColumns().addAll(vendorCol, lastNameCol, emailCol, identifierCol, installedCol, actionCol);

            table.setItems(tableData);
        } catch (IOException | InterruptedException e) {
            ServiceRegistry.INSTANCE.getPopupView().showError(e);
        }
    }

    private void setGlobalVersionLabel(String javaGlobalVersionInUse) {
        if (javaGlobalVersionInUse != null) {
            global_version_label.setText(javaGlobalVersionInUse);
        } else {
            global_version_label.setText("No global version in use");
        }
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
