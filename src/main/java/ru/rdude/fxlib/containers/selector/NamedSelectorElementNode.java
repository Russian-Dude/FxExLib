package ru.rdude.fxlib.containers.selector;

import javafx.beans.value.ObservableValue;

import java.util.function.Function;

public interface NamedSelectorElementNode<T> extends SelectorElementNode<T> {

    void setNameBy(Function<T, String> nameBy);
    void setNameByProperty(Function<T, ObservableValue<String>> nameByProperty);
    void setSearchBy(Function<T, String> function, Function<T, String>... functions);
    void setSearchByProperty(Function<T, ObservableValue<String>> function, Function<T, ObservableValue<String>>... functions);

}
