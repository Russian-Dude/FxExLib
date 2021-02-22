package ru.rdude.fxlib.pickers;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import ru.rdude.fxlib.dialogs.SearchDialog;

import java.io.File;
import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class ElementPicker<T, N extends Node> extends Control {

    private final ObjectProperty<T> element = new SimpleObjectProperty<>();
    private final N node;
    private BiConsumer<N, T> elementToNode;
    private BorderPane borderPane = new BorderPane();
    private VBox buttonsBox = new VBox();

    public ElementPicker(N mainNode, BiConsumer<N, T> elementToNode) {
        this.getChildren().add(borderPane);
        borderPane.setBottom(buttonsBox);
        borderPane.setCenter(mainNode);
        this.elementToNode = elementToNode;
        this.node = mainNode;
        element.addListener((change, oldV, newV) -> {
            elementToNode.accept(node, newV);
        });
    }

    public T getElement() {
        return element.get();
    }

    public N getNode() {
        return node;
    }

    public void addLoadFromFileButton(FileChooser fileChooser, Function<File, T> fileToElement, Button button, Collection<T>... alsoAddTo) {
        buttonsBox.getChildren().add(button);
        button.setOnAction(event -> {
            final File file = fileChooser.showOpenDialog(button.getScene().getWindow());
            if (file != null && file.exists()) {
                element.set(fileToElement.apply(file));
                for (Collection<T> addTo : alsoAddTo) {
                    addTo.add(element.get());
                }
            }
        });
    }

    public void addLoadFromButton(Collection<T> collection, Button button) {
        addLoadFromDialogButton(new SearchDialog<>(collection), button);
    }

    public void addLoadFromDialogButton(SearchDialog<T> searchDialog, Button button) {
        button.setOnAction(event -> searchDialog.showAndWait().ifPresent(element::set));
    }

    public <V> void addLoadFromButton(Collection<V> collection, Function<V, T> converter, Button button) {
        addLoadFromDialogButton(new SearchDialog<>(collection), converter, button);
    }

    public <V> void addLoadFromDialogButton(SearchDialog<V> searchDialog, Function<V, T> converter, Button button) {
        button.setOnAction(event -> searchDialog.showAndWait().ifPresent(v -> element.set(converter.apply(v))));
    }
}
