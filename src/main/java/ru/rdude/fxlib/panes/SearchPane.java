package ru.rdude.fxlib.panes;

import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.EventType;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import ru.rdude.fxlib.containers.selector.SelectorContainer;
import utils.FunctionRawOrProperty;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.*;
import java.util.stream.Collectors;

/**
 * List View that can be filtered with complex filtering. Also complex tooltips for each element of the List View can be set.
 * Naming of elements represented in list view can be set by passing a function to setNameBy method. Default is Object::toString.
 * By default there is a TextField on the top of the list. Input text of this text field is used to filter
 * list, applying one or more search functions on elements of the list.
 * Use setTextFieldSearchBy method to set text field search functions. Default is Object::toString.
 * <p>
 * To use complex filtering link Controls to Functions that called on every element of the list view by passing
 * Control and Function or Map with Control keys and Function values to addSearchOptions or setSearchOptions methods.
 * Search will work based on provided Controls:
 * </p>
 * <p>
 * TextInputControl:
 * If linked Function returns a Collection, checks if collection values after applying toString to them
 * contains control text .
 * Else if function returns number checks if text field can be parsed and be equal to the number value.
 * Else checks if function return value after applying toString equals to control text.
 * </p>
 * <p>
 * ComboBox, ChoiceBox, Spinner:
 * If linked Function returns a Collection, checks if collection contains selected value.
 * Else checks if function return value equals to selected value.
 * </p>
 * <p>
 * CheckBox:
 * If function returns a boolean, checks if this boolean equals to isSelected of the check box.
 * If boolean returns null, result will be true
 * Else checks if function return value after applying toString equals control text.
 * </p>
 * <p>
 * RadioButton:
 * Same as CheckBox but filters only when RadioButton is selected.
 * <p>
 * SelectorContainer:
 * If linked function returns a Collection, checks if this collection contains all selected elements from SelectorContainer
 * Else checks if return of the functions contains in selected elements of SelectorContainer
 * </p>
 * </p>
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
    private FunctionRawOrProperty<T, String> nameFunction;
    private Set<FunctionRawOrProperty<T, String>> searchTextFunctions;
    private CustomCellGraphic<? extends Node> customCellGraphic;
    private Function<T, Node> popupFunction;
    private Tooltip popup; // One popup for all list view cells for better performance
    private PopupBuilder popupBuilder;
    private PopupNodeHolder popupNodeHolder;
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
        searchTextFunctions.add(FunctionRawOrProperty.raw(Object::toString));
        listVBox = new VBox(searchTextField, listView);
        VBox.setVgrow(listView, Priority.ALWAYS);
        listVBox.setSpacing(5d);
        mainHBox = new HBox(listVBox, extraPane);
        getChildren().add(mainHBox);
        initTextSearch();
    }

    @SafeVarargs
    public final void setTextFieldSearchByProperty(Function<T, ObservableValue<String>> function, Function<T, ObservableValue<String>>... functions) {
        if (function == null) {
            throw new NullPointerException();
        }
        Set<Function<T, ObservableValue<String>>> set = new HashSet<>();
        set.add(function);
        if (functions != null) {
            set.addAll(Arrays.asList(functions));
        }
        setTextFieldSearchByProperty(set);
    }

    public void setTextFieldSearchByProperty(Collection<Function<T, ObservableValue<String>>> functions) {
        searchTextFunctions = functions.stream()
                .map(FunctionRawOrProperty::property)
                .collect(Collectors.toSet());
    }

    @SafeVarargs
    public final void setTextFieldSearchBy(Function<T, String> function, Function<T, String>... functions) {
        if (function == null) {
            throw new NullPointerException();
        }
        Set<Function<T, String>> set = new HashSet<>();
        set.add(function);
        if (functions != null) {
            set.addAll(Arrays.asList(functions));
        }
        setTextFieldSearchBy(set);
    }

    public void setTextFieldSearchBy(Collection<Function<T, String>> functions) {
        searchTextFunctions = functions.stream()
                .map(FunctionRawOrProperty::raw)
                .collect(Collectors.toSet());
    }

    public void setNameBy(Function<T, String> function) {
        if (function == null) {
            throw new NullPointerException();
        }
        nameFunction = FunctionRawOrProperty.raw(function);
        updateCellFactory();
    }

    public void setNameByProperty(Function<T, ObservableValue<String>> function) {
        if (function == null) {
            throw new NullPointerException();
        }
        nameFunction = FunctionRawOrProperty.property(function);
        updateCellFactory();
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
        if (control == null) {
            throw new NullPointerException("Control must not be null");
        }
        Predicate<T> predicate = null;

        // text fields
        if (control instanceof TextInputControl) {
            ((TextInputControl) control).textProperty().addListener((obs, o, n) -> updateSearch());
            predicate = t -> {
                Object value = getter.apply(t);

                if (value == null) {
                    return true;
                }
                else if (value instanceof ObservableValue) {
                    value = ((ObservableValue<?>) value).getValue();
                    if (value == null) {
                        return true;
                    }
                }


                if (value instanceof String) {
                    return ((String) value).contains(((TextInputControl) control).getText());
                }

                else if (value instanceof Number) {
                    final String text = ((TextInputControl) control).getText();
                    if (value instanceof Long) {
                        if (text.matches("\\d+")) {
                            return value.equals(Long.parseLong(text));
                        } else {
                            return false;
                        }
                    }
                    else if (value instanceof Integer) {
                        if (text.matches("\\d+")) {
                            return value.equals(Integer.parseInt(text));
                        } else {
                            return false;
                        }
                    }
                    else if (value instanceof Short) {
                        if (text.matches("\\d+")) {
                            return value.equals(Short.parseShort(text));
                        }
                        else {
                            return false;
                        }
                    }
                    else if (value instanceof Byte) {
                        if (text.matches("\\d+")) {
                            return value.equals(Byte.parseByte(text));
                        }
                        else {
                            return false;
                        }
                    }
                    else if (value instanceof Double) {
                        if (text.matches("\\d+(\\.\\d+)*")) {
                            return value.equals(Double.parseDouble(text));
                        }
                        else {
                            return false;
                        }
                    }
                    else if (value instanceof Float) {
                        if (text.matches("\\d+(\\.\\d+)*")) {
                            return value.equals(Float.parseFloat(text));
                        }
                        else {
                            return false;
                        }
                    }
                    else {
                        return false;
                    }
                }

                else if (value instanceof Boolean) {
                    return (boolean) value;
                }

                else {
                    return value.toString().contains(((TextInputControl) control).getText());
                }
            };
        }

        // combo boxes
        else if (control instanceof ComboBoxBase) {
            ((ComboBoxBase<?>) control).valueProperty().addListener((v, o, n) -> updateSearch());
            predicate = t -> {
                Object value = getter.apply(t);
                if (value instanceof ObservableValue) {
                    value = ((ObservableValue<?>) value).getValue();
                }
                if (((ComboBoxBase<?>) control).getValue() == null) {
                    return true;
                }
                else if (value instanceof Collection) {
                    return ((Collection<?>) value).contains(((ComboBoxBase<?>) control).getValue());
                }
                else {
                    return ((ComboBoxBase<?>) control).getValue().equals(value);
                }
            };
        }

        // choice box
        else if (control instanceof ChoiceBox) {
            ((ChoiceBox<?>) control).valueProperty().addListener((v, o, n) -> updateSearch());
            predicate = t -> {
                Object value = getter.apply(t);
                if (value instanceof ObservableValue) {
                    value = ((ObservableValue<?>) value).getValue();
                }
                if (((ChoiceBox<?>) control).getValue() == null) {
                    return true;
                }
                else if (value instanceof Collection) {
                    return ((Collection<?>) value).contains(((ChoiceBox<?>) control).getValue());
                }
                else {
                    return ((ChoiceBox<?>) control).getValue().equals(value);
                }
            };
        }

        // spinner
        else if (control instanceof Spinner) {
            ((Spinner<?>) control).valueProperty().addListener((v, o, n) -> updateSearch());
            predicate = t -> {
                Object value = getter.apply(t);
                if (value instanceof ObservableValue) {
                    value = ((ObservableValue<?>) value).getValue();
                }
                if (((Spinner<?>) control).getValue() == null) {
                    return true;
                }
                else if (value instanceof Collection) {
                    return ((Collection<?>) value).contains(((Spinner<?>) control).getValue());
                }
                else {
                    return ((Spinner<?>) control).getValue().equals(value);
                }
            };
        }

        // checkbox
        else if (control instanceof CheckBox) {
            ((CheckBox) control).selectedProperty().addListener((v, o, n) -> updateSearch());
            predicate = t -> {
                Object value = getter.apply(t);
                if (value == null) {
                    return true;
                }
                else if (value instanceof ObservableValue) {
                    value = ((ObservableValue<?>) value).getValue();
                    if (value == null) {
                        return true;
                    }
                }
                if (value instanceof Boolean) {
                    return (Boolean) value == ((CheckBox) control).isSelected();
                }
                else {
                    return value.toString().equals(((CheckBox) control).getText());
                }
            };
        }

        // radio button
        else if (control instanceof RadioButton) {
            ((RadioButton) control).selectedProperty().addListener((v, o, n) -> updateSearch());
            predicate = t -> {
                if (!((RadioButton) control).isSelected()) {
                    return true;
                }
                else {
                    Object value = getter.apply(t);
                    if (value == null) {
                        return true;
                    }
                    else if (value instanceof ObservableValue) {
                        value = ((ObservableValue<?>) value).getValue();
                        if (value == null) {
                            return true;
                        }
                    }
                    if (value instanceof Boolean) {
                        return ((Boolean) value) == ((RadioButton) control).isSelected();
                    }
                    else {
                        return value.toString().equals(((RadioButton) control).getText());
                    }
                }
            };
        }

        // selector container
        else if (control instanceof SelectorContainer) {
            ((SelectorContainer<?, ?>) control).getItems().addListener((ListChangeListener<Object>) change -> {
                if (change.next()) {
                    updateSearch();
                }
            });
            predicate = t -> {
                Object value = getter.apply(t);
                if (value instanceof ObservableValue) {
                    value = ((ObservableValue<?>) value).getValue();
                }
                if (value instanceof Collection) {
                    if (((SelectorContainer<?, ?>) control).getItems().isEmpty()) {
                        return true;
                    }
                    else {
                        try {
                            return ((Collection<?>) value).containsAll(((SelectorContainer<?, ?>) control).getItems());
                        }
                        catch (ClassCastException | NullPointerException exception) {
                            return true;
                        }
                    }
                }
                else {
                    return ((SelectorContainer<?, ?>) control).getItems().contains(value);
                }
            };
        }

        if (predicate != null) {
            predicates.put(control, predicate);
        }
    }

    /**
     * Link controls to T methods.
     * Controls can only be be instances of: TextInputControl, ComboBoxBase, ChoiceBox,
     * CheckBox, RadioButton, Spinner.
     *
     * @param functionMap this map link controls with T object functions.
     * @throws IllegalArgumentException if control is not instance of TextInputControl, ComboBoxBase, ChoiceBox,
     *                                  HTMLEditor, CheckBox, RadioButton, Spinner.
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

    public void addContextMenu(String name, Consumer<SearchPaneMenuItem> children) {
        Menu menu = new Menu(name);
        SearchPaneMenuItem searchPaneMenuItem = new SearchPaneMenuItem(new ArrayList<>());
        children.accept(searchPaneMenuItem);
        menu.getItems().addAll(searchPaneMenuItem.list);
        elementsContextMenu.getItems().add(menu);
    }

    public PopupBuilder popupBuilder() {
        if (popupBuilder == null) {
            popupBuilder = new PopupBuilder();
        }
        return popupBuilder;
    }

    public void setIcon(Function<T, Image> iconFunction) {
        setIcon(iconFunction, 25d, 25d);
    }

    public void setIcon(Function<T, Image> iconFunction, double width, double height) {
        customCellGraphic = new CustomCellGraphic<>(
                () -> {
                    ImageView imageView = new ImageView();
                    imageView.setFitWidth(width);
                    imageView.setFitHeight(height);
                    return imageView;
                },
                (t, view) -> view.setImage(iconFunction.apply(t)));
        updateCellFactory();
    }

    public <N extends Node> void setCellGraphic(Supplier<N> creator, BiConsumer<T, N> updater) {
        customCellGraphic = new CustomCellGraphic<>(creator, updater);
        updateCellFactory();
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
            ListCell<T> cell = new ListCell<>() {
                Node customGraphic = customGraphicOrNull();

                @Override
                protected void updateItem(T t, boolean empty) {
                    super.updateItem(t, empty);
                    if (empty) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        // set graphic
                        if (customCellGraphic != null) {
                            customCellGraphic.update(t, customGraphic);
                            setGraphic(customGraphic);
                        }
                        // set name by:
                        setText(nameFunction.apply(t));
                    }
                }

                private Node customGraphicOrNull() {
                    Node n = null;
                    if (customCellGraphic != null && customCellGraphic.creator != null) {
                        n = customCellGraphic.creator.get();
                    }
                    return n;
                }
            };
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

    private void showPopup(ListCell<T> cell) {
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

        void applyToCell(ListCell<T> cell) {
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


    public class SearchPaneMenuItem {

        private final List<MenuItem> list;

        private SearchPaneMenuItem(List<MenuItem> list) {
            this.list = list;
        }

        public void menuItem(String name, Consumer<T> consumer) {
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
            list.add(menuItem);
        }

        public void parentMenu(String name, Consumer<SearchPaneMenuItem> children) {
            if (elementsContextMenu == null) {
                elementsContextMenu = new ContextMenu();
            }
            Menu menu = new Menu(name);
            SearchPaneMenuItem searchPaneMenuItem = new SearchPaneMenuItem(new ArrayList<>());
            children.accept(searchPaneMenuItem);
            menu.getItems().addAll(searchPaneMenuItem.list);
            list.add(menu);
        }
    }

    public class CustomCellGraphic<N extends Node> {

        Supplier<N> creator;
        BiConsumer<T, N> updater;

        private CustomCellGraphic(Supplier<N> creator, BiConsumer<T, N> updater) {
            this.creator = creator;
            this.updater = updater;
        }

        void update(T t, Node n) {
            updater.accept(t, (N) n);
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
