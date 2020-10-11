package ru.rdude.fxlib.containers;

import javafx.collections.FXCollections;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Basic element holder for MultipleChoiceContainer.
 * If extend this class constructor with provided collection must be implemented so MultipleChoiceContainer could use this class.
 * @param <T> type of the elements.
 */
public class MultipleChoiceContainerElement<T> extends Pane {

    /**
     * This ComboBox holds available elements to chose from.
     */
    private ComboBox<T> elements;
    /**
     * Remove button to remove this holder from a parent if parent extends Pane class.
     * @see Pane
     */
    private Button removeButton;
    /**
     * Horizontal Box holds inside nodes.
     */
    private HBox hBox;

    /**
     * Empty constructor initializes empty array as available elements.
     */
    public MultipleChoiceContainerElement() {
        this(new ArrayList<>());
    }

    /**
     * Constructor that specifies available elements.
     * @param collection collection of available elements.
     */
    public MultipleChoiceContainerElement(Collection<T> collection) {
        super();
        elements = new ComboBox<T>();
        removeButton = new Button("X");
        removeButton.setOnAction(action -> removeFromParent());
        hBox = new HBox(elements, removeButton);
        getChildren().add(hBox);
        setElements(collection);
    }

    /**
     * Set available elements to chose from.
     * @param collection collection of available elements.
     */
    public void setElements(Collection<T> collection) {
        elements.setItems(FXCollections.observableList(new ArrayList<T>(collection)));
    }

    /**
     * Get selected element.
     * @return selected element.
     */
    public T getSelectedElement() {
        return elements.getValue();
    }

    /**
     * Set selected element.
     * @param element element to be selected.
     */
    public void setSelectedElement(T element) {
        elements.setValue(element);
    }

    /**
     * Remove instance of this class from a parent if parent extends from Pane class.
     */
    public void removeFromParent() {
        if (getParent() instanceof Pane) {
            ((Pane) getParent()).getChildren().remove(this);
        }
    }
}
