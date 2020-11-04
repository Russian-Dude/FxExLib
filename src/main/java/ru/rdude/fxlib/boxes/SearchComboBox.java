package ru.rdude.fxlib.boxes;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.ComboBox;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.StringConverter;

import java.util.*;
import java.util.function.Function;

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
public class SearchComboBox<T> extends ComboBox<T> {

    private FilteredList<T> filteredList;
    private boolean isTyped;
    private Set<Function<T, String>> getElementSearchFunctions;
    private Map<String, T> stringConverterMap;


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
        getElementSearchFunctions = Set.of(Object::toString);
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

    public void setNameAndSearchBy(Function<T, String> function) {
        setSearchBy(function);
        setNameBy(function);
    }

    @SafeVarargs
    public final void setSearchBy(Function<T, String>... functions) {
        setSearchBy(Arrays.asList(functions));
    }

    public void setSearchBy(Collection<Function<T, String>> functions) {
        this.getElementSearchFunctions = new HashSet<>(functions);
    }

    public void setNameBy(Function<T, String> function) {
        stringConverterMap = new HashMap<>();
        for (T t : filteredList) {
            String name = function.apply(t);
            if (stringConverterMap.containsKey(name)) {
                String newName = name;
                int sameNames = 0;
                while (stringConverterMap.containsKey(newName)) {
                    sameNames++;
                    newName = name + " (" + sameNames + ")";
                }
                name = newName;
            }
            stringConverterMap.put(name, t);
        }
        StringConverter<T> stringConverter = new StringConverter<T>() {
            @Override
            public String toString(T t) {
                if (t == null) {
                    return "";
                }
                return stringConverterMap.entrySet().stream()
                        .filter(entry -> entry.getValue() == t)
                        .map(Map.Entry::getKey)
                        .findFirst()
                        .orElse("");
            }

            @Override
            public T fromString(String s) {
                return stringConverterMap.get(s);
            }
        };
        setConverter(stringConverter);
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
                    filteredList.setPredicate(e -> getElementSearchFunctions.stream()
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
