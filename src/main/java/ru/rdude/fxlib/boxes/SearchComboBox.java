package ru.rdude.fxlib.boxes;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.ComboBox;
import javafx.scene.input.KeyEvent;

import java.util.ArrayList;
import java.util.Collection;

public class SearchComboBox<T> extends ComboBox<T> {

    private FilteredList<T> filteredList;
    private boolean isTyped;


    public SearchComboBox() {
        this(FXCollections.observableList(new ArrayList<>()));
    }

    public SearchComboBox(ObservableList<T> items) {
        super(items);
        filteredList = new FilteredList<>(items);
        initTextListener();
        initShowListener();
        setItems(filteredList);
        isTyped = false;
    }

    public void setCollection(Collection<T> collection) {
        filteredList = new FilteredList<>(FXCollections.observableList(new ArrayList<>(collection)));
        setItems(filteredList);
    }


    /**
     * Set listener that will listen to this text input and generate suggestions based on it.
     */
    private void initTextListener() {
        getEditor().addEventHandler(KeyEvent.KEY_TYPED, event -> {
            isTyped = true;
        });
        getEditor().textProperty().addListener(((observableValue, oldV, newV) -> {
            // filter elements only when text field changed when typing and not when value changed in any other ways
            if (isTyped) {
                isTyped = false;
                if (!newV.isEmpty() && isShowing() && isEditable()) {
                    filteredList.setPredicate(e -> e.toString().toLowerCase().contains(newV.toLowerCase()));
                } else {
                    filteredList.setPredicate(e -> true);
                }
                // need to set text back to the value after typing because parent class change it after filtering (javaFx problem)
                getEditor().setText(newV);
            }
        }));
    }

    private void initShowListener() {
        showingProperty().addListener(((observableValue, oldV, newV) -> {
            if (newV) {
                setEditable(true);
                filteredList.setPredicate(e -> true);
                getEditor().setText("");
            } else {
                setEditable(false);
            }
        }));
    }
}
