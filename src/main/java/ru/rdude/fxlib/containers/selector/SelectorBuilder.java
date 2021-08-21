package ru.rdude.fxlib.containers.selector;

import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.stage.StageStyle;
import org.intellij.lang.annotations.RegExp;
import ru.rdude.fxlib.boxes.SearchComboBox;
import ru.rdude.fxlib.textfields.AutocompletionTextField;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class SelectorBuilder {
    private SelectorBuilder() { }

    public static <T> SimpleSelectorBuilder<T> simple (Collection<T> elements) {
        return new SimpleSelectorBuilder<>(elements);
    }

    public static <T, V> AutocompletionTextFieldSelectorBuilder<T, V> withAutoCompletionTextField(
            Collection<T> mainElements, Collection<V> autoCompletionElements) {
        return new AutocompletionTextFieldSelectorBuilder<>(mainElements, autoCompletionElements);
    }

    public static <T> PercentSelectorBuilder<T> withPercents (Collection<T> elements) {
        return new PercentSelectorBuilder<>(elements);
    }

    public static <T> TextFieldSelectorBuilder<T> withTextField (Collection<T> elements) {
        return new TextFieldSelectorBuilder<>(elements);
    }

    public static <T, V> TwoComboBoxesSelectorBuilder<T, V> withTwoComboBoxes (Collection<T> mainElements, Collection<V> secondElements) {
        return new TwoComboBoxesSelectorBuilder<>(mainElements, secondElements);
    }

    public static <T, P extends Node> WithPropertiesWindowSelectorBuilder<T, P> withPropertiesWindow (Collection<T> elements, Supplier<P> propertiesCreator) {
        return new WithPropertiesWindowSelectorBuilder<>(elements, propertiesCreator);
    }



    public static abstract class SelectorBuilderType<T, E extends Node & SelectorElementNode<T>> {
        final SelectorContainer<T, E> selectorContainer;

        private SelectorBuilderType(Collection<T> elements, Supplier<E> elementNodeCreator) {
            this.selectorContainer = new SelectorContainer<>(elements, elementNodeCreator);
        }

        public SelectorBuilderType<T, E> addOption(Consumer<E> option) {
            selectorContainer.addOption(option);
            return this;
        }

        public SelectorContainer<T, E> get() {
            return selectorContainer;
        }

        public abstract SelectorBuilderType<T, E> nameBy(Function<T, String> function);
        public abstract SelectorBuilderType<T, E> nameByProperty(Function<T, ObservableValue<String>> function);
        public abstract SelectorBuilderType<T, E> searchBy(Function<T, String> function, Function<T, String>... functions);
        public abstract SelectorBuilderType<T, E> searchByProperty(Function<T, ObservableValue<String>> function, Function<T, ObservableValue<String>>... functions);

    }


    ////////////////////////////////////////////////////
    //                                                //
    //                 S I M P L E                    //
    //                                                //
    ////////////////////////////////////////////////////


    public final static class SimpleSelectorBuilder<T> extends SelectorBuilderType<T, SearchComboBox<T>> {
        private SimpleSelectorBuilder(Collection<T> collection) {
            super(collection, () -> new SearchComboBox<>(collection));
        }

        @Override
        public final SimpleSelectorBuilder<T> nameBy(Function<T, String> function) {
            selectorContainer.setSearchDialogNameBy(function);
            selectorContainer.addOption(s -> s.setNameBy(function));
            return this;
        }

        @Override
        public final SimpleSelectorBuilder<T> nameByProperty(Function<T, ObservableValue<String>> function) {
            selectorContainer.setSearchDialogNameByProperty(function);
            selectorContainer.addOption(s -> s.setNameByProperty(function));
            return this;
        }

        @SafeVarargs
        @Override
        public final SimpleSelectorBuilder<T> searchBy(Function<T, String> function, Function<T, String>... functions) {
            selectorContainer.setSearchDialogSearchBy(function, functions);
            selectorContainer.addOption(s -> s.setSearchBy(function, functions));
            return this;
        }

        @SafeVarargs
        @Override
        public final SimpleSelectorBuilder<T> searchByProperty(Function<T, ObservableValue<String>> function, Function<T, ObservableValue<String>>... functions) {
            selectorContainer.setSearchDialogSearchByProperty(function, functions);
            selectorContainer.addOption(s -> s.setSearchByProperty(function, functions));
            return this;
        }

        public final SimpleSelectorBuilder<T> disableSearch() {
            selectorContainer.addOption(s -> s.setSearchEnabled(false));
            return this;
        }
    }


    ////////////////////////////////////////////////////
    //                                                //
    //         AUTOCOMPLETION TEXT FIELD              //
    //                                                //
    ////////////////////////////////////////////////////


    public static final class AutocompletionTextFieldSelectorBuilder<T, V>
            extends SelectorBuilderType<T, SelectorElementAutocompletionTextField<T, V>> {

        private AutocompletionTextFieldSelectorBuilder(Collection<T> collection, Collection<V> autocompletionCollection) {
            super(collection, SelectorElementAutocompletionTextField::new);
            selectorContainer.addOption(n -> n.setTextFieldElements(autocompletionCollection));
        }

        @Override
        public final AutocompletionTextFieldSelectorBuilder<T, V> nameBy(Function<T, String> function) {
            selectorContainer.setSearchDialogNameBy(function);
            selectorContainer.addOption(s -> s.setNameBy(function));
            return this;
        }

        @Override
        public final AutocompletionTextFieldSelectorBuilder<T, V> nameByProperty(Function<T, ObservableValue<String>> function) {
            selectorContainer.setSearchDialogNameByProperty(function);
            selectorContainer.addOption(s -> s.setNameByProperty(function));
            return this;
        }

        @SafeVarargs
        @Override
        public final AutocompletionTextFieldSelectorBuilder<T, V> searchBy(Function<T, String> function, Function<T, String>... functions) {
            selectorContainer.setSearchDialogSearchBy(function, functions);
            selectorContainer.addOption(s -> s.setSearchBy(function, functions));
            return this;
        }

        @SafeVarargs
        @Override
        public final AutocompletionTextFieldSelectorBuilder<T, V> searchByProperty(Function<T, ObservableValue<String>> function, Function<T, ObservableValue<String>>... functions) {
            selectorContainer.setSearchDialogSearchByProperty(function, functions);
            selectorContainer.addOption(s -> s.setSearchByProperty(function, functions));
            return this;
        }

        public final AutocompletionTextFieldSelectorBuilder<T, V> textFieldNameBy(Function<V, String> function) {
            selectorContainer.addOption(s -> s.setTextFieldNameBy(function));
            return this;
        }

        public final AutocompletionTextFieldSelectorBuilder<T, V> textFieldNameByProperty(Function<V, ObservableValue<String>> function) {
            selectorContainer.addOption(s -> s.setTextFieldNameByProperty(function));
            return this;
        }

        public final AutocompletionTextFieldSelectorBuilder<T, V> textFieldType(AutocompletionTextField.Type type) {
            selectorContainer.addOption(s -> s.setTextFieldType(type));
            return this;
        }

        public final AutocompletionTextFieldSelectorBuilder<T, V> textFieldDescription(Function<V, String> function) {
            selectorContainer.addOption(s -> s.setTextFieldDescriptionFunction(function));
            return this;
        }

        public final AutocompletionTextFieldSelectorBuilder<T, V> textFieldDescriptionByProperty(Function<V, ObservableValue<String>> function) {
            selectorContainer.addOption(s -> s.setTextFieldDescriptionByPropertyFunction(function));
            return this;
        }

        public final AutocompletionTextFieldSelectorBuilder<T, V> wordsDelimiter(@RegExp String regex) {
            selectorContainer.addOption(s -> s.setWordsDelimiter(regex));
            return this;
        }

        public final AutocompletionTextFieldSelectorBuilder<T, V> disableSearch() {
            selectorContainer.addOption(s -> s.setSearchEnabled(false));
            return this;
        }

        public final AutocompletionTextFieldSelectorBuilder<T, V> sizePercentages(double comboBox, double textField) {
            selectorContainer.addOption(s -> s.setSizePercentages(comboBox, textField));
            return this;
        }
    }


    ////////////////////////////////////////////////////
    //                                                //
    //                 P E R C E N T                  //
    //                                                //
    ////////////////////////////////////////////////////


    public static class PercentSelectorBuilder<T> extends SelectorBuilderType<T, SelectorElementPercent<T>> {

        private PercentSelectorBuilder(Collection<T> elements) {
            super(elements, SelectorElementPercent::new);
        }

        @Override
        public final PercentSelectorBuilder<T> nameBy(Function<T, String> function) {
            selectorContainer.setSearchDialogNameBy(function);
            selectorContainer.addOption(s -> s.setNameBy(function));
            return this;
        }

        @Override
        public final PercentSelectorBuilder<T> nameByProperty(Function<T, ObservableValue<String>> function) {
            selectorContainer.setSearchDialogNameByProperty(function);
            selectorContainer.addOption(s -> s.setNameByProperty(function));
            return this;
        }

        @SafeVarargs
        @Override
        public final PercentSelectorBuilder<T> searchBy(Function<T, String> function, Function<T, String>... functions) {
            selectorContainer.setSearchDialogSearchBy(function, functions);
            selectorContainer.addOption(s -> s.setSearchBy(function, functions));
            return this;
        }

        @SafeVarargs
        @Override
        public final PercentSelectorBuilder<T> searchByProperty(Function<T, ObservableValue<String>> function, Function<T, ObservableValue<String>>... functions) {
            selectorContainer.setSearchDialogSearchByProperty(function, functions);
            selectorContainer.addOption(s -> s.setSearchByProperty(function, functions));
            return this;
        }

        public final PercentSelectorBuilder<T> disableSearch() {
            selectorContainer.addOption(s -> s.setSearchEnabled(false));
            return this;
        }

        public final PercentSelectorBuilder<T> sizePercentages(double comboBox, double textField) {
            selectorContainer.addOption(s -> s.setSizePercentages(comboBox, textField));
            return this;
        }
    }


    ////////////////////////////////////////////////////
    //                                                //
    //                   TEXT FIELD                   //
    //                                                //
    ////////////////////////////////////////////////////


    public static class TextFieldSelectorBuilder<T> extends SelectorBuilderType<T, SelectorElementTextField<T>> {

        private TextFieldSelectorBuilder(Collection<T> elements) {
            super(elements, SelectorElementTextField::new);
        }

        @Override
        public final TextFieldSelectorBuilder<T> nameBy(Function<T, String> function) {
            selectorContainer.setSearchDialogNameBy(function);
            selectorContainer.addOption(s -> s.setNameBy(function));
            return this;
        }

        @Override
        public final TextFieldSelectorBuilder<T> nameByProperty(Function<T, ObservableValue<String>> function) {
            selectorContainer.setSearchDialogNameByProperty(function);
            selectorContainer.addOption(s -> s.setNameByProperty(function));
            return this;
        }

        @SafeVarargs
        @Override
        public final TextFieldSelectorBuilder<T> searchBy(Function<T, String> function, Function<T, String>... functions) {
            selectorContainer.setSearchDialogSearchBy(function, functions);
            selectorContainer.addOption(s -> s.setSearchBy(function, functions));
            return this;
        }

        @SafeVarargs
        @Override
        public final TextFieldSelectorBuilder<T> searchByProperty(Function<T, ObservableValue<String>> function, Function<T, ObservableValue<String>>... functions) {
            selectorContainer.setSearchDialogSearchByProperty(function, functions);
            selectorContainer.addOption(s -> s.setSearchByProperty(function, functions));
            return this;
        }

        public final TextFieldSelectorBuilder<T> disableSearch() {
            selectorContainer.addOption(s -> s.setSearchEnabled(false));
            return this;
        }

        public final TextFieldSelectorBuilder<T> sizePercentages(double comboBox, double textField) {
            selectorContainer.addOption(s -> s.setSizePercentages(comboBox, textField));
            return this;
        }
    }


    ////////////////////////////////////////////////////
    //                                                //
    //                 TWO COMBO BOXES                //
    //                                                //
    ////////////////////////////////////////////////////


    public static class TwoComboBoxesSelectorBuilder<T, V> extends SelectorBuilderType<T, SelectorElementTwoChoice<T, V>> {

        public TwoComboBoxesSelectorBuilder(Collection<T> mainCollection, Collection<V> secondCollection) {
            super(mainCollection, SelectorElementTwoChoice::new);
            addOption(n -> n.setSecondCollection(secondCollection));
        }

        @Override
        public final TwoComboBoxesSelectorBuilder<T, V> nameBy(Function<T, String> function) {
            selectorContainer.setSearchDialogNameBy(function);
            selectorContainer.addOption(s -> s.setNameBy(function));
            return this;
        }

        @Override
        public final TwoComboBoxesSelectorBuilder<T, V> nameByProperty(Function<T, ObservableValue<String>> function) {
            selectorContainer.setSearchDialogNameByProperty(function);
            selectorContainer.addOption(s -> s.setNameByProperty(function));
            return this;
        }

        @SafeVarargs
        @Override
        public final TwoComboBoxesSelectorBuilder<T, V> searchBy(Function<T, String> function, Function<T, String>... functions) {
            selectorContainer.setSearchDialogSearchBy(function, functions);
            selectorContainer.addOption(s -> s.setSearchBy(function, functions));
            return this;
        }

        @SafeVarargs
        @Override
        public final TwoComboBoxesSelectorBuilder<T, V> searchByProperty(Function<T, ObservableValue<String>> function, Function<T, ObservableValue<String>>... functions) {
            selectorContainer.setSearchDialogSearchByProperty(function, functions);
            selectorContainer.addOption(s -> s.setSearchByProperty(function, functions));
            return this;
        }

        public final TwoComboBoxesSelectorBuilder<T, V> disableSearch() {
            selectorContainer.addOption(s -> s.setSearchEnabled(false));
            return this;
        }

        public final TwoComboBoxesSelectorBuilder<T, V> sizePercentages(double comboBox, double textField) {
            selectorContainer.addOption(s -> s.setSizePercentages(comboBox, textField));
            return this;
        }

        public final TwoComboBoxesSelectorBuilder<T, V> secondBoxNameBy(Function<V, String> function) {
            selectorContainer.addOption(s -> s.setSecondNameBy(function));
            return this;
        }

        public final TwoComboBoxesSelectorBuilder<T, V> secondBoxNameByProperty(Function<V, ObservableValue<String>> function) {
            selectorContainer.addOption(s -> s.setSecondNameByProperty(function));
            return this;
        }

        @SafeVarargs
        public final TwoComboBoxesSelectorBuilder<T, V> secondBoxSearchBy(Function<V, String> function, Function<V, String>... functions) {
            selectorContainer.addOption(s -> s.setSecondSearchBy(function, functions));
            return this;
        }

        @SafeVarargs
        public final TwoComboBoxesSelectorBuilder<T, V> secondBoxSearchByProperty(Function<V, ObservableValue<String>> function, Function<V, ObservableValue<String>>... functions) {
            selectorContainer.addOption(s -> s.setSecondSearchByProperty(function, functions));
            return this;
        }
    }

    ////////////////////////////////////////////////////
    //                                                //
    //              WITH PROPERTIES WINDOW            //
    //                                                //
    ////////////////////////////////////////////////////

    public static class WithPropertiesWindowSelectorBuilder<T, P extends Node> extends SelectorBuilderType<T, SelectorElementWindowProperties<T, P>> {

        public WithPropertiesWindowSelectorBuilder(Collection<T> mainCollection, Supplier<P> propertiesWindowCreator) {
            super(mainCollection, () -> new SelectorElementWindowProperties<>(propertiesWindowCreator.get()));
        }

        @Override
        public final WithPropertiesWindowSelectorBuilder<T, P> nameBy(Function<T, String> function) {
            selectorContainer.setSearchDialogNameBy(function);
            selectorContainer.addOption(s -> s.setNameBy(function));
            return this;
        }

        @Override
        public final WithPropertiesWindowSelectorBuilder<T, P> nameByProperty(Function<T, ObservableValue<String>> function) {
            selectorContainer.setSearchDialogNameByProperty(function);
            selectorContainer.addOption(s -> s.setNameByProperty(function));
            return this;
        }

        @SafeVarargs
        @Override
        public final WithPropertiesWindowSelectorBuilder<T, P> searchBy(Function<T, String> function, Function<T, String>... functions) {
            selectorContainer.setSearchDialogSearchBy(function, functions);
            selectorContainer.addOption(s -> s.setSearchBy(function, functions));
            return this;
        }

        @SafeVarargs
        @Override
        public final WithPropertiesWindowSelectorBuilder<T, P> searchByProperty(Function<T, ObservableValue<String>> function, Function<T, ObservableValue<String>>... functions) {
            selectorContainer.setSearchDialogSearchByProperty(function, functions);
            selectorContainer.addOption(s -> s.setSearchByProperty(function, functions));
            return this;
        }

        public final WithPropertiesWindowSelectorBuilder<T, P> disableSearch() {
            selectorContainer.addOption(s -> s.setSearchEnabled(false));
            return this;
        }

        public final WithPropertiesWindowSelectorBuilder<T, P> stageStile(StageStyle stageStyle) {
            selectorContainer.addOption(s -> s.getPropertiesWindow().initStyle(stageStyle));
            return this;
        }

        public final WithPropertiesWindowSelectorBuilder<T, P> stageOptions(Consumer<SelectorElementWindowProperties<?, ?>.PropertiesWindow> customize) {
            selectorContainer.addOption(s -> customize.accept(s.getPropertiesWindow()));
            return this;
        }

        public final WithPropertiesWindowSelectorBuilder<T, P> propertiesButtonOptions(Consumer<Button> customize) {
            selectorContainer.addOption(s -> customize.accept(s.getButton()));
            return this;
        }

    }

}
