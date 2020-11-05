package ru.rdude.fxlib.containers;

import javafx.scene.control.TitledPane;

import java.util.*;
import java.util.function.Function;

/**
 * This is extension to MultipleChoiceContainer class.
 * Can be collapsed and have a title since extends TitledPane.
 * If collapsed with more than 0 elements inside, can add amount of this elements to the title.
 * Uses to contain multiple elements of provided collection.
 * New elements can be dynamically added and deleted from this container.
 * Type of the node that represent chosen element can be set as class extended from MultipleChoiceContainerElement.
 * @param <T> type of the elements.
 */
public class TitledMultipleChoiceContainer<T> extends TitledPane {

    /**
     * If true adds elements count to the title when collapsed (if > 0).
     */
    private boolean showElementsCount;
    /**
     * Title.
     */
    private String title;
    /**
     * MultipleChoiceContainer methods delegates to this container.
     */
    private MultipleChoiceContainer<T> multipleChoiceContainer;


    public TitledMultipleChoiceContainer() {
        this(new ArrayList<>(), "");
    }

    public TitledMultipleChoiceContainer(String title) {
        this(new ArrayList<>(), title);
    }

    public TitledMultipleChoiceContainer(Collection<T> availableElements) {
        this(availableElements, "");
    }

    public TitledMultipleChoiceContainer(Collection<T> availableElements, String title) {
        super();
        showElementsCount = true;
        multipleChoiceContainer = new MultipleChoiceContainer<>(availableElements);
        setContent(multipleChoiceContainer);
        this.title = title;
        setText(title);
        expandedProperty().addListener((observable, oldV, newV) -> {
            if (!newV && showElementsCount && getNodesElements().size() > 0) {
                setText(title + " (" + getNodesElements().size() + ")");
            }
            else {
                setText(title);
            }
        });
    }


    /**
     * If true adds elements count to the title when collapsed (if > 0).
     */
    public boolean isShowElementsCount() {
        return showElementsCount;
    }

    /**
     * If true adds elements count to the title when collapsed (if > 0).
     */
    public void setShowElementsCount(boolean showElementsCount) {
        this.showElementsCount = showElementsCount;
    }


    /**
     * Get visual element type. If visualElementType field does not represent current element type try to find it and set this value.
     * @return enum value representing current visual type.
     */
    public MultipleChoiceContainer.VisualElementType getVisualElementType() {
        return multipleChoiceContainer.getVisualElementType();
    }

    /**
     * Set visual element type.
     <<<<<<< HEAD
     * Another way to set visual element type is setNodeElementType method which allows to use custom element types.
     * @see #setNodeElementType(Class)
    =======
    >>>>>>> bd4446c65870ef91a97a8f50ab424bd67df54761
     * @param visualElementType visual element type.
     */
    public void setVisualElementType(MultipleChoiceContainer.VisualElementType visualElementType) {
        multipleChoiceContainer.setVisualElementType(visualElementType);
    }

    /**
     * Set available elements to chose from.
     * @param availableElements elements to chose from. If empty no visual nodes can be added.
     */
    public void setAvailableElements(Collection<T> availableElements) {
        multipleChoiceContainer.setAvailableElements(availableElements);
    }

    /**
     * Get selected elements. To get visual nodes of elements use getNodesElements method.
     * @return list of selected elements.
     * @see #getNodesElements()
     */
    public List<T> getElements() {
        return multipleChoiceContainer.getElements();
    }

    public List<T> getValue() {
        return multipleChoiceContainer.getValue();
    }

    /**
     * Get visual nodes of this selected elements. To get only elements use getElements method.
     * @return list of visual nodes for each element. Every node is MultipleChoiceContainerElement type or
     * extends from it.
     * @see #getElements()
     * @see MultipleChoiceContainerElement
     */
    public List<MultipleChoiceContainerElement<T>> getNodesElements() {
        return multipleChoiceContainer.getNodesElements();
    }

    /**
     * Add new visual node element if collection of available elements is not empty and return Optional of this visual node.
     * Added element is the first element of the collection.
     * @return Optional of new visual node element.
     */
    public Optional<MultipleChoiceContainerElement<T>> addElement() {
        return multipleChoiceContainer.addElement();
    }

    /**
     * Add new visual node element with provided element value to the last position before the add button.
     * @param element element to add.
     * @return created visual node for element.
     */
    public MultipleChoiceContainerElement<T> addElement(T element) {
        return multipleChoiceContainer.addElement(element);
    }

    /**
     * Add new visual node element with provided element value to the specified position.
     * @param index index to add to. Note that the add button is in the last index by default.
     * @param element element to add.
     * @return created visual node for element.
     */
    public MultipleChoiceContainerElement<T> addElement(int index, T element) {
        return multipleChoiceContainer.addElement(index, element);
    }

    public Object[] getExtendedOptions() {
        return multipleChoiceContainer.getExtendedOptions();
    }

    public void setExtendedOptions(Object... extendedOptions) {
        multipleChoiceContainer.setExtendedOptions(extendedOptions);
    }

    /**
     * Set visual node type that represent element.
     * @param elementType class extended MultipleChoiceContainerElement
     */
    public void setNodeElementType(Class<? extends MultipleChoiceContainerElement> elementType) {
        multipleChoiceContainer.setNodeElementType(elementType);
    }

    @SafeVarargs
    public final void setSearchBy(Function<T, String>... elementsSearchFunctions) {
        multipleChoiceContainer.setSearchBy(elementsSearchFunctions);
    }

    public void setSearchBy(Set<Function<T, String>> elementsSearchFunctions) {
        multipleChoiceContainer.setSearchBy(elementsSearchFunctions);
    }

    public void setNameBy(Function<T, String> elementsNameFunction) {
        multipleChoiceContainer.setNameBy(elementsNameFunction);
    }

    public void setTitle(String title) {
        this.title = title;
        setText(title);
    }
}
