package ru.rdude.fxlib.containers.selector;

import javafx.beans.property.Property;
import javafx.beans.property.StringProperty;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import org.intellij.lang.annotations.RegExp;
import ru.rdude.fxlib.boxes.SearchComboBox;
import ru.rdude.fxlib.textfields.AutocompletionTextField;

import java.util.Collection;
import java.util.function.Function;

public class SelectorElementAutocompletionTextField<T, V> extends GridPane implements SelectorElementNode<T> {

    private final SearchComboBox<T> searchComboBox = new SearchComboBox<>();
    private final AutocompletionTextField<V> textField = new AutocompletionTextField<>();

    public SelectorElementAutocompletionTextField() {
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

    public void setNameBy(Function<T, String> function) {
        searchComboBox.setNameBy(function);
    }

    public void setSearchBy(Function<T, String> function, Function<T, String>... functions) {
        searchComboBox.setSearchBy(function, functions);
    }

    public void setSearchBy(Collection<Function<T, String>> functions) {
        searchComboBox.setSearchBy(functions);
    }

    public void setSearchEnabled(boolean searchEnabled) {
        searchComboBox.setSearchEnabled(searchEnabled);
    }

    public final void setSizePercentages(double comboBox, double textField) {
        getColumnConstraints().get(0).setPercentWidth(comboBox);
        getColumnConstraints().get(1).setPercentWidth(textField);
    }

    public void setTextFieldTypeType(AutocompletionTextField.Type type) {
        textField.setType(type);
    }

    public void setTextFieldNameBy(Function<V, String> elementNameFunction) {
        textField.setNameBy(elementNameFunction);
    }

    public void setWordsDelimiter(@RegExp String regex) {
        textField.setWordsDelimiter(regex);
    }

    public void setText(String value) {
        textField.setText(value);
    }

    public void setTextFieldElements(Collection<V> elements) {
        textField.setCollection(elements);
    }

    public void setTextFieldDescriptionFunction(Function<V, String> elementDescriptionFunction) {
        textField.setElementDescriptionFunction(elementDescriptionFunction);
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
