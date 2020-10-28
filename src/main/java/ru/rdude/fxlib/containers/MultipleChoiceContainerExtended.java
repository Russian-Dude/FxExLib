package ru.rdude.fxlib.containers;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Extended version of MultipleChoiceContainer to use with big amount of available elements.
 * Every element of this container has Search Dialog that helps to find elements with filters.
 * This search dialog can be customised by with linking Control nodes getters with element's class getters.
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
    private FXMLLoader loader;


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
            MultipleChoiceContainerElement<T> containerElement = elementType.getDeclaredConstructor(Collection.class).newInstance(availableElements);
            if (elementsSearchFunctions != null) {
                containerElement.getComboBoxNode().setSearchBy(elementsSearchFunctions);
                containerElement.getSearchDialog().getSearchPane().setTextFieldSearchBy(elementsSearchFunctions);
            }
            if (elementsNameFunction != null) {
                containerElement.getComboBoxNode().setNameBy(elementsNameFunction);
                containerElement.getSearchDialog().getSearchPane().setNameBy(elementsNameFunction);
            }
            containerElement.setExtendedSearch(true);
            Node extraSearchNode = null;
            C controller = null;
            if (loader != null) {
                try {
                    extraSearchNode = loader.load();
                    controller = loader.getController();
                } catch (IOException e) {
                    throw new IllegalStateException("Loader was not set properly.");
                }
            }
            else if (extendedSearchExtraNodeClass != null) {
                extraSearchNode = extendedSearchExtraNodeClass.getDeclaredConstructor().newInstance();
                controller = (C) extraSearchNode;
            }
            if (extraSearchNode != null) {
                containerElement.getSearchDialog().getSearchPane().getExtraPane().getChildren().add(extraSearchNode);
                C finalController = controller;
                containerElement.getSearchDialog().getSearchPane().addSearchOptions(
                        elementsSearchExtendedFunctions.entrySet().stream()
                                .collect(Collectors.toMap(entry -> entry.getKey().apply(finalController), Map.Entry::getValue, (a, b) -> a, HashMap::new)));
            }
            if (getExtendedOptions() != null) {
                containerElement.setExtendedOptions(getExtendedOptions());
            }
            containerElement.setSelectedElement(element);
            vBox.getChildren().add(index, containerElement);
            return containerElement;
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new IllegalArgumentException("elementType field instanced with class that does not meet the requirements.");
        }
    }


    public void setExtendedSearchOptions(Map<Function<C, Control>, Function<T, ?>> functionMap) {
        this.elementsSearchExtendedFunctions = functionMap;
    }

    public void setExtendedSearchOptions(Class<? extends Node> extendedOptionsNode, Map<Function<C, Control>, Function<T, ?>> functionMap) {
        setExtendedSearchOptionsNode(extendedOptionsNode);
        setExtendedSearchOptions(functionMap);
    }

    public void setExtendedSearchOptions(FXMLLoader loader, Map<Function<C, Control>, Function<T, ?>> functionMap) {
        setExtendedSearchOptionsNode(loader);
        setExtendedSearchOptions(functionMap);
    }

    public void addExtendedSearchOption(Function<C, Control> controlGetter, Function<T, ?> elementGetter) {
        elementsSearchExtendedFunctions.put(controlGetter, elementGetter);
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

}
