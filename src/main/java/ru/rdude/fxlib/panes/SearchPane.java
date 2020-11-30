package ru.rdude.fxlib.panes;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.EventType;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.web.HTMLEditor;
import javafx.util.StringConverter;
import ru.rdude.fxlib.containers.ValueProvider;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * List View that can be filtered with complex filtering and complex tooltips for each element of the List View can be set.
 * Naming of elements represented in list view can be set by passing a function to setNameBy method. Default is Object::toString.
 * By default there is a TextField on the top of the list. Input text of this text field is used to filter
 * list, applying one or more search functions on elements of the list.
 * Use setTextFieldSearchBy method to set text field search functions. Default is Object::toString.
 * <p>
 * To use complex filtering link Controls to Functions that called on every element of the list view by passing
 * Control and Function or Map with Control keys and Function values to addSearchOptions or setSearchOptions methods.
 * Search will work based on provided Controls:
 * <p>
 * TextInputControl, HTMLEditor:
 * If linked Function returns a Collection, checks if collection values after applying toString to them
 * contains control text .
 * Else checks if function return value after applying toString equals to control text.
 * <p>
 * ComboBox, ChoiceBox, Spinner, ValueProvider:
 * If linked Function returns a Collection, checks if collection contains selected value.
 * Else checks if function return value equals to selected value.
 * <p>
 * CheckBox:
 * If linked function returns a Collection, checks if collection values after applying toString to them
 * contains control text.
 * Else if function returns a boolean, checks if this boolean equals to isSelected of the check box.
 * Else checks if function return value after applying toString equals control text.
 * <p>
 * RadioButton:
 * Same as CheckBox but filters only when RadioButton is selected.
 * <p>
 * Also filtering can be set by passing custom Predicate to addSearchOptions method.
 * <p>
 * Also filtering can be set automatically with addNodeAndAutoLinkControls method by simply passing
 * a Node that contain controls, Controller for this node (may be the Node itself) and Class of the elements in ListView.
 * Controller fields names and object class getters must have the same name (ignoring "get" and "is").
 * However this method uses reflection and generates filter options based on reflection. So if performance
 * is an issue use manual linking with addSearchOptions or setSearchOptions methods.
 * <p>
 * Elements of the List View can have custom tooltips.
 * those tooltips can be set with setPopupFunction method or by using a popup builder.
 * Customizing tooltips with a builder is preferred way due to popups generated with setPopupFunction will
 * create new Node provided by function for tooltip every time tooltip is shown.
 *
 * @param <T> type of elements in ListView of this SearchPane.
 */

public class SearchPane<T> extends Pane {

    private HBox mainHBox;
    private VBox listVBox;
    private ListView<T> listView;
    private TextField searchTextField;
    private AnchorPane extraPane;

    private FilteredList<T> filteredList;
    private Map<Object, Predicate<T>> predicates;
    private Set<Function<T, String>> searchTextFunctions;
    private Function<T, String> nameByFunction;
    private Function<T, Node> popupFunction;
    private Tooltip popup; // One popup for all list view cells for better performance
    private PopupBuilder popupBuilder;
    private PopupNodeHolder popupNodeHolder;
    private Map<String, T> stringConverterMap;
    // creating this context menu to all elements instead of creating one for every element
    private ContextMenu elementsContextMenu;
    private T contextMenuRequester;

    public SearchPane() {
        this(new ArrayList<>());
    }

    public SearchPane(Collection<T> collection) {
        super();
        if (collection instanceof ObservableList) {
            filteredList = new FilteredList<>((ObservableList<T>) collection);
        } else {
            filteredList = new FilteredList<>(FXCollections.observableList(new ArrayList<>(collection)));
        }
        listView = new ListView<>(filteredList);
        predicates = new HashMap<>();
        searchTextField = new TextField();
        searchTextFunctions = new HashSet<>();
        extraPane = new AnchorPane();
        searchTextFunctions.add(Object::toString);
        listVBox = new VBox(searchTextField, listView);
        listVBox.setSpacing(5d);
        mainHBox = new HBox(listVBox, extraPane);
        getChildren().add(mainHBox);
        initTextSearch();
    }

    @SafeVarargs
    public final void setTextFieldSearchBy(Function<T, String>... functions) {
        setTextFieldSearchBy(List.of(functions));
    }

    public void setTextFieldSearchBy(Collection<Function<T, String>> functions) {
        searchTextFunctions = new HashSet<>(functions);
    }

    public void setNameBy(Function<T, String> function) {
        nameByFunction = function;
        stringConverterMap = new HashMap<>();
        // listener for filtered list changes
        filteredList.getSource().addListener((ListChangeListener<T>) change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    change.getAddedSubList().forEach(t -> stringConverterMap.putIfAbsent(generateNameToByNameFunction(t), t));
                } else if (change.wasRemoved()) {
                    AtomicReference<String> lookingForName = new AtomicReference<>();
                    change.getRemoved().forEach(t -> stringConverterMap.entrySet().stream()
                            .filter(entry -> {
                                if (entry.getValue() == t) {
                                    lookingForName.set(entry.getKey());
                                    return true;
                                }
                                return false;
                            }).findFirst()
                            .ifPresent(entry -> stringConverterMap.remove(lookingForName.get())));
                }
            }
        });
        // update stringConverterMap with presented elements
        for (T t : filteredList) {
            String name = generateNameToByNameFunction(t);
            stringConverterMap.put(name, t);
        }
        updateCellFactory();
    }

    private String generateNameToByNameFunction(T t) {
        String name = nameByFunction.apply(t);
        if (stringConverterMap.containsKey(name)) {
            String newName = name;
            int sameNames = 0;
            while (stringConverterMap.containsKey(newName)) {
                sameNames++;
                newName = name + " (" + sameNames + ")";
            }
            name = newName;
        }
        return name;
    }

    public void setPopupFunction(Function<T, Node> function) {
        if (popup == null) {
            popup = new Tooltip();
        }
        popupFunction = function;
        popupNodeHolder = null;
        updateCellFactory();
    }


    public void addSearchOption(Control control, Function<T, ?> getter) {
        predicates.put(control, (t) -> {
            Object value = getter.apply(t);
            boolean result = false;
            if (control instanceof TextInputControl) {
                result = ((String) value).contains(((TextInputControl) control).getText());
            } else if (control instanceof ComboBoxBase) {
                Object controlValue = ((ComboBoxBase<?>) control).getValue();
                if (controlValue == null) {
                    result = true;
                } else {
                    result = controlValue.equals(value);
                }
            } else if (control instanceof ChoiceBox) {
                Object controlValue = ((ChoiceBox<?>) control).getValue();
                if (controlValue == null) {
                    result = true;
                } else if (value instanceof Collection) {
                    result = ((Collection<?>) value).contains(((ChoiceBox<?>) control).getValue());
                } else {
                    result = controlValue.equals(value);
                }
            } else if (control instanceof HTMLEditor) {
                result = ((String) value).contains(((HTMLEditor) control).getHtmlText());
            } else if (control instanceof Spinner) {
                result = ((Spinner<?>) control).getValue().equals(value);
            } else if (control instanceof CheckBox) {
                Boolean boolValue = null;
                try {
                    boolValue = (boolean) value;
                } catch (Exception ignore) {
                }
                if (boolValue != null) {
                    result = ((CheckBox) control).isSelected() == boolValue;
                } else if (value instanceof Collection) {
                    String checkBoxText = ((CheckBox) control).getText();
                    result = ((Collection<?>) value).stream().map(Object::toString).anyMatch(s -> s.equals(checkBoxText));
                } else {
                    result = ((CheckBox) control).getText().equals(value.toString());
                }
            } else if (control instanceof RadioButton) {
                Boolean boolValue = null;
                try {
                    boolValue = (boolean) value;
                } catch (Exception ignore) {
                }
                if (boolValue != null) {
                    if (((RadioButton) control).isSelected()) {
                        result = ((RadioButton) control).isSelected() == boolValue;
                    } else {
                        result = true;
                    }
                } else if (((RadioButton) control).isSelected()) {
                    if (value instanceof Collection) {
                        String radioButtonText = ((RadioButton) control).getText();
                        result = ((Collection<?>) value).stream().map(Object::toString).anyMatch(s -> s.equals(radioButtonText));
                    } else {
                        result = ((RadioButton) control).getText().equals(value.toString());
                    }
                } else {
                    result = true;
                }
            } else if (control instanceof ValueProvider) {
                if (((ValueProvider) control).getValue() instanceof Collection) {
                    result = ((Collection<?>) value).containsAll((Collection<?>) ((ValueProvider<?>) control).getValue());
                } else {
                    result = ((ValueProvider<?>) control).getValue().equals(value);
                }
            } else {
                throw new IllegalArgumentException("Controls can only be be instances of: " +
                        "TextInputControl, ComboBoxBase, ChoiceBox, HTMLEditor, CheckBox, RadioButton, Spinner or ValueProvider. " +
                        "Control creating this exception is: " + control.getClass());
            }
            return result;
        });
        control.addEventHandler(EventType.ROOT, (event) -> updateSearch());
    }

    /**
     * Link controls to T methods.
     * Controls can only be be instances of: TextInputControl, ComboBoxBase, ChoiceBox,
     * HTMLEditor, CheckBox, RadioButton, Spinner or ValueProvider.
     *
     * @param functionMap this map link controls with T object functions.
     * @throws IllegalArgumentException if control is not instance of TextInputControl, ComboBoxBase, ChoiceBox,
     *                                  HTMLEditor, CheckBox, RadioButton, Spinner or ValueProvider.
     */
    public void addSearchOptions(Map<Control, Function<T, ?>> functionMap) {
        functionMap.forEach((this::addSearchOption));
    }

    public void setSearchOptions(Map<Control, Function<T, ?>> functionMap) {
        this.predicates = new HashMap<>();
        addSearchOptions(functionMap);
    }

    /**
     * Add custom predicate to search. List view will be updated every time action on controls set is handled.
     *
     * @param customNodePredicate list view will be filtered based on this predicate.
     * @param controls            list view will be filtered every time event on any of this controls is handled.
     */
    public void addSearchOptions(Set<Control> controls, Predicate<T> customNodePredicate) {
        predicates.put(new Object(), customNodePredicate);
        controls.forEach(control -> control.addEventHandler(EventType.ROOT, (event) -> updateSearch()));
    }

    /**
     * Add node close to the list view. Try to link child controls to T getter functions.
     * Getter method names of the objects in search pane must be same as controller field names ("get" and "is" are ignored).
     * Note: this method use reflection and force filtering use reflection as well. So if performance is an issue use
     * addSearchOptions method to link controls with methods manually.
     *
     * @param node        node to add.
     * @param controller  node controller.
     * @param objectClass class of the collection object.
     */
    public <C> void addNodeAndAutoLinkControls(Node node, C controller, Class<T> objectClass) {
        extraPane.getChildren().add(node);
        Map<Control, Function<T, ?>> functionMap = new HashMap<>();
        Set<Field> controllerFields = new HashSet<>();
        for (Field declaredField : controller.getClass().getDeclaredFields()) {
            if (Control.class.isAssignableFrom(declaredField.getDeclaringClass())) {
                controllerFields.add(declaredField);
            }
        }
        for (Method declaredMethod : objectClass.getDeclaredMethods()) {
            String methodName = declaredMethod.getName().toLowerCase().replaceFirst("^(get|is)", "");
            for (Field controllerField : controllerFields) {
                if (controllerField.getName().toLowerCase().equals(methodName)) {
                    try {
                        functionMap.put((Control) controllerField.get(controller), t -> {
                            try {
                                return declaredMethod.invoke(t);
                            } catch (IllegalAccessException | InvocationTargetException e) {
                                e.printStackTrace();
                            }
                            throw new IllegalArgumentException("Can not invoke get method");
                        });
                    } catch (IllegalAccessException ignored) {
                    }
                }
            }
        }
        functionMap.forEach((key, value) -> {
            try {
                addSearchOption(key, value);
            } catch (IllegalArgumentException ignore) {
            }
        });
    }

    public void setCollection(Collection<T> collection) {
        if (collection instanceof ObservableList) {
            filteredList = new FilteredList<>((ObservableList<T>) collection);
        } else {
            filteredList = new FilteredList<>(FXCollections.observableList(new ArrayList<>(collection)));
        }
        listView.setItems(filteredList);
    }

    public AnchorPane getExtraPane() {
        return extraPane;
    }

    public void addExtraSearchNode(Node node) {
        extraPane.getChildren().add(node);
    }

    public ListView<T> getListView() {
        return listView;
    }

    public TextField getSearchTextField() {
        return searchTextField;
    }

    public void setHasSearchTextField(boolean value) {
        if (!value && listVBox.getChildren().contains(searchTextField)) {
            listVBox.getChildren().remove(searchTextField);
        } else if (value && !listVBox.getChildren().contains(searchTextField)) {
            listVBox.getChildren().add(listVBox.getChildren().size() > 1 ? 1 : 0, searchTextField);
        }
    }

    public void setExtraPanePosition(Pos position) {
        mainHBox.getChildren().remove(extraPane);
        listVBox.getChildren().remove(extraPane);
        switch (position) {
            case TOP_LEFT:
            case BOTTOM_LEFT:
            case CENTER_LEFT:
            case BASELINE_LEFT:
                mainHBox.getChildren().add(0, extraPane);
                break;
            case TOP_RIGHT:
            case BOTTOM_RIGHT:
            case CENTER_RIGHT:
            case BASELINE_RIGHT:
                mainHBox.getChildren().add(mainHBox.getChildren().size() - 1, extraPane);
                break;
            case TOP_CENTER:
                listVBox.getChildren().add(0, extraPane);
                break;
            case BOTTOM_CENTER:
            case BASELINE_CENTER:
                listVBox.getChildren().add(listVBox.getChildren().size() - 1, extraPane);
                break;
            case CENTER:
                listVBox.getChildren().add(1, extraPane);
        }
    }

    public void addContextMenuItem(String name, Consumer<T> consumer) {
        if (elementsContextMenu == null) {
            elementsContextMenu = new ContextMenu();
        }
        MenuItem menuItem = new MenuItem(name);
        menuItem.setOnAction(event -> {
            if (contextMenuRequester != null) {
                consumer.accept(contextMenuRequester);
                contextMenuRequester = null;
            }
        });
        elementsContextMenu.getItems().add(menuItem);
    }

    public PopupBuilder popupBuilder() {
        if (popupBuilder == null) {
            popupBuilder = new PopupBuilder();
        }
        return popupBuilder;
    }

    private void initTextSearch() {
        searchTextField.textProperty().addListener((observableValue, oldV, newV) -> {
            predicates.put(searchTextField, t -> searchTextFunctions.stream()
                    .anyMatch(func -> func.apply(t).toLowerCase().contains(newV.toLowerCase())));
            updateSearch();
        });
    }


    private void updateSearch() {
        T selectedItem = listView.getSelectionModel().getSelectedItem();
        filteredList.setPredicate(e -> predicates.values().stream()
                .allMatch(tPredicate -> tPredicate.test(e)));
        // need to manually reselect item because selection disappears after filtered list update.
        if (filteredList.contains(selectedItem)) {
            listView.getSelectionModel().select(selectedItem);
        }
    }

    void updateCellFactory() {
        listView.setCellFactory(lv -> {
            TextFieldListCell<T> cell = new TextFieldListCell<T>();
            // set name by:
            if (nameByFunction != null) {
                cell.setConverter(new StringConverter<T>() {
                    @Override
                    public String toString(T t) {
                        if (t == null) {
                            return "";
                        }
                        return stringConverterMap.entrySet().stream()
                                .filter(entry -> entry.getValue() == t)
                                .map(Map.Entry::getKey)
                                .findFirst()
                                .orElse("");
                    }

                    @Override
                    public T fromString(String s) {
                        return stringConverterMap.get(s);
                    }
                });
            }
            // set popup:
            if ((popupFunction != null || popupNodeHolder != null) && popup != null) {
                cell.hoverProperty().addListener((observableValue, oldV, newV) -> {
                    if (newV && !cell.isEmpty()) {
                        showPopup(cell);
                    } else {
                        hidePopup();
                    }
                });
            }
            // set context menu
            if (elementsContextMenu != null) {
                cell.setContextMenu(elementsContextMenu);
                cell.setOnContextMenuRequested(contextMenuEvent -> {
                    contextMenuRequester = cell.getItem();
                });
            }
            return cell;
        });
    }

    private void showPopup(TextFieldListCell<T> cell) {
        if (popupFunction != null) {
            Node popupNode = popupFunction.apply(cell.getItem());
            popup.setGraphic(popupNode);
        } else if (popupNodeHolder != null) {
            popupNodeHolder.applyToCell(cell);
        }
        Bounds bounds = cell.localToScreen(cell.getBoundsInLocal());
        popup.show(cell, bounds.getMaxX(), bounds.getMinY());
    }

    private void hidePopup() {
        popup.hide();
    }


    private class PopupNodeHolder extends AnchorPane {

        private Map<Label, Function<T, String>> textFunctions;
        private Map<Node, Function<T, Node>> nodeFunctions;
        private VBox vBox;

        PopupNodeHolder(VBox builderBox, Map<Label, Function<T, String>> textFunctions, Map<Node, Function<T, Node>> nodeFunctions) {
            getChildren().add(builderBox);
            vBox = builderBox;
            this.textFunctions = textFunctions;
            this.nodeFunctions = new ConcurrentHashMap<>(nodeFunctions);
            popup = new Tooltip();
            popup.setGraphic(this);
            popupFunction = null;
        }

        void applyToCell(TextFieldListCell<T> cell) {
            textFunctions.forEach((label, function) -> label.setText(function.apply(cell.getItem())));
            nodeFunctions.forEach((node, function) -> {
                Node newNode = function.apply(cell.getItem());
                int position = vBox.getChildren().indexOf(node);
                vBox.getChildren().remove(node);
                vBox.getChildren().add(position, newNode);
                nodeFunctions.remove(node);
                nodeFunctions.put(newNode, function);
            });
        }

    }

    public class PopupBuilder {

        private VBox mainVBox;
        private Map<Label, Function<T, String>> textFunctions;
        private Map<Node, Function<T, Node>> nodeFunctions;
        private String textStyle;


        PopupBuilder() {
            this.mainVBox = new VBox();
            this.mainVBox.setAlignment(Pos.CENTER);
            textFunctions = new HashMap<>();
            nodeFunctions = new HashMap<>();
        }

        public PopupBuilder addText(String text) {
            Label label = new Label(text);
            if (textStyle != null && !textStyle.isEmpty()) {
                label.setStyle(textStyle);
            }
            mainVBox.getChildren().add(label);
            return this;
        }

        public PopupBuilder addText(Label label) {
            mainVBox.getChildren().add(label);
            return this;
        }

        public PopupBuilder addText(Function<T, String> function) {
            Label label = new Label();
            if (textStyle != null && !textStyle.isEmpty()) {
                label.setStyle(textStyle);
            }
            textFunctions.put(label, function);
            mainVBox.getChildren().add(label);
            return this;
        }

        public PopupBuilder addNode(Node node) {
            mainVBox.getChildren().add(node);
            return this;
        }

        public PopupBuilder addNode(Function<T, Node> function) {
            Node placeHolder = new AnchorPane();
            nodeFunctions.put(placeHolder, function);
            mainVBox.getChildren().add(placeHolder);
            return this;
        }

        public void apply() {
            popupNodeHolder = new PopupNodeHolder(this.mainVBox, this.textFunctions, this.nodeFunctions);
            updateCellFactory();
        }

        public PopupBuilder clear() {
            SearchPane.this.popupBuilder = new PopupBuilder();
            return this;
        }

        public void setStyle(String value) {
            mainVBox.setStyle(value);
        }

        public void setTextStyle(String textStyle) {
            this.textStyle = textStyle;
        }
    }
}
