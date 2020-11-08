import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import ru.rdude.fxlib.boxes.SearchComboBox;
import ru.rdude.fxlib.containers.*;
import ru.rdude.fxlib.panes.SearchPane;
import ru.rdude.fxlib.textfields.AutocomplitionTextField;
import ru.rdude.fxlib.textfields.AutocomplitionTextFieldSimple;

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
        SearchComboBox<TestClass> searchComboBox = new SearchComboBox<>();
        searchComboBox.setCollection(List.of(te1, te2, te3, te4, te5, te6));
        searchComboBox.setNameAndSearchBy(TestClass::getName);
        searchComboBox.setSearchBy(TestClass::getName, testClass -> String.valueOf(testClass.getValue()));

        MultipleChoiceContainerExtended<TestEnum, SearchPane> container = new MultipleChoiceContainerExtended<>(List.of(TestEnum.values()));
        container.setNodeElementType(MultipleChoiceContainerElementTwoChoiceWithPercents.class);
        MultipleChoiceContainerElementTwoChoice.ExtendedOptionsBuilder<TestClass, SearchPane> optionsBuilder = MultipleChoiceContainerElementTwoChoice.extendedOptionsBuilder();
        optionsBuilder.setCollection(testClassList)
                .setNameByFunction(TestClass::getName)
                .setSearchByFunction(TestClass::getName)
                .setExtendedSearchNode(SearchPane.class)
                .addExtendedSearchFunction(SearchPane::getSearchTextField, TestClass::getName);
        container.setExtendedOptions(optionsBuilder);
        container.setNameBy(TestEnum::name);
        container.setSearchBy(TestEnum::name);
        container.setUniqueElements(true);


/*
 *                first option - collection of the second value Search Combo Box (Collection);
 *                second option - nameBy function for Search Combo Box (Function V, String);
 *                third option - searchBy function for SearchComboBox (Function V, String);
 *                fourth option- node class for extended search (Class or FXMLLoader);
 *                fifth option - search options.
 */
        TitledMultipleChoiceContainer<TestClass> container2 = new TitledMultipleChoiceContainer<>(testClassList, "test node");
        container2.setNodeElementType(MultipleChoiceContainerElementWithTextField.class);
        TitledMultipleChoiceContainer<TestClass> container3 = new TitledMultipleChoiceContainer<>(testClassList, "test node");
        container3.setNodeElementType(MultipleChoiceContainerElementWithAutofillTextField.class);
        container3.setExtendedOptions(List.of("one", "two", "three", "four", "five"));

        AutocomplitionTextField<TestClass> autocomplitionTextField = new AutocomplitionTextField();
        autocomplitionTextField.setElements(testClassList);
        autocomplitionTextField.setItemNameFunction(TestClass::getName);
        autocomplitionTextField.setExtendedDescriptionFunction(testClass -> testClass.getName().toUpperCase() + " test description");

        mainPane.setMinWidth(300);
        VBox vBox = new VBox(container2, container3, autocomplitionTextField);

        TextField textField = new TextField();
        RadioButton radioButton = new RadioButton("fourth");

        Map<Control, Function<TestClass, ?>> map = new HashMap<>();
        map.put(textField, TestClass::getName);
        map.put(radioButton, TestClass::getCollection);

        SearchPane<TestClass> searchPane = new SearchPane<>(testClassList);
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


        HBox hBox = new HBox(container, vBox, searchComboBox, searchPane, textField, radioButton);
        container.setMinSize(400d, 400d);
        container.setMaxSize(400d, 400d);

        mainPane.getChildren().add(hBox);
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
    }
}
