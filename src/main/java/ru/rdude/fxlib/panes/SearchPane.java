package ru.rdude.fxlib.panes;

import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.event.EventType;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.web.HTMLEditor;
import javafx.util.Callback;
import javafx.util.StringConverter;
import ru.rdude.fxlib.containers.ValueProvider;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

public class SearchPane<T> extends Pane {

    private HBox mainHBox;
    private ListView<T> listView;
    private TextField searchTextField;
    private AnchorPane extraPane;

    private FilteredList<T> filteredList;
    private Map<Object, Predicate<T>> predicates;
    private Set<Function<T, String>> searchTextFunctions;
    private Map<String, T> stringConverterMap;

    public SearchPane() {
        this(new ArrayList<>());
    }

    public SearchPane(Collection<T> collection) {
        super();
        filteredList = new FilteredList<>(FXCollections.observableList(new ArrayList<>(collection)));
        listView = new ListView<>(filteredList);
        predicates = new HashMap<>();
        searchTextField = new TextField();
        searchTextFunctions = new HashSet<>();
        extraPane = new AnchorPane();
        searchTextFunctions.add(Object::toString);
        VBox leftVBox = new VBox(searchTextField, listView);
        leftVBox.setSpacing(5d);
        mainHBox = new HBox(leftVBox, extraPane);
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
        stringConverterMap = new HashMap<>();
        for (T t : filteredList) {
            String name = function.apply(t);
            if (stringConverterMap.containsKey(name)) {
                String newName = name;
                int sameNames = 0;
                while (stringConverterMap.containsKey(newName)) {
                    sameNames++;
                    newName = name + " (" + sameNames + ")";
                }
                name = newName;
            }
            stringConverterMap.put(name, t);
        }
        listView.setCellFactory(lv -> {
            TextFieldListCell<T> cell = new TextFieldListCell<T>();
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
            return cell;
        });
    }

    /**
     * Link controls to T methods.
     * Controls can only be be instances of: TextInputControl, ComboBoxBase, ChoiceBox,
     * HTMLEditor, CheckBox, RadioButton, Spinner or ValueProvider.
     * @param functionMap this map link controls with T object functions.
     * @throws IllegalArgumentException if control is not instance of TextInputControl, ComboBoxBase, ChoiceBox,
     * HTMLEditor, CheckBox, RadioButton, Spinner or ValueProvider.
     */
    public void addSearchOptions(Map<Control, Function<T, ?>> functionMap) {
        functionMap.forEach(((control, tFunction) -> {
            predicates.put(control, (t) -> {
                Object value = tFunction.apply(t);
                boolean result = false;
                if (control instanceof TextInputControl) {
                    result = ((String) value).contains(((TextInputControl) control).getText());
                }
                else if (control instanceof ComboBoxBase) {
                    Object controlValue = ((ComboBoxBase<?>) control).getValue();
                    if (controlValue == null) {
                        result = true;
                    }
                    else {
                        result = controlValue.equals(value);
                    }
                }
                else if (control instanceof ChoiceBox) {
                    Object controlValue = ((ChoiceBox<?>) control).getValue();
                    if (controlValue == null) {
                        result = true;
                    }
                    else {
                        result = controlValue.equals(value);
                    }
                }
                else if (control instanceof HTMLEditor) {
                    result = ((String) value).contains(((HTMLEditor) control).getHtmlText());
                }
                else if (control instanceof Spinner) {
                    result = ((Spinner<?>) control).getValue().equals(value);
                }
                else if (control instanceof CheckBox) {
                    Boolean boolValue = null;
                    try {
                        boolValue = (boolean) value;
                    }
                    catch (Exception ignore) {}
                    if (boolValue != null) {
                        result = ((CheckBox) control).isSelected() == boolValue;
                    }
                    else if (((CheckBox) control).isSelected()) {
                        result = ((CheckBox) control).getText().equals(value.toString());
                    }
                    else {
                        result = true;
                    }
                }
                else if (control instanceof RadioButton) {
                    Boolean boolValue = null;
                    try {
                        boolValue = (boolean) value;
                    }
                    catch (Exception ignore) {}
                    if (boolValue != null) {
                        result = ((RadioButton) control).isSelected() == boolValue;
                    }
                    else if (((RadioButton) control).isSelected()) {
                        result = ((RadioButton) control).getText().equals(value.toString());
                    }
                    else {
                        result = true;
                    }
                }
                else if (control instanceof ValueProvider) {
                    result = ((ValueProvider<?>) control).getValue().equals(value);
                }
                else {
                    throw new IllegalArgumentException("Controls can only be be instances of: " +
                            "TextInputControl, ComboBoxBase, ChoiceBox, HTMLEditor, CheckBox, RadioButton, Spinner or ValueProvider. " +
                            "Control creating this exception is: " + control.getClass());
                }
                return result;
            });
            control.addEventHandler(EventType.ROOT, (event) -> updateSearch());
        }));
    }

    /**
     * Add custom predicate to search. List view will be updated every time action on controls set is handled.
     * @param customNodePredicate list view will be filtered based on this predicate.
     * @param controls list view will be filtered every time event on any of this controls is handled.
     */
    public void addSearchOptions(Set<Control> controls, Predicate<T> customNodePredicate) {
        predicates.put(new Object(), customNodePredicate);
        controls.forEach(control -> control.addEventHandler(EventType.ROOT, (event) -> updateSearch()));
    }

    /**
     * Add node to the left from the list view. Try to link child controls to T getter functions.
     * Getter method names of the objects in search pane must be same as controller field names ("get" and "is" are ignored).
     * Note: this method use reflection and force filtering use reflection as well. So if performance is an issue use
     * addSearchOptions method to link controls with methods manually.
     * @param node node to add.
     * @param controller node controller.
     * @param objectClass class of the collection object.
     */
    public <C> void addNodeAndAutoLinkControls(Node node, C controller, Class<T> objectClass) {
        mainHBox.getChildren().add(node);
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
                    }
                    catch (IllegalAccessException e) { }
                }
            }
        }
        addSearchOptions(functionMap);
    }

    public void setCollection(Collection<T> collection) {
        this.filteredList = new FilteredList<>(FXCollections.observableList(new ArrayList<>(collection)));
        listView.setItems(filteredList);
    }

    public AnchorPane getExtraPane() {
        return extraPane;
    }

    public ListView<T> getListView() {
        return listView;
    }

    private void initTextSearch() {
        searchTextField.textProperty().addListener((observableValue, oldV, newV) -> {
            predicates.put(searchTextField, t -> searchTextFunctions.stream()
                    .anyMatch(func -> func.apply(t).toLowerCase().contains(newV.toLowerCase())));
            updateSearch();
        });
    }

    private void updateSearch() {
        filteredList.setPredicate(e -> predicates.values().stream()
                .allMatch(tPredicate -> tPredicate.test(e)));
    }
}
