package ru.rdude.fxlib.containers;

import javafx.scene.control.Button;

import java.util.ArrayList;
import java.util.Collection;

public class MultipleChoiceContainerExtendedElement<T> extends MultipleChoiceContainerElement<T> {

    private Button adderButton;

    /**
     * Empty constructor initializes empty array as available elements.
     */
    public MultipleChoiceContainerExtendedElement() {
        this(new ArrayList<>());
    }

    /**
     * Constructor that specifies available elements.
     *
     * @param collection collection of available elements.
     */
    public MultipleChoiceContainerExtendedElement(Collection<T> collection) {
        super(collection);
        this.adderButton = new Button("ADD");
        getChildren().add(0, adderButton);
    }
}
