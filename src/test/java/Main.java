import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Control;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import ru.rdude.fxlib.boxes.SearchComboBox;
import ru.rdude.fxlib.containers.*;
import ru.rdude.fxlib.panes.SearchPane;
import ru.rdude.fxlib.textfields.AutocomplitionTextField;

import java.io.IOException;
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
        List<String> testInputs = List.of("First", "Second", "Third");
        MultipleChoiceContainer<String> container = new MultipleChoiceContainer<>(testInputs);
        container.setNodeElementType(MultipleChoiceContainerExtendedElement.class);
        TitledMultipleChoiceContainer<String> container2 = new TitledMultipleChoiceContainer<>(testInputs, "test node");
        container2.setNodeElementType(MultipleChoiceContainerElementWithTextField.class);
        TitledMultipleChoiceContainer<String> container3 = new TitledMultipleChoiceContainer<>(testInputs, "test node");
        container3.setNodeElementType(MultipleChoiceContainerElementWithAutofillTextField.class);
        mainPane.setMinWidth(300);
        VBox vBox = new VBox(container2, container3);

        TestClass te1 = new TestClass("first", 1, true);
        TestClass te2 = new TestClass("second", 2, false);
        TestClass te3 = new TestClass("third", 3, true);
        TestClass te4 = new TestClass("fourth", 4, false);
        TestClass te5 = new TestClass("second", 5, true);
        TestClass te6 = new TestClass("fifth", 6, false);
        List<TestClass> testClassList = List.of(te1, te2, te3, te4, te5, te6);
        SearchComboBox<TestClass> searchComboBox = new SearchComboBox<>();
        searchComboBox.setCollection(List.of(te1, te2, te3, te4, te5, te6));
        searchComboBox.setNameAndSearchBy(TestClass::getName);
        searchComboBox.setSearchBy(TestClass::getName, testClass -> String.valueOf(testClass.getValue()));

        TextField textField = new TextField();
        RadioButton radioButton = new RadioButton("fourth");

        Map<Control, Function<TestClass, ?>> map = new HashMap<>();
        map.put(textField, TestClass::getName);
        map.put(radioButton, TestClass::isTrue);

        SearchPane<TestClass> searchPane = new SearchPane<>(testClassList);
        searchPane.setTextFieldSearchBy(TestClass::getName, (t) -> String.valueOf(t.getValue()));
        searchPane.setNameBy(TestClass::getName);
        searchPane.addSearchOptions(map);


        HBox hBox = new HBox(container, vBox, searchComboBox, searchPane, textField, radioButton);
        container.setMinSize(400d, 400d);
        container.setMaxSize(400d, 400d);

        mainPane.getChildren().add(hBox);
    }

    class TestClass {
        private String name;
        private int value;
        private TestClass anotherReference;
        private boolean isTrue;

        public TestClass(String name, int value, boolean isTrue) {
            this.name = name;
            this.value = value;
            this.isTrue = isTrue;
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
    }
}
