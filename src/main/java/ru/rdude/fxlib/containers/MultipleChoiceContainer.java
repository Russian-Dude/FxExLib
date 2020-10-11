package ru.rdude.fxlib.containers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class MultipleChoiceContainer<T> extends ScrollPane {

    private Collection<T> availableElements;
    @FXML
    private Button addButton;
    @FXML
    private VBox vBox;

    public MultipleChoiceContainer() {
        this(new ArrayList<>());
    }

    public MultipleChoiceContainer(Collection<T> availableElements) {
        super();
        this.availableElements = availableElements;
        vBox = new VBox();
        addButton = new Button("+");
        addButton.setOnAction(
                action -> {
                    MultipleChoiceContainerElement<T> element = new MultipleChoiceContainerElement<>(availableElements);
                    vBox.getChildren().add(vBox.getChildren().size() - 1, element);
                });
        vBox.getChildren().add(addButton);
        getChildren().add(vBox);
    }

    public void setAvailableElements(Collection<T> availableElements) {
        this.availableElements = availableElements;
        getNodesElements().forEach(element -> element.setElements(availableElements));
    }

    public List<T> getElements() {
        return getNodesElements().stream()
                .map(MultipleChoiceContainerElement::getSelectedElement)
                .collect(Collectors.toList());
    }

    public List<MultipleChoiceContainerElement<T>> getNodesElements() {
        return vBox.getChildren().stream()
                .filter(child -> child instanceof MultipleChoiceContainerElement)
                .map(child -> (MultipleChoiceContainerElement<T>) child)
                .collect(Collectors.toList());
    }

}
