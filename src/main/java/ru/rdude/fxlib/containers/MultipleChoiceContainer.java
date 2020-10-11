package ru.rdude.fxlib.containers;

import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Uses to contain multiple elements of provided collection.
 * New elements can be dynamically added and deleted from this container.
 * Type of the node that represent chosen element can be set as class extended from MultipleChoiceContainerElement.
 * @param <T> type of the elements.
 */
public class MultipleChoiceContainer<T> extends ScrollPane {

    /**
     * Collection of the elements every added visual node can chose from.
     * If collection is empty no visual nodes can be added.
     */
    private Collection<T> availableElements;
    /**
     * Button to add new visual node representing element.
     */
    private Button addButton;
    /**
     * Vertical box uses to hold the add button and element visual nodes.
     */
    private VBox vBox;
    /**
     * Class that will be used to visually represent every selected element.
     * MultipleChoiceContainerElement by default.
     */
    private Class<? extends MultipleChoiceContainerElement> elementType;



    /**
     * Empty constructor with no elements to chose from.
     */
    public MultipleChoiceContainer() {
        this(new ArrayList<>());
    }

    /**
     * Constructor with provided elements to chose from.
     * @param availableElements elements to chose from. If empty no visual nodes can be added.
     */
    public MultipleChoiceContainer(Collection<T> availableElements) {
        super();
        this.availableElements = availableElements;
        vBox = new VBox();
        addButton = new Button("+");
        elementType = MultipleChoiceContainerElement.class;
        // max width set to double max value so button will be always stretched to pane size
        addButton.setMaxWidth(Double.MAX_VALUE);
        // add new element when add button pressed
        addButton.setOnAction(actionEvent -> addElement());
        vBox.getChildren().add(addButton);
        setContent(vBox);
    }

    /**
     * Set available elements to chose from.
     * @param availableElements elements to chose from. If empty no visual nodes can be added.
     */
    public void setAvailableElements(Collection<T> availableElements) {
        this.availableElements = availableElements;
        getNodesElements().forEach(element -> element.setElements(availableElements));
    }

    /**
     * Get selected elements. To get visual nodes of elements use getNodesElements method.
     * @return list of selected elements.
     * @see #getNodesElements()
     */
    public List<T> getElements() {
        return getNodesElements().stream()
                .map(MultipleChoiceContainerElement::getSelectedElement)
                .collect(Collectors.toList());
    }

    /**
     * Get visual nodes of this selected elements. To get only elements use getElements method.
     * @return list of visual nodes for each element. Every node is MultipleChoiceContainerElement type or
     * extends from it.
     * @see #getElements()
     * @see MultipleChoiceContainerElement
     */
    public List<MultipleChoiceContainerElement<T>> getNodesElements() {
        return vBox.getChildren().stream()
                .filter(child -> child instanceof MultipleChoiceContainerElement)
                .map(child -> (MultipleChoiceContainerElement<T>) child)
                .collect(Collectors.toList());
    }

    /**
     * Add new visual node element if collection of available elements is not empty.
     */
    public void addElement() {
        availableElements.stream()
                .findFirst()
                .ifPresent(this::addElement);
    }

    /**
     * Add new visual node element with provided element value to the last position before the add button.
     * @param element element to add.
     */
    public void addElement(T element) {
        addElement(vBox.getChildren().size() - 1, element);
    }

    /**
     * Add new visual node element with provided element value to the specified position.
     * @param element element to add.
     * @param index index to add to. Note that the add button is in the last index by default.
     */
    public void addElement(int index, T element) {
        try {
            MultipleChoiceContainerElement<T> containerElement = elementType.getDeclaredConstructor(Collection.class).newInstance(availableElements);
            containerElement.setSelectedElement(element);
            vBox.getChildren().add(index, containerElement);
        }
        catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    /**
     * Set visual node type that represent element.
     * @param elementType class extended MultipleChoiceContainerElement
     */
    public void setNodeElementType(Class<? extends MultipleChoiceContainerElement> elementType) {
        this.elementType = elementType;
    }

}
