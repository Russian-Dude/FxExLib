package ru.rdude.fxlib.containers.selector;

import javafx.beans.property.*;
import javafx.beans.value.ObservableValue;
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
    private final SelectorContainerObservableList selectedElements = new SelectorContainerObservableList(new ArrayList<>());
    private final ObservableList<ElementHolder> selectedElementsNodes = FXCollections.observableArrayList();
    private final BooleanProperty unique = new SimpleBooleanProperty(true);
    private final ElementHolderBuilder holderBuilder = new ElementHolderBuilder();

    private final VBox elementsVbox = new VBox();
    private final Button addButton = new Button("+");
    private final SearchDialog<T> searchDialog = new SearchDialog<>();
    private final SimpleObjectProperty<Predicate<T>> searchDialogPredicate = new SimpleObjectProperty<>(t -> !getSelected().contains(t));
    private final SimpleObjectProperty<FilteredList<T>> searchDialogFilteredList = new SimpleObjectProperty<>();

    private static final String DEFAULT_STYLE_CLASS = "fxex-selector-container";
    private static final String ADD_BUTTON_STYLE_CLASS = "fxex-selector-container-add-button";
    private static final String REMOVE_BUTTON_STYLE_CLASS = "fxex-selector-container-remove-button";
    private static final String SEARCH_BUTTON_STYLE_CLASS = "fxex-selector-container-search-button";


    public static <T> SelectorBuilder.SimpleSelectorBuilder<T, SearchComboBox<T>> simple(@NotNull Collection<T> collection) {
        return SelectorBuilder.simple(collection);
    }

    public static <T, E extends SearchComboBox<T>> SelectorBuilder.SimpleSelectorBuilder<T, E> simple(@NotNull Collection<T> collection, @NotNull Supplier<E> creator) {
        return SelectorBuilder.simple(collection, creator);
    }

    public static <T, V> SelectorBuilder.AutocompletionTextFieldSelectorBuilder<T, V, SelectorElementAutocompletionTextField<T, V>> withAutocompletionTextField(
            @NotNull Collection<T> mainCollection, @NotNull Collection<V> secondCollection) {
        return SelectorBuilder.withAutoCompletionTextField(mainCollection, secondCollection);
    }

    public static <T, V, E extends SelectorElementAutocompletionTextField<T, V>> SelectorBuilder.AutocompletionTextFieldSelectorBuilder<T, V, E> withAutocompletionTextField(
            @NotNull Collection<T> mainCollection, @NotNull Collection<V> secondCollection, @NotNull Supplier<E> creator) {
        return SelectorBuilder.withAutoCompletionTextField(mainCollection, secondCollection, creator);
    }

    public static <T> SelectorBuilder.PercentSelectorBuilder<T, SelectorElementPercent<T>> withPercents(@NotNull Collection<T> collection) {
        return SelectorBuilder.withPercents(collection);
    }

    public static <T, E extends SelectorElementPercent<T>> SelectorBuilder.PercentSelectorBuilder<T, E> withPercents(@NotNull Collection<T> collection, @NotNull Supplier<E> creator) {
        return SelectorBuilder.withPercents(collection, creator);
    }

    public static <T> SelectorBuilder.TextFieldSelectorBuilder<T, SelectorElementTextField<T>> withTextField(@NotNull Collection<T> collection) {
        return SelectorBuilder.withTextField(collection);
    }

    public static <T, E extends SelectorElementTextField<T>> SelectorBuilder.TextFieldSelectorBuilder<T, E> withTextField(@NotNull Collection<T> collection, @NotNull Supplier<E> creator) {
        return SelectorBuilder.withTextField(collection, creator);
    }

    public static <T, V> SelectorBuilder.TwoComboBoxesSelectorBuilder<T, V, SelectorElementTwoChoice<T, V>> withTwoComboBoxes(
            @NotNull Collection<T> mainCollection,
            @NotNull Collection<V> secondCollection) {
        return SelectorBuilder.withTwoComboBoxes(mainCollection, secondCollection);
    }

    public static <T, V, E extends SelectorElementTwoChoice<T, V>> SelectorBuilder.TwoComboBoxesSelectorBuilder<T, V, E> withTwoComboBoxes(
            @NotNull Collection<T> mainCollection,
            @NotNull Collection<V> secondCollection,
            @NotNull Supplier<E> creator) {
        return SelectorBuilder.withTwoComboBoxes(mainCollection, secondCollection, creator);
    }

    public static <T, V extends Node> SelectorBuilder.WithPropertiesWindowSelectorBuilder<T, V, SelectorElementWindowProperties<T, V>> withPropertiesWindow(
            @NotNull Collection<T> collection,
            @NotNull Supplier<V> propertiesWindowCreator
    ){
        return SelectorBuilder.withPropertiesWindow(collection, propertiesWindowCreator);
    }

    public static <T, V extends Node, E extends SelectorElementWindowProperties<T, V>> SelectorBuilder.WithPropertiesWindowSelectorBuilder<T, V, E> withPropertiesWindow(
            @NotNull Collection<T> collection,
            @NotNull Supplier<V> propertiesWindowCreator,
            @NotNull Function<V, E> creator
    ){
        return SelectorBuilder.withPropertiesWindow(collection, propertiesWindowCreator, creator);
    }


    public SelectorContainer(Collection<T> elements, Supplier<E> elementNodeCreator) {
        super();
        this.getStyleClass().add(DEFAULT_STYLE_CLASS);
        addButton.getStyleClass().add(ADD_BUTTON_STYLE_CLASS);
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
        selectedElements.clear();
    }

    public E add(T t) {
        return selectedElements.addAndReturnHolder(t);
    }

    public boolean addAll(Collection<T> values) {
        return selectedElements.addAll(values);
    }

    public void removeFirst(T t) {
        selectedElements.removeFirst(t);
    }

    private void remove(ElementHolder holder) {
        final int index = selectedElementsNodes.indexOf(holder);
        if (index >= 0) {
            selectedElements.remove(index);
        }
    }

    public List<T> getSelected() {
        return new ArrayList<>(selectedElements);
    }

    public Collection<T> getSelected(Supplier<Collection<T>> collectionSupplier) {
        final Collection<T> collection = collectionSupplier.get();
        collection.addAll(selectedElements);
        return collection;
    }

    public List<E> getSelectedElementsNodes() {
        return selectedElementsNodes.stream()
                .map(n -> n.elementNode)
                .collect(Collectors.toList());
    }

    public ObservableList<T> getItems() {
        return selectedElements;
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

    public SelectorContainer<T, E> onNodeElementValueChange(BiConsumer<E, T> option) {
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

    public void setSearchDialogNameByProperty(Function<T, ObservableValue<String>> function) {
        if (function == null) {
            throw new NullPointerException();
        }
        searchDialog.getSearchPane().setNameByProperty(function);
    }

    public void setSearchDialogSearchByProperty(Function<T, ObservableValue<String>> function, Function<T, ObservableValue<String>>... functions) {
        if (function == null) {
            throw new NullPointerException();
        }
        searchDialog.getSearchPane().setTextFieldSearchByProperty(function, functions);
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

    private class SelectorContainerObservableList extends SimpleListProperty<T> {

        public SelectorContainerObservableList(List<T> list) {
            super(FXCollections.observableArrayList(list));
        }

        @Override
        public void clear() {
            super.clear();
            selectedElementsNodes.clear();
        }

        @Override
        public void remove(int i, int i1) {
            super.remove(i, i1);
            selectedElementsNodes.remove(i, i1);
        }

        @Override
        public boolean removeAll(Collection<?> collection) {
            super.removeAll(collection);
            return selectedElementsNodes.removeIf(holder -> collection.contains(holder.getValue()));
        }

        @Override
        public boolean retainAll(Collection<?> collection) {
            super.retainAll(collection);
            final List<ElementHolder> retain = selectedElementsNodes.stream()
                    .filter(holder -> collection.contains(holder.getValue()))
                    .collect(Collectors.toList());
            return selectedElementsNodes.retainAll(retain);
        }

        @Override
        public boolean setAll(Collection<? extends T> collection) {
            super.setAll(collection);
            selectedElementsNodes.clear();
            final List<ElementHolder> add = collection.stream()
                    .map(holderBuilder::create)
                    .collect(Collectors.toList());
            return selectedElementsNodes.addAll(add);
        }

        @Override
        public boolean addAll(Collection<? extends T> collection) {
            super.addAll(collection);
            final List<ElementHolder> add = collection.stream()
                    .map(holderBuilder::create)
                    .collect(Collectors.toList());
            return selectedElementsNodes.addAll(add);
        }

        @Override
        public boolean addAll(int i, Collection<? extends T> collection) {
            super.addAll(i, collection);
            final List<ElementHolder> add = collection.stream()
                    .map(holderBuilder::create)
                    .collect(Collectors.toList());
            return selectedElementsNodes.addAll(i, add);
        }

        @Override
        public void add(int i, T t) {
            super.add(i, t);
            selectedElementsNodes.add(i, holderBuilder.create(t));
        }

        @Override
        public T set(int i, T t) {
            T was = super.set(i, t);
            selectedElementsNodes.set(i, holderBuilder.create(t));
            return was;
        }

        @Override
        public boolean remove(Object o) {
            boolean removed = super.remove(o);
            if (removed && o != null) {
                selectedElementsNodes.removeIf(holder -> o.equals(holder.getValue()));
            }
            return removed;
        }

        @Override
        public T remove(int i) {
            T res = super.remove(i);
            if (res != null) {
                selectedElementsNodes.remove(i);
            }
            return res;
        }

        void replace(int i, T t) {
            super.remove(i);
            super.add(i, t);
        }

        public void removeFirst(T t) {
            final T remove = this.stream()
                    .filter(t1 -> t1.equals(t))
                    .findFirst()
                    .orElse(null);
            if (remove != null) {
                super.remove(t);
                selectedElementsNodes.stream()
                        .filter(holder -> holder.getValue().equals(t))
                        .findFirst()
                        .ifPresent(selectedElementsNodes::remove);
            }
        }

        @Override
        public boolean addAll(T... ts) {
            return addAll(Arrays.asList(ts));
        }

        @Override
        public boolean setAll(T... ts) {
            return setAll(Arrays.asList(ts));
        }

        @Override
        public boolean removeAll(T... ts) {
            return removeAll(Arrays.asList(ts));
        }

        @Override
        public boolean retainAll(T... ts) {
            return retainAll(Arrays.asList(ts));
        }

        @Override
        public boolean add(T t) {
            super.add(t);
            return selectedElementsNodes.add(holderBuilder.create(t));
        }

        E addAndReturnHolder(T t) {
            super.add(t);
            ElementHolder holder = holderBuilder.create(t);
            selectedElementsNodes.add(holder);
            return holder.elementNode;
        }
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
                    int index = selectedElementsNodes.indexOf(elementHolder);
                    if (index >= 0) {
                        selectedElements.replace(index, newV);
                    }
                    selectedElementsNodes.forEach(SelectorContainer.this::setPredicateFor);
                    biOptions.forEach(option -> option.accept(elementHolder.elementNode, newV));
                }
            });
            elementHolder.setValue(t);
            elementHolder.userChangeValue = true;

            // search button
            if (hasSearchButton.get()) {
                elementHolder.searchButton = new Button();
                elementHolder.searchButton.getStyleClass().add(SEARCH_BUTTON_STYLE_CLASS);
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
            elementHolder.deleteButton.getStyleClass().add(REMOVE_BUTTON_STYLE_CLASS);
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
            if (elementNode instanceof SearchComboBox) {
                ((SearchComboBox<?>) elementNode).setMaxWidth(Double.MAX_VALUE);
            }
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
