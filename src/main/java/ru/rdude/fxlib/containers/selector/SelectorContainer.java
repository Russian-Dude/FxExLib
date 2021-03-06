package ru.rdude.fxlib.containers.selector;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.collections.transformation.FilteredList;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import org.jetbrains.annotations.NotNull;
import ru.rdude.fxlib.boxes.SearchComboBox;
import ru.rdude.fxlib.dialogs.SearchDialog;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;

public class SelectorContainer<T, E extends Node & SelectorElementNode<T>> extends ScrollPane {

    private ObservableList<T> elements;
    private final Supplier<E> elementNodeCreator;
    private final ObservableList<ElementHolder> selectedElementsNodes = FXCollections.observableArrayList();
    private final BooleanProperty unique = new SimpleBooleanProperty(true);
    private final ElementHolderBuilder holderBuilder = new ElementHolderBuilder();

    private final VBox elementsVbox = new VBox();
    private final Button addButton = new Button("+");
    private final SearchDialog<T> searchDialog = new SearchDialog<>();
    private final SimpleObjectProperty<Predicate<T>> searchDialogPredicate = new SimpleObjectProperty<>(t -> !getSelected().contains(t));
    private final SimpleObjectProperty<FilteredList<T>> searchDialogFilteredList = new SimpleObjectProperty<>();

    public static <T> SelectorContainer<T, SearchComboBox<T>> simple(@NotNull Collection<T> collection) {
        return SelectorFactory.simple(collection);
    }

    @SafeVarargs
    public static <T> SelectorContainer<T, SearchComboBox<T>> simple(
            @NotNull Collection<T> collection,
            @NotNull Function<T, String> nameFunction,
            Function<T, String>... searchFunctions) {

        return SelectorFactory.simple(collection, nameFunction, searchFunctions);
    }

    public static <T, V> SelectorContainer<T, SelectorElementAutocompletionTextField<T, V>> withAutocompletionTextField(
            @NotNull Collection<T> collection,
            @NotNull Collection<V> autocompletionCollection) {

        return withAutocompletionTextField(collection, autocompletionCollection, null, null);
    }


    @SafeVarargs
    public static <T, V> SelectorContainer<T, SelectorElementAutocompletionTextField<T, V>> withAutocompletionTextField(
            @NotNull Collection<T> collection,
            @NotNull Collection<V> autocompletionCollection,
            Function<T, String> nameFunction,
            Function<V, String> autocompletionNameFunction,
            @NotNull Function<T, String>... searchFunctions) {

        return SelectorFactory.withAutocompletionTextField(collection, autocompletionCollection, nameFunction,
                autocompletionNameFunction, searchFunctions);
    }


    public static <T> SelectorContainer<T, SelectorElementPercent<T>> withPercents(@NotNull Collection<T> collection) {
        return withPercents(collection, null);
    }


    @SafeVarargs
    public static <T> SelectorContainer<T, SelectorElementPercent<T>> withPercents(
            @NotNull Collection<T> collection,
            Function<T, String> nameFunction,
            @NotNull Function<T, String>... searchFunctions) {

        return SelectorFactory.withPercents(collection, nameFunction, searchFunctions);
    }


    public static <T> SelectorContainer<T, SelectorElementTextField<T>> withTextField(@NotNull Collection<T> collection) {
        return withTextField(collection, null);
    }


    @SafeVarargs
    public static <T> SelectorContainer<T, SelectorElementTextField<T>> withTextField(
            @NotNull Collection<T> collection,
            Function<T, String> nameFunction,
            @NotNull Function<T, String>... searchFunctions) {

        return SelectorFactory.withTextField(collection, nameFunction, searchFunctions);
    }


    public static <T, V> SelectorContainer<T, SelectorElementTwoChoice<T, V>> withTwoComboBoxes(
            @NotNull Collection<T> mainCollection,
            @NotNull Collection<V> secondCollection) {
        return withTwoComboBoxes(mainCollection, secondCollection, null, null, null, null);
    }


    public static <T, V> SelectorContainer<T, SelectorElementTwoChoice<T, V>> withTwoComboBoxes(
            @NotNull Collection<T> mainCollection,
            @NotNull Collection<V> secondCollection,
            Function<T, String> mainNameFunction,
            Collection<Function<T, String>> mainSearchFunctions,
            Function<V, String> secondNameFunction,
            Collection<Function<V, String>> secondSearchFunctions) {

        return SelectorFactory.withTwoComboBoxes(mainCollection, secondCollection, mainNameFunction,
                mainSearchFunctions, secondNameFunction, secondSearchFunctions);
    }


    public SelectorContainer(Collection<T> elements, Supplier<E> elementNodeCreator) {
        super();
        this.elementNodeCreator = elementNodeCreator;
        setElements(elements);

        this.setFitToWidth(true);

        VBox vBox = new VBox();
        vBox.getChildren().add(elementsVbox);
        vBox.getChildren().add(addButton);
        addButton.setMaxWidth(Double.POSITIVE_INFINITY);
        vBox.setFillWidth(true);
        setContent(vBox);

        configAdding();
    }

    public void setElements(Collection<T> elements) {
        this.elements = elements instanceof ObservableList ? (ObservableList<T>) elements : FXCollections.observableArrayList(elements);
        this.searchDialogFilteredList.set(new FilteredList<>(this.elements));
        this.searchDialogPredicate.set(t -> true);
        this.searchDialog.setCollection(new FilteredList<>(this.searchDialogFilteredList.get(), this.searchDialogPredicate.get()));
    }

    public void clear() {
        selectedElementsNodes.clear();
    }

    public E add(T t) {
        ElementHolder holder = holderBuilder.create(t);
        selectedElementsNodes.add(holder);
        return holder.elementNode;
    }

    public void addAll(Collection<T> values) {
        values.forEach(this::add);
    }

    public void removeFirst(T t) {
        selectedElementsNodes.stream()
                .filter(holder -> holder.getValue().equals(t))
                .findFirst()
                .ifPresent(this::remove);
    }

    private void remove(ElementHolder holder) {
        selectedElementsNodes.remove(holder);
    }

    public List<T> getSelected() {
        return selectedElementsNodes.stream()
                .map(ElementHolder::getValue)
                .collect(Collectors.toList());
    }

    public Collection<T> getSelected(Supplier<Collection<T>> collectionSupplier) {
        return selectedElementsNodes.stream()
                .map(ElementHolder::getValue)
                .collect(Collectors.toCollection(collectionSupplier));
    }

    public List<E> getSelectedElementsNodes() {
        return selectedElementsNodes.stream()
                .map(n -> n.elementNode)
                .collect(Collectors.toList());
    }

    public boolean isUnique() {
        return unique.get();
    }

    public BooleanProperty uniqueProperty() {
        return unique;
    }

    public void setUnique(boolean unique) {
        this.unique.set(unique);
    }

    public Button getAddButton() {
        return addButton;
    }

    public SearchDialog<T> getSearchDialog() {
        return searchDialog;
    }

    public void setHasSearchButton(boolean value) {
        holderBuilder.hasSearchButton.setValue(value);
    }

    public void setSearchButtonText(String value) {
        holderBuilder.searchButtonText.setValue(value);
    }

    public void setSearchButtonGraphic(Node value) {
        holderBuilder.searchButtonGraphic.setValue(value);
    }

    public void setDeleteButtonText(String value) {
        holderBuilder.deleteButtonText.setValue(value);
    }

    public void setDeleteButtonGraphic(Node value) {
        holderBuilder.deleteButtonGraphic.setValue(value);
    }

    public void configDeleteButton(Consumer<Button> consumer) {
        if (consumer == null) {
            throw new NullPointerException();
        }
        holderBuilder.deleteButtonOptions.add(consumer);
    }

    public void configSearchButton(Consumer<Button> consumer) {
        if (consumer == null) {
            throw new NullPointerException();
        }
        holderBuilder.searchButtonOptions.add(consumer);
    }

    public SelectorContainer<T, E> addOnNodeElementValueChange(BiConsumer<E, T> option) {
        this.holderBuilder.biOptions.add(option);
        return this;
    }

    public SelectorContainer<T, E> addOption(Consumer<E> option) {
        this.holderBuilder.options.add(option);
        return this;
    }

    public void setSearchDialogNameBy(Function<T, String> function) {
        if (function == null) {
            throw new NullPointerException();
        }
        searchDialog.getSearchPane().setNameBy(function);
    }

    public void setSearchDialogSearchBy(Function<T, String> function, Function<T, String>... functions) {
        if (function == null) {
            throw new NullPointerException();
        }
        searchDialog.getSearchPane().setTextFieldSearchBy(function, functions);
    }

    private void configAdding() {
        addButton.setOnAction(event -> elements.stream()
                .filter(t -> !unique.get() || selectedElementsNodes.stream()
                        .map(ElementHolder::getValue)
                        .noneMatch(t::equals))
                .findFirst()
                .ifPresent(this::add));
        selectedElementsNodes.addListener((ListChangeListener<ElementHolder>) change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    elementsVbox.getChildren().addAll(change.getAddedSubList());
                } else if (change.wasRemoved()) {
                    elementsVbox.getChildren().removeAll(change.getRemoved());
                }
            }
        });
    }

    // need to recreate predicate on selected elements list update to force filter.
    // using same object for predicate do not work so every time create new
    private void setPredicateFor(ElementHolder elementHolder) {
        elementHolder.userChangeValue = false;
        // need to reassign value because filtered list changes makes value become null
        T storedValue = elementHolder.getValue();
        elementHolder.collection.setPredicate(
                t -> t.equals(elementHolder.getValue()) || selectedElementsNodes.stream().map(ElementHolder::getValue).noneMatch(t::equals));
        elementHolder.setValue(storedValue);
        elementHolder.userChangeValue = true;
    }

    private class ElementHolderBuilder {

        final BooleanProperty hasSearchButton = new SimpleBooleanProperty(true);
        final StringProperty searchButtonText = new SimpleStringProperty("\uD83D\uDD0E");
        final StringProperty deleteButtonText = new SimpleStringProperty("X");
        final ObjectProperty<Node> searchButtonGraphic = new SimpleObjectProperty<>();
        final ObjectProperty<Node> deleteButtonGraphic = new SimpleObjectProperty<>();
        final ObservableSet<Consumer<Button>> searchButtonOptions = FXCollections.observableSet(new HashSet<>());
        final ObservableSet<Consumer<Button>> deleteButtonOptions = FXCollections.observableSet(new HashSet<>());
        final ObservableSet<Consumer<E>> options = FXCollections.observableSet(new HashSet<>());
        final ObservableSet<BiConsumer<E, T>> biOptions = FXCollections.observableSet(new HashSet<>());

        public ElementHolderBuilder() {
            // listen to has search button property
            hasSearchButton.addListener((observableValue, oldV, newV) -> {
                if (oldV && !newV) {
                    selectedElementsNodes.forEach(selected -> selected.leftProperty().set(null));
                } else if (!oldV && newV) {
                    selectedElementsNodes.forEach(selected -> {
                        selected.searchButton = new Button();
                        selected.searchButton.setOnAction(event -> searchDialog.showAndWait()
                                .ifPresent(selected.elementNode::setValue));
                        selected.searchButton.setText(searchButtonText.get());
                        selected.searchButton.textProperty().bind(searchButtonText);
                        selected.searchButton.setGraphic(searchButtonGraphic.get());
                        selected.searchButton.graphicProperty().bind(searchButtonGraphic);
                        searchButtonOptions.forEach(c -> c.accept(selected.searchButton));
                        selected.setLeft(selected.searchButton);
                    });
                }
            });
        }

        ElementHolder create(T t) {
            FilteredList<T> filter = new FilteredList<>(elements);
            ElementHolder elementHolder = new ElementHolder(elementNodeCreator.get(), filter);
            setPredicateFor(elementHolder);
            options.forEach(option -> option.accept(elementHolder.elementNode));

            elementHolder.valueProperty().addListener((observableValue, oldV, newV) -> {
                if (newV == null && oldV != null) {
                    elementHolder.setValue(oldV);
                }
                if (elementHolder.userChangeValue && newV != null) {
                    selectedElementsNodes.forEach(SelectorContainer.this::setPredicateFor);
                    biOptions.forEach(option -> option.accept(elementHolder.elementNode, newV));
                }
            });
            elementHolder.setValue(t);
            elementHolder.userChangeValue = true;

            // search button
            if (hasSearchButton.get()) {
                elementHolder.searchButton = new Button();
                searchButtonOptions.forEach(c -> c.accept(elementHolder.searchButton));
                elementHolder.searchButton.setOnAction(event -> {
                    searchDialogFilteredList.get().setPredicate(t1 -> t1.equals(elementHolder.getValue()) || selectedElementsNodes.stream().map(ElementHolder::getValue).noneMatch(t1::equals));
                    searchDialog.showAndWait()
                            .ifPresent(elementHolder.elementNode::setValue);
                });
                elementHolder.searchButton.setText(searchButtonText.get());
                elementHolder.searchButton.textProperty().bind(searchButtonText);
                elementHolder.searchButton.setGraphic(searchButtonGraphic.get());
                elementHolder.searchButton.graphicProperty().bind(searchButtonGraphic);
                elementHolder.setLeft(elementHolder.searchButton);
            }

            // delete button
            elementHolder.deleteButton = new Button();
            deleteButtonOptions.forEach(c -> c.accept(elementHolder.deleteButton));
            elementHolder.deleteButton.setOnAction(event -> {
                remove(elementHolder);
                selectedElementsNodes.forEach(SelectorContainer.this::setPredicateFor);
            });
            elementHolder.deleteButton.setText(deleteButtonText.get());
            elementHolder.deleteButton.textProperty().bind(deleteButtonText);
            elementHolder.deleteButton.setGraphic(deleteButtonGraphic.get());
            elementHolder.deleteButton.graphicProperty().bind(deleteButtonGraphic);
            elementHolder.setRight(elementHolder.deleteButton);

            return elementHolder;
        }
    }

    private class ElementHolder extends BorderPane {

        E elementNode;
        Button searchButton;
        Button deleteButton;
        FilteredList<T> collection;
        // Holders nodes filtered list must be updated when another holder changed its value
        // But filtered list behavior reapplies values so this thing is needed and used to check
        // if value changed because of user change the value with ui
        boolean userChangeValue = false;

        public ElementHolder(E elementNode, FilteredList<T> collection) {
            this.elementNode = elementNode;
            this.collection = collection;
            this.elementNode.setCollection(collection);
            setMinWidth(0d);
            setCenter(elementNode);
        }

        T getValue() {
            return elementNode.getValue();
        }

        void setValue(T t) {
            if (t != null) {
                elementNode.setValue(t);
            }
        }

        public Property<T> valueProperty() {
            return elementNode.valueProperty();
        }
    }
}
