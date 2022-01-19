package ru.rdude.fxlib.containers.elementsholder;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import ru.rdude.fxlib.boxes.SearchComboBox;

import java.util.function.Function;
import java.util.function.Supplier;

public class ElementsHolder <T extends Node> extends ScrollPane {

    private final ObservableList<T> elements = FXCollections.observableArrayList();
    private final ObservableList<NodeContainer> containers = FXCollections.observableArrayList();
    private final Supplier<T> elementNodeCreator;
    private Function<T, ? extends Button> customDeleteButton;

    private final VBox elementsVbox = new VBox();
    private final Button addButton = new Button("+");

    private static final String DEFAULT_STYLE_CLASS = "fxex-elements-holder";
    private static final String ADD_BUTTON_STYLE_CLASS = "fxex-elements-holder-add-button";
    private static final String REMOVE_BUTTON_STYLE_CLASS = "fxex-elements-holder-remove-button";

    public ElementsHolder(Supplier<T> elementNodeCreator) {
        super();
        this.elementNodeCreator = elementNodeCreator;

        this.getStyleClass().add(DEFAULT_STYLE_CLASS);
        addButton.getStyleClass().add(ADD_BUTTON_STYLE_CLASS);

        this.setFitToWidth(true);
        VBox vBox = new VBox();
        vBox.getChildren().add(elementsVbox);
        vBox.getChildren().add(addButton);
        addButton.setMaxWidth(Double.POSITIVE_INFINITY);
        vBox.setFillWidth(true);
        setContent(vBox);

        addButton.setOnAction(event -> add());
        elements.addListener((ListChangeListener<T>) change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    change.getAddedSubList().forEach(t ->
                            vBox.getChildren().add(vBox.getChildren().size() - 1, new NodeContainer(t)));
                }
                else if (change.wasRemoved()) {
                    change.getRemoved().forEach(t -> {
                        vBox.getChildren().stream()
                                .filter(node -> node.getClass().equals(NodeContainer.class))
                                .filter(node -> ((NodeContainer) node).elementNode.equals(t))
                                .findAny()
                                .ifPresent(vBox.getChildren()::remove);
                    });
                }
            }
        });
    }

    public T add() {
        final T node = elementNodeCreator.get();
        elements.add(node);
        return node;
    }

    public T add(T t) {
        elements.add(t);
        return t;
    }

    public boolean remove(T t) {
        return elements.remove(t);
    }

    public ObservableList<T> getElements() {
        return elements;
    }

    public Supplier<T> getElementNodeCreator() {
        return elementNodeCreator;
    }

    public void setCustomDeleteButton(Function<T, ? extends Button> customDeleteButton) {
        this.customDeleteButton = customDeleteButton;
    }

    public Function<T, ? extends Button> getCustomDeleteButton() {
        return customDeleteButton;
    }

    public Button getAddButton() {
        return addButton;
    }

    private class NodeContainer extends BorderPane {

        T elementNode;

        public NodeContainer(T elementNode) {
            this.elementNode = elementNode;
            setMinWidth(0d);
            setCenter(elementNode);
            if (elementNode instanceof SearchComboBox) {
                ((SearchComboBox<?>) elementNode).setMaxWidth(Double.MAX_VALUE);
            }
            Button deleteButton;
            if (customDeleteButton == null) {
                deleteButton = new Button("X");
                deleteButton.getStyleClass().add(REMOVE_BUTTON_STYLE_CLASS);
                deleteButton.setMinWidth(5);
                deleteButton.setOnAction(action -> remove(elementNode));
            }
            else {
                deleteButton = customDeleteButton.apply(elementNode);
                EventHandler<ActionEvent> onAction = deleteButton.getOnAction();
                deleteButton.setOnAction(action -> {
                    if (onAction != null) {
                        onAction.handle(action);
                    }
                    remove(elementNode);
                });
            }
            setRight(deleteButton);
        }
    }

}
