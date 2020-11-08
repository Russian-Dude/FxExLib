package ru.rdude.fxlib.containers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import ru.rdude.fxlib.boxes.SearchComboBox;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Uses to contain multiple elements of provided collection.
 * New elements can be dynamically added and deleted from this container.
 * Type of the node that visually represent chosen element can be set as class extended from MultipleChoiceContainerElement.
 *
 * @param <T> type of the elements.
 */
public class MultipleChoiceContainer<T> extends ScrollPane implements ValueProvider<List<T>> {

    /**
     * This enum used to represent visual element types in Scene Builder.
     */
    public enum VisualElementType {
        BASIC(MultipleChoiceContainerElement.class),
        WITH_TEXT_FIELD(MultipleChoiceContainerElementWithTextField.class),
        WITH_SIMPLE_AUTOFILL_TEXT_FIELD(MultipleChoiceContainerElementWithSimpleAutofillTextField.class),
        WITH_AUTOFILL_TEXT_FIELD(MultipleChoiceContainerElementWithAutofillTextField.class),
        PERCENT_TEXT_FIELD(MultipleChoiceContainerElementWithPercents.class),
        WITH_TWO_VALUES(MultipleChoiceContainerElementTwoChoice.class),
        WITH_TWO_VALUES_AND_TEXT_FIELD(MultipleChoiceContainerElementTwoChoiceWithTextField.class),
        WITH_TWO_VALUES_AND_PERCENTS(MultipleChoiceContainerElementTwoChoiceWithPercents.class);

        private final Class<? extends MultipleChoiceContainerElement> cl;

        VisualElementType(Class<? extends MultipleChoiceContainerElement> cl) {
            this.cl = cl;
        }

        public Class<? extends MultipleChoiceContainerElement> getCl() {
            return cl;
        }
    }

    /**
     * This field allows to set visual element type in Scene Builder.
     * Another way to set visual element type is setNodeElementType method.
     *
     * @see #setNodeElementType(Class)
     */
    private VisualElementType visualElementType;
    /**
     * Collection of the elements every added visual node can chose from.
     * If collection is empty no visual nodes can be added.
     */
    protected ObservableList<T> elements;
    /**
     * Button to add new visual node representing element.
     */
    private Button addButton;
    /**
     * Vertical box uses to hold the add button and element visual nodes.
     */
    protected VBox vBox;
    /**
     * Class that will be used to visually represent every selected element.
     * MultipleChoiceContainerElement by default.
     */
    protected Class<? extends MultipleChoiceContainerElement> elementType;
    /**
     * Search functions of SearchComboBox for every created element
     */
    protected Set<Function<T, String>> elementsSearchFunctions;
    /**
     * Naming function of SearchComboBox for every created element
     */
    protected Function<T, String> elementsNameFunction;
    /**
     * If children elements need some extended option to initialize when created this is used.
     */
    private Object[] extendedOptions;
    /**
     * If true only one copy of each element available to chose from
     */
    private boolean uniqueElements;
    /**
     * If true elements value can not be null
     */
    private boolean notNull;


    /**
     * Empty constructor with no elements to chose from.
     */
    public MultipleChoiceContainer() {
        this(new ArrayList<>());
    }

    /**
     * Constructor with provided elements to chose from.
     * Use observable list to let this container dynamically update available elements.
     *
     * @param elements elements to chose from. If empty no visual nodes can be added.
     */
    public MultipleChoiceContainer(Collection<T> elements) {
        super();
        visualElementType = VisualElementType.BASIC;
        if (elements instanceof ObservableList) {
            this.elements = (ObservableList<T>) elements;
        } else {
            this.elements = FXCollections.observableArrayList(elements);
        }
        setUniqueElements(false);
        notNull = true;
        vBox = new VBox();
        addButton = new Button("+");
        elementType = MultipleChoiceContainerElement.class;
        // button max width set to double max value so button will be always stretched to pane size
        addButton.setMaxWidth(Double.MAX_VALUE);
        // add new element when add button pressed
        addButton.setOnAction(actionEvent -> addElement());
        vBox.getChildren().add(addButton);
        vBox.setMaxWidth(Double.MAX_VALUE);
        vBox.setFillWidth(true);
        setFitToWidth(true);
        setContent(vBox);
    }

    /**
     * Get visual element type. If visualElementType field does not represent current element type try to find it and set this value.
     *
     * @return enum value representing current visual type.
     */
    public VisualElementType getVisualElementType() {
        if (!visualElementType.getCl().equals(elementType)) {
            for (VisualElementType value : VisualElementType.values()) {
                if (value.getCl().equals(elementType)) {
                    visualElementType = value;
                }
            }
        }
        return visualElementType;
    }

    /**
     * Set visual element type.
     * Another way to set visual element type is setNodeElementType method which allows to use custom element types.
     *
     * @param visualElementType visual element type.
     * @see #setNodeElementType(Class)
     */
    public void setVisualElementType(VisualElementType visualElementType) {
        this.visualElementType = visualElementType;
        setNodeElementType(visualElementType.getCl());
    }

    /**
     * Set available elements to chose from.
     * Use observable list to let this container dynamically update available elements.
     *
     * @param elements elements to chose from. If empty no visual nodes can be added.
     */
    public void setElements(Collection<T> elements) {
        if (elements instanceof ObservableList) {
            this.elements = (ObservableList<T>) elements;
        } else {
            this.elements = FXCollections.observableArrayList(elements);
        }
        getNodesElements().forEach(element -> element.setElements(elements));
    }

    /**
     * Get selected elements. To get visual nodes of elements use getNodesElements method.
     *
     * @return list of selected elements.
     * @see #getNodesElements()
     */
    public List<T> getElements() {
        return getNodesElements().stream()
                .map(MultipleChoiceContainerElement::getSelectedElement)
                .collect(Collectors.toList());
    }

    @Override
    public List<T> getValue() {
        return getElements();
    }

    /**
     * Get visual nodes of this selected elements. To get only elements use getElements method.
     *
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
     * Add new visual node element if collection of available elements is not empty and return Optional of this visual node.
     * Added element is the first element of the collection.
     *
     * @return Optional of new visual node element.
     */
    public Optional<MultipleChoiceContainerElement<T>> addElement() {
        return !notNull ? Optional.ofNullable(addElement(null)) :
                elements.stream()
                        .filter(t -> {
                            if (uniqueElements) {
                                return getNodesElements()
                                        .stream()
                                        .map(MultipleChoiceContainerElement::getSelectedElement)
                                        .allMatch(v -> v != t);
                            } else {
                                return true;
                            }
                        })
                        .findFirst()
                        .map(this::addElement);
    }

    /**
     * Add new visual node element with provided element value to the last position before the add button.
     *
     * @param element element to add.
     * @return created visual node for element.
     */
    public MultipleChoiceContainerElement<T> addElement(T element) {
        return addElement(vBox.getChildren().size() - 1, element);
    }

    /**
     * Add new visual node element with provided element value to the specified position.
     *
     * @param element element to add.
     * @param index   index to add to. Note that the add button is in the last index by default.
     * @return created visual node for element.
     */
    public MultipleChoiceContainerElement<T> addElement(int index, T element) {
        try {
            MultipleChoiceContainerElement<T> containerElement = elementType.getDeclaredConstructor(Collection.class).newInstance(elements);

            // search functions
            if (elementsSearchFunctions != null) {
                containerElement.getComboBoxNode().setSearchBy(elementsSearchFunctions);
                containerElement.getSearchDialog().getSearchPane().setTextFieldSearchBy(elementsSearchFunctions);
            }

            // name function
            if (elementsNameFunction != null) {
                containerElement.getComboBoxNode().setNameBy(elementsNameFunction);
                containerElement.getSearchDialog().getSearchPane().setNameBy(elementsNameFunction);
            }

            // not null option
            containerElement.getComboBoxNode().valueProperty().addListener((observableValue, oldV, newV) -> {
                if (isElementsNotNull() && newV == null) {
                    containerElement.setSelectedElement(oldV);
                }
            });

            // unique elements option
            containerElement.getComboBoxNode().showingProperty().addListener((observableValue, oldV, newV) -> {
                if (isUniqueElements() && newV != oldV) {
                    T oldValue = containerElement.getSelectedElement();
                    if (newV) {
                        FilteredList<T> subFilteredList = new FilteredList<>(elements, createUniqueElementsPredicate(containerElement));
                        SearchComboBox<T> comboBoxNode = containerElement.getComboBoxNode();
                        comboBoxNode.setCollection(subFilteredList);
                        // because listeners work in order need to set prompt and text again (search combo box did it already thou)
                        comboBoxNode.setPromptText(comboBoxNode.getConverter().toString(oldValue));
                        comboBoxNode.getEditor().setText("");
                    }
                }
            });

            // extended custom options
            if (getExtendedOptions() != null) {
                containerElement.setExtendedOptions(getExtendedOptions());
            }

            // creating result container element
            containerElement.setSelectedElement(element);
            vBox.getChildren().add(index, containerElement);
            return containerElement;
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new IllegalArgumentException("elementType field instanced with class that does not meet the requirements.");
        }
    }

    public Object[] getExtendedOptions() {
        return extendedOptions;
    }

    public void setExtendedOptions(Object... extendedOptions) {
        this.extendedOptions = extendedOptions;
    }

    /**
     * Set visual node type that represent element.
     *
     * @param elementType class extended MultipleChoiceContainerElement
     */
    public void setNodeElementType(Class<? extends MultipleChoiceContainerElement> elementType) {
        this.elementType = elementType;
    }

    @SafeVarargs
    public final void setSearchBy(Function<T, String>... elementsSearchFunctions) {
        this.elementsSearchFunctions = new HashSet<>(List.of(elementsSearchFunctions));
    }

    public void setSearchBy(Set<Function<T, String>> elementsSearchFunctions) {
        this.elementsSearchFunctions = elementsSearchFunctions;
    }

    public void setNameBy(Function<T, String> elementsNameFunction) {
        this.elementsNameFunction = elementsNameFunction;
    }

    public boolean isUniqueElements() {
        return uniqueElements;
    }

    public void setUniqueElements(boolean value) {
        this.uniqueElements = value;
    }

    public boolean isElementsNotNull() {
        return notNull;
    }

    public void setElementsNotNull(boolean value) {
        this.notNull = value;
    }

    protected Predicate<T> createUniqueElementsPredicate(MultipleChoiceContainerElement<T> keep) {
        return t -> (keep != null && keep.getSelectedElement() == t)
                || getNodesElements().stream().allMatch(nodeElement -> nodeElement.getSelectedElement() != t);
    }
}
