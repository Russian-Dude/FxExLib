package ru.rdude.fxlib.containers.selector;

import javafx.beans.property.Property;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import ru.rdude.fxlib.boxes.SearchComboBox;

import java.util.Collection;
import java.util.function.Function;

public class SelectorElementTwoChoice<T, V> extends GridPane implements SelectorElementNode<T> {

    private final SearchComboBox<T> searchComboBox = new SearchComboBox<>();
    private final SearchComboBox<V> secondSearchComboBox = new SearchComboBox<>();

    public SelectorElementTwoChoice() {
        super();
        searchComboBox.setMaxWidth(Double.MAX_VALUE);
        searchComboBox.setMinWidth(0);
        secondSearchComboBox.setMaxWidth(Double.MAX_VALUE);
        secondSearchComboBox.setMinWidth(0);
        ColumnConstraints firstBoxConstraints = new ColumnConstraints();
        firstBoxConstraints.setPercentWidth(50);
        firstBoxConstraints.setFillWidth(true);
        ColumnConstraints secondBoxConstraints = new ColumnConstraints();
        secondBoxConstraints.setPercentWidth(50);
        secondBoxConstraints.setFillWidth(true);
        getColumnConstraints().addAll(firstBoxConstraints, secondBoxConstraints);
        addColumn(0, searchComboBox);
        addColumn(1, secondSearchComboBox);
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

    public SearchComboBox<T> getSearchComboBox() {
        return searchComboBox;
    }

    public void setSearchEnabled(boolean searchEnabled) {
        searchComboBox.setSearchEnabled(searchEnabled);
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


    public Property<V> getSecondValueProperty() {
        return secondSearchComboBox.valueProperty();
    }

    public V getSecondValue() {
        return secondSearchComboBox.getValue();
    }

    public SearchComboBox<V> getSecondSearchComboBox() {
        return secondSearchComboBox;
    }

    public void setSecondCollection(Collection<V> collection) {
        secondSearchComboBox.setCollection(collection);
    }

    public void setSecondSearchEnabled(boolean searchEnabled) {
        secondSearchComboBox.setSearchEnabled(searchEnabled);
    }

    public void setSecondSearchBy(Function<V, String> function) {
        secondSearchComboBox.setNameAndSearchBy(function);
    }

    public void setSecondSearchBy(Function<V, String> function, Function<V, String>... functions) {
        secondSearchComboBox.setSearchBy(function, functions);
    }

    public void setSecondSearchBy(Collection<Function<V, String>> functions) {
        secondSearchComboBox.setSearchBy(functions);
    }

    public void setSecondNameAndSearchBy(Function<V, String> function) {
        secondSearchComboBox.setNameAndSearchBy(function);
    }


    public void setSecondNameBy(Function<V, String> function) {
        secondSearchComboBox.setNameBy(function);
    }

    public final void setSizePercentages(double main, double second) {
        getColumnConstraints().get(0).setPercentWidth(main);
        getColumnConstraints().get(1).setPercentWidth(second);
    }
}
