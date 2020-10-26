package ru.rdude.fxlib.containers;

import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import ru.rdude.fxlib.panes.SearchPane;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Dialog with SearchPane. Returns Optional of SearchPane selected value after show methods called when select button pressed.
 * @param <R> result.
 */
public class SearchDialog<R> extends Dialog<R> {

    private SearchPane<R> searchPane;

    public SearchDialog() {
        this(new ArrayList<>());
    }

    public SearchDialog(Collection<R> collection) {
        super();
        this.searchPane = new SearchPane<>(collection);
        setMainPanes();
    }

    private void setMainPanes() {
        ButtonType buttonOk = new ButtonType("Select", ButtonBar.ButtonData.OK_DONE);
        ButtonType buttonCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        getDialogPane().getButtonTypes().add(buttonOk);
        getDialogPane().getButtonTypes().add(buttonCancel);
        ((Button) getDialogPane().lookupButton(buttonOk))
                .setOnAction(event -> {
                    R selectedItem = searchPane.getListView().selectionModelProperty().get().getSelectedItem();
                    resultProperty().setValue(selectedItem);
                });
        ((Button) getDialogPane().lookupButton(buttonCancel)).setOnAction(event -> resultProperty().setValue(null));
        // due to javaFx Dialog class returns ButtonType instead of Optional with null value (as expected)
        // this set result converter to null is needed:
        setResultConverter(buttonType -> null);
        getDialogPane().setContent(searchPane);
    }

    public SearchPane<R> getSearchPane() {
        return searchPane;
    }


}
