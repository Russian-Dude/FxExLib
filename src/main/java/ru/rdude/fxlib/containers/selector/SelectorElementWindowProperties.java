package ru.rdude.fxlib.containers.selector;

import javafx.beans.property.Property;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import ru.rdude.fxlib.boxes.SearchComboBox;

import java.util.Collection;
import java.util.function.Function;

public class SelectorElementWindowProperties<T, P extends Node> extends GridPane implements NamedSelectorElementNode<T> {

    private final SearchComboBox<T> searchComboBox = new SearchComboBox<>();
    private final Button button = new Button("✎");
    private final P propertiesNode;
    private final PropertiesWindow propertiesWindow;
    private StageStyle stageStyle = StageStyle.UTILITY;

    public SelectorElementWindowProperties(P propertiesNode) {
        super();
        this.propertiesNode = propertiesNode;
        this.propertiesWindow = new PropertiesWindow(propertiesNode);
        button.setOnAction(event -> propertiesWindow.showAndWait());

        searchComboBox.setMaxWidth(Double.MAX_VALUE);
        searchComboBox.setMinWidth(0);
        setFillWidth(searchComboBox, true);
        setHgrow(searchComboBox, Priority.ALWAYS);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setMinWidth(30);
        ColumnConstraints comboBoxConstraints = new ColumnConstraints();
        comboBoxConstraints.setFillWidth(true);
        ColumnConstraints buttonConstraints = new ColumnConstraints();
        buttonConstraints.setFillWidth(true);
        getColumnConstraints().addAll(comboBoxConstraints, buttonConstraints);
        addColumn(0, searchComboBox);
        addColumn(1, button);
    }

    public void setNameAndSearchBy(Function<T, String> function) {
        searchComboBox.setNameAndSearchBy(function);
    }

    public void setNameAndSearchByProperty(Function<T, ObservableValue<String>> function) {
        searchComboBox.setNameAndSearchByProperty(function);
    }

    public void setSearchBy(Function<T, String> function, Function<T, String>... functions) {
        searchComboBox.setSearchBy(function, functions);
    }

    public void setSearchByProperty(Function<T, ObservableValue<String>> function, Function<T, ObservableValue<String>>... functions) {
        searchComboBox.setSearchByProperty(function, functions);
    }

    public void setSearchBy(Collection<Function<T, String>> functions) {
        searchComboBox.setSearchBy(functions);
    }

    public void setSearchByProperty(Collection<Function<T, ObservableValue<String>>> functions) {
        searchComboBox.setSearchByProperty(functions);
    }

    public void setNameBy(Function<T, String> function) {
        searchComboBox.setNameBy(function);
    }

    public void setNameByProperty(Function<T, ObservableValue<String>> function) {
        searchComboBox.setNameByProperty(function);
    }

    public void setSearchEnabled(boolean searchEnabled) {
        searchComboBox.setSearchEnabled(searchEnabled);
    }

    public Button getButton() {
        return button;
    }

    public P getPropertiesNode() {
        return propertiesNode;
    }

    public void setStageStyle(StageStyle stageStyle) {
        this.stageStyle = stageStyle;
    }

    public PropertiesWindow getPropertiesWindow() {
        return propertiesWindow;
    }

    @Override
    public Property<T> valueProperty() {
        return searchComboBox.valueProperty();
    }

    @Override
    public T getValue() {
        return searchComboBox.getValue();
    }

    @Override
    public void setValue(T t) {
        searchComboBox.setValue(t);
    }

    @Override
    public void setCollection(Collection<T> collection) {
        searchComboBox.setCollection(collection);
    }

    public class PropertiesWindow extends Stage {

        public PropertiesWindow(P inside) {
            super(StageStyle.UTILITY);
            initModality(Modality.APPLICATION_MODAL);
            Scene scene = new Scene(new AnchorPane(inside));
            this.setScene(scene);
        }
    }
}
