import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import ru.rdude.fxlib.containers.MultipleChoiceContainer;
import ru.rdude.fxlib.containers.MultipleChoiceContainerElementWithPercents;
import ru.rdude.fxlib.containers.MultipleChoiceContainerElementWithTextField;
import ru.rdude.fxlib.textfields.AutocomplitionTextField;

import java.io.IOException;
import java.util.List;

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
        container.setNodeElementType(MultipleChoiceContainerElementWithPercents.class);
        MultipleChoiceContainer<String> container2 = new MultipleChoiceContainer<>(testInputs);
        container2.setNodeElementType(MultipleChoiceContainerElementWithTextField.class);
        mainPane.setMinWidth(300);
        HBox hBox = new HBox(container, container2);
        container.setMinSize(400d, 400d);
        container.setMaxSize(400d, 400d);
        container2.setMinSize(400d, 400d);
        container2.setMaxSize(400d, 400d);
        mainPane.getChildren().add(hBox);
    }
}
