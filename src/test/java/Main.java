import javafx.application.Application;
import javafx.beans.Observable;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import ru.rdude.fxlib.boxes.SearchComboBox;
import ru.rdude.fxlib.containers.selector.SelectorContainer;
import ru.rdude.fxlib.panes.SearchPane;
import ru.rdude.fxlib.textfields.AutocompletionTextField;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class Main extends Application {

    private Stage primaryStage;
    private AnchorPane mainPane;



    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        this.primaryStage = primaryStage;
        showMainView();
        realTest();
    }


    private void showMainView() throws IOException {
        mainPane = new AnchorPane();
        Scene scene = new Scene(mainPane);
        primaryStage.setScene(scene);
        primaryStage.show();
    }


    private void realTest() {

        TestClass te1 = new TestClass("first", 1, true);
        TestClass te2 = new TestClass("second", 2, false);
        TestClass te3 = new TestClass("third", 3, true);
        TestClass te4 = new TestClass("fourth", 4, false);
        TestClass te5 = new TestClass("second", 5, true);
        TestClass te6 = new TestClass("fifth", 6, false);
        te5.setCollection(new ArrayList<>());
        te1.setCollection(new ArrayList<>());
        List<TestClass> testClassList = List.of(te1, te2, te3, te4, te5, te6);
        ObservableList<TestClass> testClassObservableList = FXCollections.observableArrayList(
                testClass -> new Observable[] { testClass.stringProperty });
        testClassObservableList.addAll(testClassList);
        SearchComboBox<TestClass> searchComboBox = new SearchComboBox<>();
        searchComboBox.setCollection(testClassObservableList);
        //searchComboBox.setNameAndSearchBy(TestClass::getName);
        //searchComboBox.setSearchBy(TestClass::getName, testClass -> String.valueOf(testClass.getValue()));
        searchComboBox.setNameByProperty(TestClass::stringPropertyProperty);

        mainPane.setMinWidth(300);

        TextField textField = new TextField();
        RadioButton radioButton = new RadioButton("fourth");

        Map<Control, Function<TestClass, ?>> map = new HashMap<>();
        map.put(textField, TestClass::getName);
        map.put(radioButton, TestClass::getCollection);

        SearchPane<TestClass> searchPane = new SearchPane<>();
        searchPane.setCollection(testClassList);
        searchPane.setTextFieldSearchBy(TestClass::getName, (t) -> String.valueOf(t.getValue()));
        searchPane.setNameBy(TestClass::getName);
        searchPane.addSearchOptions(map);
        searchPane.popupBuilder()
                .addText("Test string")
                .addText(new Label("Test label"))
                .addText(TestClass::getName)
                .addNode(new Label("test static node"))
                .addNode(TestClass::getLabel)
                .apply();


        SearchPane<String> searchPane2 = new SearchPane<>(List.of("1", "2", "3"));

        //HBox hBox = new HBox(container, vBox, searchComboBox, searchPane, searchPane2, textField, radioButton);
        HBox hBox = new HBox();

        mainPane.getChildren().add(hBox);

        searchPane.addContextMenuItem("get name", testClass -> System.out.println(testClass.name));
        searchPane.addContextMenuItem("get value", testClass -> System.out.println(testClass.value));


/*        var selectorContainer = new SelectorContainerDepr<>(testClassList, SelectorElementPercent::new);
        selectorContainer.setSearchDialogNameBy(TestClass::getName);
        selectorContainer.setSearchDialogSearchBy(TestClass::getName);
        selectorContainer.addOption(n -> n.setPercents(15));
        */

/*        var selectorContainer = SelectorContainerDepr.withTwoComboBoxes(testClassList, List.of("1", "2", "3"));
        selectorContainer.addOption(c -> c.setNameAndSearchBy(TestClass::getName));*/

        var selectorContainer =
                SelectorContainer.withAutocompletionTextField(testClassObservableList, List.of("qwerty", "asdfgh"))
                .sizePercentages(80, 20)
                .nameByProperty(TestClass::stringPropertyProperty)
                .get();

/*        var selectorContainer = new SelectorContainerDepr<>(testClassList, SearchComboBox::new);
        selectorContainer.addOption(n -> n.setNameAndSearchBy(TestClass::getName));
        selectorContainer.setSearchDialogNameBy(TestClass::getName);
        selectorContainer.setSearchDialogSearchBy(TestClass::getName);
        selectorContainer.setHasSearchButton(false);*/

/*        var selectorContainer = new SelectorContainerDepr<>(testClassList, SelectorElementTwoChoice::new);
        selectorContainer.addOption(n -> n.setNameAndSearchBy(TestClass::getName));
        selectorContainer.addOption(n -> n.setSecondCollection(Set.of("one", "two", "three")));*/

        hBox.setPrefWidth(300);
        hBox.setMinWidth(300);
        hBox.setMaxHeight(100);
        hBox.getChildren().add(selectorContainer);

        AutocompletionTextField<TestClass> autocompletionTextField = new AutocompletionTextField<>(testClassList);
        autocompletionTextField.setNameBy(TestClass::getName);
        autocompletionTextField.setType(AutocompletionTextField.Type.WORDS);
        autocompletionTextField.setWordsDelimiter("\\W");
        autocompletionTextField.setElementDescriptionFunction(t -> "(" + t.getValue() + ")");
        hBox.getChildren().add(autocompletionTextField);

        hBox.getChildren().add(searchComboBox);

        Button change1 = new Button("Change first");
        Button change2 = new Button("Change second");
        change1.setOnAction(event -> te1.stringProperty.set("changed"));
        change2.setOnAction(event -> te2.stringProperty.set("changed"));

        hBox.getChildren().addAll(change1, change2);

        searchComboBox.setOnAction(event -> System.out.println(searchComboBox.getValue().name));

    }

    enum TestEnum { ONE, TWO, THREE, FOUR, FIVE }

    class TestClass {
        private String name;
        private int value;
        private TestClass anotherReference;
        private boolean isTrue;
        private TextField textField;
        private List<String> collection;
        private Label label;
        private Image image;
        private SimpleStringProperty stringProperty = new SimpleStringProperty("created");

        public TestClass(String name, int value, boolean isTrue) {
            this.name = name;
            this.value = value;
            this.isTrue = isTrue;
            textField = new TextField();
            collection = List.of("One", "fourth");
            label = new Label("Label inside test class");
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }

        public TestClass getAnotherReference() {
            return anotherReference;
        }

        public void setAnotherReference(TestClass anotherReference) {
            this.anotherReference = anotherReference;
        }

        public boolean isTrue() {
            return isTrue;
        }

        public void setTrue(boolean aTrue) {
            isTrue = aTrue;
        }

        public TextField getTextField() {
            return textField;
        }

        public void setTextField(TextField textField) {
            this.textField = textField;
        }

        public List<String> getCollection() {
            return collection;
        }

        public void setCollection(List<String> collection) {
            this.collection = collection;
        }

        public Label getLabel() {
            return label;
        }

        public void setLabel(Label label) {
            this.label = label;
        }

        public Image getImage() {
            return image;
        }

        public void setImage(Image image) {
            this.image = image;
        }

        public String getStringProperty() {
            return stringProperty.get();
        }

        public SimpleStringProperty stringPropertyProperty() {
            return stringProperty;
        }

        public void setStringProperty(String stringProperty) {
            this.stringProperty.set(stringProperty);
        }
    }
}
