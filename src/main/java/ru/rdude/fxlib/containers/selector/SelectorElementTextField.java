package ru.rdude.fxlib.containers.selector;

import javafx.beans.property.Property;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import ru.rdude.fxlib.boxes.SearchComboBox;

import java.util.Collection;
import java.util.function.Function;

public class SelectorElementTextField<T> extends GridPane implements SelectorElementNode<T> {

    private final SearchComboBox<T> searchComboBox = new SearchComboBox<>();
    private final TextField textField = new TextField();

    public SelectorElementTextField() {
        super();
        searchComboBox.setMaxWidth(Double.MAX_VALUE);
        searchComboBox.setMinWidth(0);
        textField.setMaxWidth(Double.MAX_VALUE);
        textField.setMinWidth(0);
        ColumnConstraints comboBoxConstraints = new ColumnConstraints();
        comboBoxConstraints.setPercentWidth(30);
        comboBoxConstraints.setFillWidth(true);
        ColumnConstraints textFieldConstraints = new ColumnConstraints();
        textFieldConstraints.setPercentWidth(70);
        textFieldConstraints.setFillWidth(true);
        getColumnConstraints().addAll(comboBoxConstraints, textFieldConstraints);
        addColumn(0, searchComboBox);
        addColumn(1, textField);
    }

    public StringProperty getTextFieldProperty() {
        return textField.textProperty();
    }

    public SearchComboBox<T> getSearchComboBox() {
        return searchComboBox;
    }

    public TextField getTextField() {
        return textField;
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

    public final void setSizePercentages(double comboBox, double textField) {
        getColumnConstraints().get(0).setPercentWidth(comboBox);
        getColumnConstraints().get(1).setPercentWidth(textField);
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

}
