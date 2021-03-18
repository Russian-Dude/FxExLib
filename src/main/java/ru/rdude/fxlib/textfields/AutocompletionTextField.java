package ru.rdude.fxlib.textfields;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Side;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import org.intellij.lang.annotations.RegExp;
import org.jetbrains.annotations.NotNull;
import utils.FunctionRawOrProperty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

public class AutocompletionTextField<T> extends TextField {

    public enum Type {
        WORDS,
        FULL_STRING_STARTS_WITH,
        FULL_STRING_CONTAINS
    }

    private final SimpleObjectProperty<ContextMenu> popup = new SimpleObjectProperty<>(new ContextMenu());
    private final SimpleObjectProperty<ObservableList<T>> elements = new SimpleObjectProperty<>(FXCollections.observableArrayList(new ArrayList<>()));
    private final SimpleObjectProperty<FilteredList<T>> filteredElements = new SimpleObjectProperty<>(new FilteredList<>(FXCollections.observableArrayList(new ArrayList<>())));
    private final SimpleObjectProperty<FunctionRawOrProperty<T, String>> elementNameFunction = new SimpleObjectProperty<>(FunctionRawOrProperty.raw(Object::toString));
    private final SimpleObjectProperty<FunctionRawOrProperty<T, String>> elementDescriptionFunction = new SimpleObjectProperty<>();
    private final SimpleObjectProperty<Type> type = new SimpleObjectProperty<>(Type.FULL_STRING_CONTAINS);
    private final SimpleObjectProperty<String> wordsDelimiter = new SimpleObjectProperty<>("\\s");

    public AutocompletionTextField() {
        initListeners();
        setCollection(new ArrayList<>());
    }

    public AutocompletionTextField(@NotNull Collection<T> collection) {
        initListeners();
        setCollection(collection);
    }

    public AutocompletionTextField(@NotNull Type type) {
        initListeners();
        setType(type);
    }

    public AutocompletionTextField(@NotNull Collection<T> collection, @NotNull Type type) {
        initListeners();
        setCollection(collection);
        setType(type);
    }

    public ContextMenu getPopup() {
        return popup.get();
    }

    public SimpleObjectProperty<ContextMenu> popupProperty() {
        return popup;
    }

    public SimpleObjectProperty<FilteredList<T>> filteredElementsProperty() {
        return filteredElements;
    }

    public Type getType() {
        return type.get();
    }

    public SimpleObjectProperty<Type> typeProperty() {
        return type;
    }

    public void setCollection(@NotNull Collection<T> collection) {
        if (collection instanceof ObservableList) {
            elements.set((ObservableList<T>) collection);
        } else {
            elements.set(FXCollections.observableArrayList(collection));
        }
        filteredElements.set(new FilteredList<>(elements.get()));
    }

    public void setType(Type type) {
        this.type.set(type);
    }

    public void setNameBy(Function<T, String> elementNameFunction) {
        this.elementNameFunction.set(FunctionRawOrProperty.raw(elementNameFunction));
    }

    public void setNameByProperty(Function<T, ObservableValue<String>> elementNameFunction) {
        this.elementNameFunction.set(FunctionRawOrProperty.property(elementNameFunction));
    }

    public String getWordsDelimiter() {
        return wordsDelimiter.get();
    }

    public SimpleObjectProperty<String> wordsDelimiterProperty() {
        return wordsDelimiter;
    }

    public void setWordsDelimiter(@RegExp String regex) {
        this.wordsDelimiter.set(regex);
    }

    public void setElementDescriptionFunction(Function<T, String> elementDescriptionFunction) {
        this.elementDescriptionFunction.set(FunctionRawOrProperty.raw(elementDescriptionFunction));
    }

    public void setElementDescriptionByPropertyFunction(Function<T, ObservableValue<String>> elementDescriptionFunction) {
        this.elementDescriptionFunction.set(FunctionRawOrProperty.property(elementDescriptionFunction));
    }

    private void initListeners() {
        filteredElements.addListener((observableValue, oldV, newV) -> {
            newV.addListener((ListChangeListener<T>) change -> {
                ContextMenu popup = this.popup.get();
                popup.getItems().clear();
                newV.stream()
                        .map(ItemHolder::new)
                        .forEach(itemHolder -> {
                            MenuItem menuItem = new MenuItem(itemHolder.fullName);
                            menuItem.setOnAction(actionEvent -> addSuggestionToTextField(itemHolder));
                            popup.getItems().add(menuItem);
                        });
            });
        });

        AtomicBoolean typed = new AtomicBoolean(false);

        textProperty().addListener((observableValue, oldV, newV) -> {
            if (newV == null || newV.isEmpty() || newV.equals(oldV)) {
                popup.get().hide();
                return;
            }
            if (type.get().equals(Type.FULL_STRING_CONTAINS)) {
                filteredElements.get().setPredicate(t -> elementNameFunction.get().apply(t).toLowerCase().contains(getText().toLowerCase()));
            }
            else if (type.get().equals(Type.FULL_STRING_STARTS_WITH)) {
                filteredElements.get().setPredicate(t -> elementNameFunction.get().apply(t).toLowerCase().startsWith(getText().toLowerCase()));
            }
            else if (type.get().equals(Type.WORDS)) {
                typed.set(true);
                int realCaretPosition = newV.length() > oldV.length() ? getCaretPosition() + 1 : getCaretPosition() - 1;
                int end = realCaretPosition;
                int start = end;
                while (start > 0 && !String.valueOf(newV.charAt(start - 1)).matches(wordsDelimiter.get())) {
                    start--;
                }
                if (start == end) {
                    popup.get().hide();
                    return;
                }
                String word = newV.substring(start, end);
                filteredElements.get().setPredicate(t -> elementNameFunction.get().apply(t).toLowerCase().contains(word.toLowerCase()));
            }
            if (filteredElements.get().size() > 0 && isFocused()) {
                popup.get().show(AutocompletionTextField.this, Side.BOTTOM, 0, 0);
            }
            else {
                popup.get().hide();
            }
        });
        caretPositionProperty().addListener((observableValue, oldV, newV) -> {
            if (type.get().equals(Type.WORDS) && popup.get().isShowing()) {
                if (typed.get()) {
                    typed.set(false);
                }
                else {
                    popup.get().hide();
                }
            }
        });
    }

    private void addSuggestionToTextField(ItemHolder itemHolder) {
        Type type = this.type.get();
        if (type.equals(Type.FULL_STRING_CONTAINS) || type.equals(Type.FULL_STRING_STARTS_WITH)) {
            setText(itemHolder.name);
            positionCaret(getText().length());
        }
        else {
            String text = getText();
            int start = getCaretPosition();
            while (start > 0 && !String.valueOf(text.charAt(start - 1)).matches(wordsDelimiter.get())) {
                start--;
            }
            String firstPart = start > 0 ? text.substring(0, start) : "";
            String endPart = text.substring(getCaretPosition());
            setText(firstPart + itemHolder.name + endPart);
            positionCaret(firstPart.length() + itemHolder.name.length());
        }
    }

    private class ItemHolder {
        T t;
        String name;
        String fullName;

        ItemHolder(T t) {
            this.t = t;
            name = elementNameFunction.get().apply(t);
            fullName = elementDescriptionFunction.get() == null ? name : name + " " + elementDescriptionFunction.get().apply(t);
        }
    }


}
