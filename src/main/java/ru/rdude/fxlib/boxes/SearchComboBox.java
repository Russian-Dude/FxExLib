package ru.rdude.fxlib.boxes;

import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.ComboBox;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.StringConverter;
import ru.rdude.fxlib.containers.selector.SelectorElementNode;
import utils.FunctionRawOrProperty;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Extended version of ComboBox.
 * When open elements popup, editable mode will be enabled. When closed - disabled.
 * Elements can be searched by typing in the text field.
 * Elements searched by default based on toString() method of element, this can be changed by passing
 * function in the setSearchBy() method.
 * Elements representation can be changed without need to manually customize string converter, by passing function
 * to the setNameBy() method.
 * Both options can be set at once with setNameAndSearchBy() method.
 * Items can be set from any collection by using setCollection() method.
 */
public class SearchComboBox<T> extends ComboBox<T> implements SelectorElementNode<T> {

    private FilteredList<T> filteredList;
    private boolean isTyped;
    private Set<FunctionRawOrProperty<T, String>> searchFunctions;
    private boolean searchEnabled = true;


    public SearchComboBox() {
        this(FXCollections.observableList(new ArrayList<>()));
    }

    public SearchComboBox(Collection<T> items) {
        initTextListener();
        initShowListener();
        setCollection(items);
        isTyped = false;
        searchFunctions = Set.of(FunctionRawOrProperty.raw(Object::toString));
    }

    public void setCollection(Collection<T> collection) {
        if (collection instanceof ObservableList) {
            filteredList = new FilteredList<>((ObservableList<T>) collection);
        }
        else {
            filteredList = new FilteredList<>(FXCollections.observableList(new ArrayList<>(collection)));
        }
        setItems(filteredList);
    }

    public boolean isSearchEnabled() {
        return searchEnabled;
    }

    public void setSearchEnabled(boolean searchEnabled) {
        this.searchEnabled = searchEnabled;
    }

    public void setNameAndSearchBy(Function<T, String> function) {
        setSearchBy(function);
        setNameBy(function);
    }

    public void setNameAndSearchByProperty(Function<T, ObservableValue<String>> function) {
        setSearchByProperty(function);
        setNameByProperty(function);
    }

    @SuppressWarnings(value = "varargs")
    public void setSearchBy(Function<T, String> function, Function<T, String>... functions) {
        if (function == null) {
            throw new NullPointerException();
        }
        Set<Function<T, String>> set = new HashSet<>();
        set.add(function);
        if (functions != null) {
            set.addAll(Arrays.asList(functions));
        }
        setSearchBy(set);
    }

    public void setSearchBy(Collection<Function<T, String>> functions) {
        this.searchFunctions = functions.stream()
                .map(FunctionRawOrProperty::raw)
                .collect(Collectors.toSet());
    }

    public void setSearchByProperty(Function<T, ObservableValue<String>> function, Function<T, ObservableValue<String>>... functions) {
        if (function == null) {
            throw new NullPointerException();
        }
        Set<Function<T, ObservableValue<String>>> set = new HashSet<>();
        set.add(function);
        if (functions != null) {
            set.addAll(Arrays.asList(functions));
        }
        setSearchByProperty(set);
    }

    public void setSearchByProperty(Collection<Function<T, ObservableValue<String>>> functions) {
        this.searchFunctions = functions.stream()
                .map(FunctionRawOrProperty::property)
                .collect(Collectors.toSet());
    }

    public void setNameBy(Function<T, String> function) {
        if (function == null) {
            throw new NullPointerException();
        }
        setConverter(new StringConverter<T>() {
            @Override
            public String toString(T t) {
                return t != null ? function.apply(t) : null;
            }

            @Override
            public T fromString(String s) {
                return null;
            }
        });
    }

    public void setNameByProperty(Function<T, ObservableValue<String>> function) {
        if (function == null) {
            throw new NullPointerException();
        }
        setConverter(new StringConverter<T>() {
            @Override
            public String toString(T t) {
                return t != null ? function.apply(t).getValue() : null;
            }

            @Override
            public T fromString(String s) {
                return null;
            }
        });
    }


    private void initTextListener() {
        getEditor().addEventHandler(KeyEvent.KEY_TYPED, event -> isTyped = true);
        // for some reason key_typed capture delete and backspace after text changed so extra event handler here:
        getEditor().addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.BACK_SPACE || event.getCode() == KeyCode.DELETE) {
                isTyped = true;
            }
        });
        getEditor().textProperty().addListener(((observableValue, oldV, newV) -> {
            // filter elements only when text field changed when typing and not when value changed in any other way
            if (isTyped) {
                isTyped = false;
                if (!newV.isEmpty() && isShowing() && isEditable()) {
                    filteredList.setPredicate(e -> searchFunctions.stream()
                            .anyMatch(func -> func.apply(e).toLowerCase().contains(newV.toLowerCase())));
                } else {
                    filteredList.setPredicate(e -> true);
                }
                // need to set text back to the value after typing because parent class change it after filtering (javaFx problem)
                getEditor().setText(newV);
            }
        }));
    }

    private void initShowListener() {
        getEditor().setPrefWidth(0);
        showingProperty().addListener(((observableValue, oldV, newV) -> {
            if (searchEnabled && newV) {
                setEditable(true);
                filteredList.setPredicate(e -> true);
                setPromptText(getConverter().toString(getValue()));
                getEditor().setText("");
            } else {
                setEditable(false);
            }
        }));
    }
}
