package ru.rdude.fxlib.containers.selector;

import javafx.beans.property.Property;

import java.util.Collection;

public interface SelectorElementNode<T> {

    Property<T> valueProperty();

    T getValue();

    void setValue(T t);

    void setCollection(Collection<T> collection);
}
