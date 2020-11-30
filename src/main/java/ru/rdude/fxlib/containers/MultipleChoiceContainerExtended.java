package ru.rdude.fxlib.containers;

import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import ru.rdude.fxlib.boxes.SearchComboBox;
import ru.rdude.fxlib.panes.SearchPane;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Extended version of MultipleChoiceContainer to use with big amount of available elements.
 * Every element of this container has Search Dialog that helps to find elements with filters.
 * This search dialog can be customised by linking Control nodes getters with element's class getters.
 *
 * @param <T> type of the elements in container.
 * @param <C> controller type.
 */
public class MultipleChoiceContainerExtended<T, C> extends MultipleChoiceContainer<T> {

    /**
     * Search functions of Search dialog used when extended search is on.
     * This map link control with element getter.
     * Key - function with node as argument - node that will be added to search pane.
     * Value - function to get value from T element.
     */
    private Map<Function<C, Control>, Function<T, ?>> elementsSearchExtendedFunctions;
    /**
     * Node class that will be created and set into the Search Pane of the Search Dialog if extended search is on.
     * Creation of this node is set by loader field or by class field. Setting this field will set loader field to null.
     * This will work with elementsSearchExtendedFunctions Map.
     */
    private Class<? extends Node> extendedSearchExtraNodeClass;
    /**
     * Loader that will create node and set it into the Search Pane of the Search Dialog if extended search is on.
     * Creation of this node is set by loader field or by class field. Setting this field will set extendedSearchExtraNodeClass field to null.
     * This will work with elementsSearchExtendedFunctions Map.
     */
    @Deprecated
    private FXMLLoader loader;
    /**
     * Search Dialog for extended search options.
     */
    private SearchDialog<T> searchDialog;
    /**
     * Extra search node
     */
    private Node extraSearchNode;
    /**
     * Extra search node controller
     */
    private C extraSearchController;
    /**
     * Function to apply to popup in list view in extended search window.
     */
    private Function<T, Node> extendedSearchPopupFunction;

    /**
     * Extended search popup builder.
     */
    private ExtendedSearchPopupBuilder popupBuilder;

    public MultipleChoiceContainerExtended() {
        this(new ArrayList<>());
    }

    public MultipleChoiceContainerExtended(Collection<T> availableElements) {
        super(availableElements);
        elementsSearchExtendedFunctions = new HashMap<>();
    }

    /**
     * Add new visual node element with provided element value to the specified position.
     *
     * @param element element to add.
     * @param index   index to add to. Note that the add button is in the last index by default.
     * @return created visual node for element.
     */
    @Override
    public MultipleChoiceContainerElement<T> addElement(int index, T element) {
        try {
            MultipleChoiceContainerElement<T> containerElement = elementType.getDeclaredConstructor(Collection.class).newInstance(elements);

            // observe elements change
            AtomicBoolean clicked = new AtomicBoolean(false);
            containerElement.getComboBoxNode().addEventHandler(ComboBox.ON_HIDING, event -> {
                clicked.set(false);
            });
            containerElement.getComboBoxNode().addEventHandler(ComboBox.ON_SHOWING, event -> {
                clicked.set(false);
            });
            containerElement.getComboBoxNode().setOnMouseClicked(event -> {
                clicked.set(true);
            });
            containerElement.getComboBoxNode().valueProperty().addListener((observableValue, oldV, newV) -> {
                if (oldV != newV && clicked.get()) {
                    selectedElements.remove(oldV);
                    selectedElements.add(newV);
                }
            });

            // search functions
            if (elementsSearchFunctions != null) {
                containerElement.getComboBoxNode().setSearchBy(elementsSearchFunctions);
                containerElement.getSearchDialog().getSearchPane().setTextFieldSearchBy(elementsSearchFunctions);
            }

            // name function
            if (elementsNameFunction != null) {
                containerElement.getComboBoxNode().setNameBy(elementsNameFunction);
                searchDialog.getSearchPane().setNameBy(elementsNameFunction);
            }

            // not null option
            containerElement.getComboBoxNode().valueProperty().addListener((observableValue, oldV, newV) -> {
                if (isElementsNotNull() && newV == null) {
                    containerElement.setSelectedElement(oldV);
                }
            });

            // unique elements option
            containerElement.getComboBoxNode().showingProperty().addListener((observableValue, oldV, newV) -> {
                if (isUniqueElements() && newV != oldV) {
                    T oldValue = containerElement.getSelectedElement();
                    if (newV) {
                        FilteredList<T> subFilteredList = new FilteredList<>(elements, createUniqueElementsPredicate(containerElement));
                        SearchComboBox<T> comboBoxNode = containerElement.getComboBoxNode();
                        comboBoxNode.setCollection(subFilteredList);
                        // because listeners work in order need to set prompt and text again (search combo box did it already thou)
                        comboBoxNode.setPromptText(comboBoxNode.getConverter().toString(oldValue));
                        comboBoxNode.getEditor().setText("");
                    }
                }
            });

            // extended custom options
            if (getExtendedOptions() != null) {
                containerElement.setExtendedOptions(getExtendedOptions());
            }

            // extended search
            containerElement.setExtendedSearch(true);
            if (searchDialog != null) {
                containerElement.setSearchDialog(searchDialog);
            }

            // extended search popup
            if (extendedSearchPopupFunction != null) {
                containerElement.getSearchDialog().getSearchPane().setPopupFunction(extendedSearchPopupFunction);
            } else if (popupBuilder != null) {
                SearchPane<T>.PopupBuilder searchPanePopupBuilder = containerElement.getSearchDialog().getSearchPane().popupBuilder();
                popupBuilder.functions.forEach(function -> function.apply(searchPanePopupBuilder));
                searchPanePopupBuilder.apply();
            }

            // creating result container element
            containerElement.setSelectedElement(element);
            vBox.getChildren().add(index, containerElement);
            return containerElement;
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new IllegalArgumentException("elementType field instanced with class that does not meet the requirements.");
        }
    }

    private void createSearchDialog() {
        createSearchDialog(false);
    }

    private void createSearchDialog(boolean forceReload) {
        if (searchDialog == null || forceReload) {
            if (loader != null) {
                try {
                    if (extraSearchNode == null) {
                        extraSearchNode = loader.load();
                    }
                    if (extraSearchController == null) {
                        extraSearchController = loader.getController();
                    }
                } catch (IOException e) {
                    throw new IllegalStateException("Loader was not set properly.");
                }
            } else if (extendedSearchExtraNodeClass != null) {
                try {
                    extraSearchNode = extendedSearchExtraNodeClass.getDeclaredConstructor().newInstance();
                    extraSearchController = (C) extraSearchNode;
                }
                catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    throw new IllegalArgumentException("Can not load class for extended search");
                }
            }
            searchDialog = new SearchDialog<>(elements);
            if (extraSearchNode != null) {
                searchDialog.getSearchPane().addExtraSearchNode(extraSearchNode);
                searchDialog.getSearchPane().setSearchOptions(
                        elementsSearchExtendedFunctions.entrySet().stream()
                                .collect(Collectors.toMap(entry -> entry.getKey().apply(extraSearchController), Map.Entry::getValue, (a, b) -> a, HashMap::new)));
            }
        }
    }

    public void setExtendedSearchOptions(Map<Function<C, Control>, Function<T, ?>> functionMap) {
        this.elementsSearchExtendedFunctions = functionMap;
        createSearchDialog(true);
    }

    public void setExtendedSearchOptions(Class<? extends Node> extendedOptionsNode, Map<Function<C, Control>, Function<T, ?>> functionMap) {
        setExtendedSearchOptionsNode(extendedOptionsNode);
        setExtendedSearchOptions(functionMap);
        createSearchDialog(true);
    }

    public void setExtendedSearchOptions(FXMLLoader loader, Map<Function<C, Control>, Function<T, ?>> functionMap) {
        setExtendedSearchOptionsNode(loader);
        setExtendedSearchOptions(functionMap);
        createSearchDialog(true);
    }

    public void addExtendedSearchOption(Function<C, Control> controlGetter, Function<T, ?> elementGetter) {
        elementsSearchExtendedFunctions.put(controlGetter, elementGetter);
        if (searchDialog == null) {
            createSearchDialog();
        }
        else {
            searchDialog.getSearchPane().addSearchOption(controlGetter.apply(extraSearchController), elementGetter);
        }
    }

    public void removeExtendedSearchOption(Function<C, Control> controlGetter) {
        elementsSearchExtendedFunctions.remove(controlGetter);
    }

    public void setExtendedSearchOptionsNode(Class<? extends Node> extendedOptionsNode) {
        this.loader = null;
        this.extendedSearchExtraNodeClass = extendedOptionsNode;
    }

    public void setExtendedSearchOptionsNode(FXMLLoader extendedOptionsNodeLoader) {
        this.loader = extendedOptionsNodeLoader;
        this.extendedSearchExtraNodeClass = null;
    }

    public void setExtendedSearchPopupFunction(Function<T, Node> function) {
        this.extendedSearchPopupFunction = function;
        this.popupBuilder = null;
    }

    public ExtendedSearchPopupBuilder extendedSearchPopupBuilder() {
        if (popupBuilder == null) {
            popupBuilder = new ExtendedSearchPopupBuilder();
        }
        return popupBuilder;
    }

    public class ExtendedSearchPopupBuilder {

        private List<Function<SearchPane<T>.PopupBuilder, ?>> functions;
        private String popupStyle;
        private String textStyle;

        ExtendedSearchPopupBuilder() {
            functions = new ArrayList<>();
        }

        public ExtendedSearchPopupBuilder addText(String text) {
            functions.add(builder -> builder.addText(text));
            return this;
        }

        public ExtendedSearchPopupBuilder addText(Label label) {
            functions.add(builder -> builder.addText(label));
            return this;
        }

        public ExtendedSearchPopupBuilder addText(Function<T, String> function) {
            functions.add(builder -> builder.addText(function));
            return this;
        }

        public ExtendedSearchPopupBuilder addNode(Function<T, Node> function) {
            functions.add(builder -> builder.addNode(function));
            return this;
        }

        public void apply() {
            extendedSearchPopupFunction = null;
        }

        public ExtendedSearchPopupBuilder clear() {
            functions.clear();
            return this;
        }

        public ExtendedSearchPopupBuilder setStyle(String value) {
            this.popupStyle = value;
            return this;
        }

        public void setTextStyle(String textStyle) {
            this.textStyle = textStyle;
        }

        List<Function<SearchPane<T>.PopupBuilder, ?>> getFunctions() {
            return functions;
        }

        String getPopupStyle() {
            return popupStyle;
        }

        String getTextStyle() {
            return textStyle;
        }
    }

}
