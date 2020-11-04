package ru.rdude.fxlib.containers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import ru.rdude.fxlib.boxes.SearchComboBox;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Multiple Choice Container element that can hold two Search Combo Boxes. Second combo box
 * settings allowed by setExtendedOptions method. This settings can be easier created with builder.
 * @param <T> main element type.
 * @param <V> second element type.
 */
public class MultipleChoiceContainerElementTwoChoice<T, V> extends MultipleChoiceContainerElement<T> {

    private SearchComboBox<V> secondValueComboBox;
    private SearchDialog<V> secondValueSearchDialog;
    private Button secondValueSearchButton;

    public MultipleChoiceContainerElementTwoChoice() {
        this(new ArrayList<>());
    }

    public MultipleChoiceContainerElementTwoChoice(Collection<T> collection) {
        super(collection);
        secondValueComboBox = new SearchComboBox<>();
        getChildren().add(1, secondValueComboBox);
    }

    /**
     * Pass 1, 3 or 5 options listed below.
     *
     * @param options first option - ExtendedOptionsBuilder or:
     *                first option - collection of the second value Search Combo Box (Collection);
     *                second option - nameBy function for Search Combo Box (Function V, String);
     *                third option - searchBy function for SearchComboBox (Function V, String);
     *                fourth option- node class for extended search (Class or FXMLLoader);
     *                fifth option - search options.
     */
    @Override
    public void setExtendedOptions(Object... options) {
        if (options[0] instanceof ExtendedOptionsBuilder) {
            ExtendedOptionsBuilder builder = (ExtendedOptionsBuilder) options[0];
            setExtendedOptions(builder.collection, builder.nameByFunction, builder.searchByFunction, builder.getLoaderOrClass(), builder.extendedSearchFunctions);
            return;
        }
        if (options[0] instanceof Collection) {
            secondValueComboBox.setCollection((Collection<V>) options[0]);
        }
        if (options.length >= 3 && options[1] instanceof Function && (options[2] instanceof Function || options[2] instanceof Collection)) {
            secondValueComboBox.setNameBy((Function<V, String>) options[1]);
            if (options[2] instanceof Function) {
                secondValueComboBox.setSearchBy((Function<V, String>) options[2]);
            } else {
                secondValueComboBox.setSearchBy((Collection<Function<V, String>>) options[2]);
            }
        }
        if (options.length == 5 && (options[3] instanceof Class || options[3] instanceof FXMLLoader)) {
            secondValueSearchDialog = new SearchDialog<>((Collection<V>) options[0]);
            secondValueSearchButton = new Button("...");
            secondValueSearchButton.setOnAction(action -> secondValueSearchDialog.showAndWait().ifPresent(v -> secondValueComboBox.setValue(v)));
            getChildren().add(getChildren().size() - 2, secondValueSearchButton);
            secondValueSearchDialog.getSearchPane().setNameBy((Function<V, String>) options[1]);
            if (options[2] instanceof Function) {
                secondValueSearchDialog.getSearchPane().setTextFieldSearchBy((Function<V, String>) options[2]);
            } else {
                secondValueSearchDialog.getSearchPane().setTextFieldSearchBy((Collection<Function<V, String>>) options[2]);
            }
            Node extendedSearchNode = null;
            Object controller = null;
            if (options[3] instanceof Class) {
                try {
                    extendedSearchNode = (Node) ((Class) options[3]).getDeclaredConstructor().newInstance();
                    controller = extendedSearchNode;
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
            else {
                try {
                    extendedSearchNode = ((FXMLLoader) options[3]).load();
                    controller = ((FXMLLoader) options[3]).getController();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (extendedSearchNode != null && controller != null) {
                secondValueSearchDialog.getSearchPane().getExtraPane().getChildren().add(extendedSearchNode);
            }
            Object finalController = controller;
            secondValueSearchDialog.getSearchPane().setSearchOptions(
                    ((Map<Function<Object, Control>, Function<V,?>>) options[4]).entrySet().stream()
                    .collect(Collectors.toMap(entry -> entry.getKey().apply(finalController), Map.Entry::getValue, (a, b) -> a, HashMap::new)));
        }
    }

    public V getSecondValue() {
        return secondValueComboBox.getValue();
    }

    public static <R, C> ExtendedOptionsBuilder<R, C> extendedOptionsBuilder() {
        return new ExtendedOptionsBuilder<>();
    }

    /**
     * Builder allow to create extended options step by step.
     */
    public static class ExtendedOptionsBuilder<T, C> {

        private ObservableList<T> collection;
        private Function<T, String> nameByFunction;
        private Function<T, String> searchByFunction;
        private FXMLLoader extendedSearchNodeLoader;
        private Class<? extends Node> extendedSearchNodeClass;
        private Map<Function<C, Control>, Function<T,?>> extendedSearchFunctions;

        public ExtendedOptionsBuilder() {
            extendedSearchFunctions = new HashMap<>();
        }

        public S2 setCollection(Collection<T> collection) {
            if (collection instanceof ObservableList) {
                this.collection = (ObservableList<T>) collection;
            }
            else {
                this.collection = FXCollections.observableList(new ArrayList<>(collection));
            }
            return new S2();
        }

        protected Object getLoaderOrClass() {
            if (extendedSearchNodeClass != null) {
                return extendedSearchNodeClass;
            }
            else return extendedSearchNodeLoader;
        }

        public class S2 {
            public S3 setNameByFunction(Function<T, String> nameByFunction) {
                ExtendedOptionsBuilder.this.nameByFunction = nameByFunction;
                return new S3();
            }

            public ExtendedOptionsBuilder<T, C> get() {
                return ExtendedOptionsBuilder.this;
            }
        }

        public class S3 {
            public S4 setSearchByFunction(Function<T, String> searchByFunction) {
                ExtendedOptionsBuilder.this.searchByFunction = searchByFunction;
                return new S4();
            }
        }

        public class S4 {
            public S5 setExtendedSearchNode(Class<? extends Node> extendedSearchNodeClass) {
                ExtendedOptionsBuilder.this.extendedSearchNodeClass = extendedSearchNodeClass;
                ExtendedOptionsBuilder.this.extendedSearchNodeLoader = null;
                return new S5();
            }

            public S5 setExtendedSearchNode(FXMLLoader extendedSearchNodeLoader) {
                ExtendedOptionsBuilder.this.extendedSearchNodeLoader = extendedSearchNodeLoader;
                ExtendedOptionsBuilder.this.extendedSearchNodeClass = null;
                return new S5();
            }

            public ExtendedOptionsBuilder<T, C> get() {
                return ExtendedOptionsBuilder.this;
            }
        }

        public class S5 {
            public ExtendedOptionsBuilder<T, C>  setExtendedSearchFunctions(Map<Function<C, Control>, Function<T, ?>> extendedSearchFunctions) {
                ExtendedOptionsBuilder.this.extendedSearchFunctions = extendedSearchFunctions;
                return ExtendedOptionsBuilder.this;
            }

            public S5 addExtendedSearchFunction(Function<C, Control> controlGetter, Function<T, ?> elementFieldGetter) {
                ExtendedOptionsBuilder.this.extendedSearchFunctions.put(controlGetter, elementFieldGetter);
                return this;
            }
        }
    }
}
