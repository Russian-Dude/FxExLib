package ru.rdude.fxlib.containers;

import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;

import java.util.ArrayList;
import java.util.Collection;

public class MultipleChoiceContainerExtendedElement<T> extends MultipleChoiceContainerElement<T> {

    private Button adderButton;
    private SearchDialog<T> searchDialog;

    public MultipleChoiceContainerExtendedElement() {
        this(new ArrayList<>());
    }

    public MultipleChoiceContainerExtendedElement(Collection<T> collection) {
        super(collection);
        this.adderButton = new Button("O");
        getChildren().add(0, adderButton);
        searchDialog = new SearchDialog<>(collection);
        adderButton.setOnAction(event -> searchDialog.showAndWait().ifPresent(this::setSelectedElement));
    }
}
