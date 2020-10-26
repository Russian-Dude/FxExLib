package ru.rdude.fxlib.containers;

import javafx.collections.FXCollections;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import ru.rdude.fxlib.panes.SearchPane;

import java.util.ArrayList;
import java.util.Collection;

public class SearchDialog<R> extends Dialog<R> {

    private DialogPane dialogPane;
    private SearchPane<R> searchPane;

    public SearchDialog() {
        this(new ArrayList<>());
    }

    public SearchDialog(Collection<R> collection) {
        super();
        this.dialogPane = new DialogPane();
        this.searchPane = new SearchPane<>(collection);
        setMainPanes();
        setDialogPane(dialogPane);
    }

    private void setMainPanes() {
        ButtonType buttonOk = new ButtonType("Select" ,ButtonBar.ButtonData.OK_DONE);
        dialogPane.getButtonTypes().add(buttonOk);
        dialogPane.setContent(searchPane);
    }

    public void setElements(Collection<R> collection) {
        //listView.setItems(FXCollections.observableList(new ArrayList<>(collection)));
    }

}
