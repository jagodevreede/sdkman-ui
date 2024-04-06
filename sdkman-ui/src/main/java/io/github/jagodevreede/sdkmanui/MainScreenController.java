package io.github.jagodevreede.sdkmanui;

import io.github.jagodevreede.sdkman.api.domain.JavaVersion;
import io.github.jagodevreede.sdkmanui.view.JavaVersionView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.ResourceBundle;

public class MainScreenController implements Initializable {
    @FXML
    TableView<JavaVersionView> table;

    ObservableList<JavaVersionView> tableData;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        tableData = FXCollections.observableArrayList(
                new JavaVersionView(new JavaVersion("Jacob", "22", "jacob", "j22", false)),
                new JavaVersionView(new JavaVersion("Jacob", "17", "jacob", "j17", true))
        );

        TableColumn<JavaVersionView, String> vendorCol = getTableColumn("Vendor", "vendor");
        TableColumn<JavaVersionView, String> lastNameCol = getTableColumn("Version", "version");
        TableColumn<JavaVersionView, String> emailCol = getTableColumn("Dist", "dist");
        TableColumn<JavaVersionView, String> identifierCol = getTableColumn("Identifier", "identifier");
        TableColumn<JavaVersionView, String> installedCol = getTableColumn("installed", "installed");
        TableColumn<JavaVersionView, String> actionCol = getTableColumn("actions", "actions");

        table.getColumns().clear();
        table.getColumns().addAll(vendorCol, lastNameCol, emailCol, identifierCol, installedCol, actionCol);

        table.setItems(tableData);
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
