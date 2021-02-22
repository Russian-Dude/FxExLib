package ru.rdude.fxlib.dialogs;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import ru.rdude.fxlib.panes.SearchPane;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Dialog with SearchPane. Returns Optional of SearchPane selected value after show methods called when select button pressed.
 *
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
        getDialogPane().setPadding(new Insets(10));
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
        // double click on element:
        searchPane.getListView().setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2 && event.getTarget() instanceof Cell) {
                resultProperty().setValue(searchPane.getListView().selectionModelProperty().get().getSelectedItem());
                close();
            }
        });
    }

    public void setCollection(Collection<R> collection) {
        searchPane.setCollection(collection);
    }

    public SearchPane<R> getSearchPane() {
        return searchPane;
    }


}
