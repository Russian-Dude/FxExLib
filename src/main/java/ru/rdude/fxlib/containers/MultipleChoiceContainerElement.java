package ru.rdude.fxlib.containers;

import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import ru.rdude.fxlib.boxes.SearchComboBox;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Basic element holder for MultipleChoiceContainer.
 * If extend this class constructor with provided collection must be implemented so MultipleChoiceContainer could use this class.
 * @param <T> type of the elements.
 */
public class MultipleChoiceContainerElement<T> extends HBox {

    /**
     * This ComboBox holds available elements to chose from.
     */
    private SearchComboBox<T> elements;
    /**
     * Remove button to remove this holder from a parent if parent extends Pane class.
     * @see Pane
     */
    private Button removeButton;
    /**
     * Button to open extended search dialog.
     */
    private Button searchButton;
    /**
     * Extended search dialog.
     */
    private SearchDialog<T> searchDialog;
    /**
     * Is extended search on and search button visible.
     */
    private boolean extendedSearch;

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
        extendedSearch = false;
        elements = new SearchComboBox<>();
        searchButton = new Button("...");
        removeButton = new Button("X");
        removeButton.setOnAction(action -> removeFromParent());
        searchButton.setOnAction(action -> searchDialog.showAndWait().ifPresent(this::setSelectedElement));
        searchDialog = new SearchDialog<>(collection);
        getChildren().add(elements);
        getChildren().add(removeButton);
        elements.setMaxWidth(Double.MAX_VALUE);
        setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(elements, Priority.ALWAYS);
        HBox.setHgrow(removeButton, Priority.ALWAYS);
        setAlignment(Pos.CENTER);
        setElements(collection);
    }

    /**
     * Set available elements to chose from.
     * @param collection collection of available elements.
     */
    public void setElements(Collection<T> collection) {
        elements.setCollection(collection);
        if (searchDialog != null) {
            searchDialog.setCollection(collection);
        }
    }

    public boolean isExtendedSearch() {
        return extendedSearch;
    }

    public void setExtendedSearch(boolean extendedSearch) {
        this.extendedSearch = extendedSearch;
        if (extendedSearch && searchDialog == null) {
            searchDialog = new SearchDialog<>(elements.getItems());
        }
        if (extendedSearch && !getChildren().contains(searchButton)) {
            getChildren().add(0, searchButton);
        }
        else if (!extendedSearch) {
            getChildren().remove(searchButton);
        }
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

    public void setExtendedOptions(Object... options) {}

    public SearchComboBox<T> getComboBoxNode() {
        return elements;
    }

    public Button getRemoveButton() {
        return removeButton;
    }

    public Button getSearchButton() {
        return searchButton;
    }

    public SearchDialog<T> getSearchDialog() {
        return searchDialog;
    }
}
