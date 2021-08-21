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
    SelectorBuilder() { }

    public static <T> SimpleSelectorBuilder<T, SearchComboBox<T>> simple (Collection<T> elements) {
        return new SimpleSelectorBuilder<>(elements, SearchComboBox::new);
    }

    public static <T, E extends SearchComboBox<T>> SimpleSelectorBuilder<T, E> simple (Collection<T> elements, Supplier<E> creator) {
        return new SimpleSelectorBuilder<>(elements, creator);
    }

    public static <T, V> AutocompletionTextFieldSelectorBuilder<T, V, SelectorElementAutocompletionTextField<T, V>> withAutoCompletionTextField(
            Collection<T> mainElements, Collection<V> autoCompletionElements) {
        return new AutocompletionTextFieldSelectorBuilder<>(mainElements, autoCompletionElements, SelectorElementAutocompletionTextField::new);
    }

    public static <T, V, E extends SelectorElementAutocompletionTextField<T, V>> AutocompletionTextFieldSelectorBuilder<T, V, E> withAutoCompletionTextField(
            Collection<T> mainElements, Collection<V> autoCompletionElements, Supplier<E> creator) {
        return new AutocompletionTextFieldSelectorBuilder<>(mainElements, autoCompletionElements, creator);
    }

    public static <T> PercentSelectorBuilder<T, SelectorElementPercent<T>> withPercents (Collection<T> elements) {
        return new PercentSelectorBuilder<>(elements, SelectorElementPercent::new);
    }

    public static <T, E extends SelectorElementPercent<T>> PercentSelectorBuilder<T, E> withPercents (Collection<T> elements, Supplier<E> creator) {
        return new PercentSelectorBuilder<>(elements, creator);
    }

    public static <T> TextFieldSelectorBuilder<T, SelectorElementTextField<T>> withTextField (Collection<T> elements) {
        return new TextFieldSelectorBuilder<>(elements, SelectorElementTextField::new);
    }

    public static <T, E extends SelectorElementTextField<T>> TextFieldSelectorBuilder<T, E> withTextField (Collection<T> elements, Supplier<E> creator) {
        return new TextFieldSelectorBuilder<>(elements, creator);
    }

    public static <T, V> TwoComboBoxesSelectorBuilder<T, V, SelectorElementTwoChoice<T, V>> withTwoComboBoxes (Collection<T> mainElements, Collection<V> secondElements) {
        return new TwoComboBoxesSelectorBuilder<>(mainElements, secondElements, SelectorElementTwoChoice::new);
    }

    public static <T, V, E extends SelectorElementTwoChoice<T, V>> TwoComboBoxesSelectorBuilder<T, V, E> withTwoComboBoxes (Collection<T> mainElements, Collection<V> secondElements, Supplier<E> creator) {
        return new TwoComboBoxesSelectorBuilder<>(mainElements, secondElements, creator);
    }

    public static <T, P extends Node> WithPropertiesWindowSelectorBuilder<T, P, SelectorElementWindowProperties<T, P>> withPropertiesWindow (Collection<T> elements, Supplier<P> propertiesCreator) {
        return new WithPropertiesWindowSelectorBuilder<>(elements, propertiesCreator, SelectorElementWindowProperties::new);
    }

    public static <T, P extends Node, E extends SelectorElementWindowProperties<T, P>> WithPropertiesWindowSelectorBuilder<T, P, E> withPropertiesWindow (Collection<T> elements, Supplier<P> propertiesCreator, Function<P, E> creator) {
        return new WithPropertiesWindowSelectorBuilder<>(elements, propertiesCreator, creator);
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


    public final static class SimpleSelectorBuilder<T, E extends SearchComboBox<T>> extends SelectorBuilderType<T, E> {
        private SimpleSelectorBuilder(Collection<T> collection, Supplier<E> creator) {
            super(collection, creator);
        }

        @Override
        public final SimpleSelectorBuilder<T, E> nameBy(Function<T, String> function) {
            selectorContainer.setSearchDialogNameBy(function);
            selectorContainer.addOption(s -> s.setNameBy(function));
            return this;
        }

        @Override
        public final SimpleSelectorBuilder<T, E> nameByProperty(Function<T, ObservableValue<String>> function) {
            selectorContainer.setSearchDialogNameByProperty(function);
            selectorContainer.addOption(s -> s.setNameByProperty(function));
            return this;
        }

        @SafeVarargs
        @Override
        public final SimpleSelectorBuilder<T, E> searchBy(Function<T, String> function, Function<T, String>... functions) {
            selectorContainer.setSearchDialogSearchBy(function, functions);
            selectorContainer.addOption(s -> s.setSearchBy(function, functions));
            return this;
        }

        @SafeVarargs
        @Override
        public final SimpleSelectorBuilder<T, E> searchByProperty(Function<T, ObservableValue<String>> function, Function<T, ObservableValue<String>>... functions) {
            selectorContainer.setSearchDialogSearchByProperty(function, functions);
            selectorContainer.addOption(s -> s.setSearchByProperty(function, functions));
            return this;
        }

        public final SimpleSelectorBuilder<T, E> disableSearch() {
            selectorContainer.addOption(s -> s.setSearchEnabled(false));
            return this;
        }
    }


    ////////////////////////////////////////////////////
    //                                                //
    //         AUTOCOMPLETION TEXT FIELD              //
    //                                                //
    ////////////////////////////////////////////////////


    public static final class AutocompletionTextFieldSelectorBuilder<T, V, E extends SelectorElementAutocompletionTextField<T, V>>
            extends SelectorBuilderType<T, E> {

        private AutocompletionTextFieldSelectorBuilder(Collection<T> collection, Collection<V> autocompletionCollection, Supplier<E> creator) {
            super(collection, creator);
            selectorContainer.addOption(n -> n.setTextFieldElements(autocompletionCollection));
        }

        @Override
        public final AutocompletionTextFieldSelectorBuilder<T, V, E> nameBy(Function<T, String> function) {
            selectorContainer.setSearchDialogNameBy(function);
            selectorContainer.addOption(s -> s.setNameBy(function));
            return this;
        }

        @Override
        public final AutocompletionTextFieldSelectorBuilder<T, V, E> nameByProperty(Function<T, ObservableValue<String>> function) {
            selectorContainer.setSearchDialogNameByProperty(function);
            selectorContainer.addOption(s -> s.setNameByProperty(function));
            return this;
        }

        @SafeVarargs
        @Override
        public final AutocompletionTextFieldSelectorBuilder<T, V, E> searchBy(Function<T, String> function, Function<T, String>... functions) {
            selectorContainer.setSearchDialogSearchBy(function, functions);
            selectorContainer.addOption(s -> s.setSearchBy(function, functions));
            return this;
        }

        @SafeVarargs
        @Override
        public final AutocompletionTextFieldSelectorBuilder<T, V, E> searchByProperty(Function<T, ObservableValue<String>> function, Function<T, ObservableValue<String>>... functions) {
            selectorContainer.setSearchDialogSearchByProperty(function, functions);
            selectorContainer.addOption(s -> s.setSearchByProperty(function, functions));
            return this;
        }

        public final AutocompletionTextFieldSelectorBuilder<T, V, E> textFieldNameBy(Function<V, String> function) {
            selectorContainer.addOption(s -> s.setTextFieldNameBy(function));
            return this;
        }

        public final AutocompletionTextFieldSelectorBuilder<T, V, E> textFieldNameByProperty(Function<V, ObservableValue<String>> function) {
            selectorContainer.addOption(s -> s.setTextFieldNameByProperty(function));
            return this;
        }

        public final AutocompletionTextFieldSelectorBuilder<T, V, E> textFieldType(AutocompletionTextField.Type type) {
            selectorContainer.addOption(s -> s.setTextFieldType(type));
            return this;
        }

        public final AutocompletionTextFieldSelectorBuilder<T, V, E> textFieldDescription(Function<V, String> function) {
            selectorContainer.addOption(s -> s.setTextFieldDescriptionFunction(function));
            return this;
        }

        public final AutocompletionTextFieldSelectorBuilder<T, V, E> textFieldDescriptionByProperty(Function<V, ObservableValue<String>> function) {
            selectorContainer.addOption(s -> s.setTextFieldDescriptionByPropertyFunction(function));
            return this;
        }

        public final AutocompletionTextFieldSelectorBuilder<T, V, E> wordsDelimiter(@RegExp String regex) {
            selectorContainer.addOption(s -> s.setWordsDelimiter(regex));
            return this;
        }

        public final AutocompletionTextFieldSelectorBuilder<T, V, E> disableSearch() {
            selectorContainer.addOption(s -> s.setSearchEnabled(false));
            return this;
        }

        public final AutocompletionTextFieldSelectorBuilder<T, V, E> sizePercentages(double comboBox, double textField) {
            selectorContainer.addOption(s -> s.setSizePercentages(comboBox, textField));
            return this;
        }
    }


    ////////////////////////////////////////////////////
    //                                                //
    //                 P E R C E N T                  //
    //                                                //
    ////////////////////////////////////////////////////


    public static class PercentSelectorBuilder<T, E extends SelectorElementPercent<T>> extends SelectorBuilderType<T, E> {

        private PercentSelectorBuilder(Collection<T> elements, Supplier<E> creator) {
            super(elements, creator);
        }

        @Override
        public final PercentSelectorBuilder<T, E> nameBy(Function<T, String> function) {
            selectorContainer.setSearchDialogNameBy(function);
            selectorContainer.addOption(s -> s.setNameBy(function));
            return this;
        }

        @Override
        public final PercentSelectorBuilder<T, E> nameByProperty(Function<T, ObservableValue<String>> function) {
            selectorContainer.setSearchDialogNameByProperty(function);
            selectorContainer.addOption(s -> s.setNameByProperty(function));
            return this;
        }

        @SafeVarargs
        @Override
        public final PercentSelectorBuilder<T, E> searchBy(Function<T, String> function, Function<T, String>... functions) {
            selectorContainer.setSearchDialogSearchBy(function, functions);
            selectorContainer.addOption(s -> s.setSearchBy(function, functions));
            return this;
        }

        @SafeVarargs
        @Override
        public final PercentSelectorBuilder<T, E> searchByProperty(Function<T, ObservableValue<String>> function, Function<T, ObservableValue<String>>... functions) {
            selectorContainer.setSearchDialogSearchByProperty(function, functions);
            selectorContainer.addOption(s -> s.setSearchByProperty(function, functions));
            return this;
        }

        public final PercentSelectorBuilder<T, E> disableSearch() {
            selectorContainer.addOption(s -> s.setSearchEnabled(false));
            return this;
        }

        public final PercentSelectorBuilder<T, E> sizePercentages(double comboBox, double textField) {
            selectorContainer.addOption(s -> s.setSizePercentages(comboBox, textField));
            return this;
        }
    }


    ////////////////////////////////////////////////////
    //                                                //
    //                   TEXT FIELD                   //
    //                                                //
    ////////////////////////////////////////////////////


    public static class TextFieldSelectorBuilder<T, E extends SelectorElementTextField<T>> extends SelectorBuilderType<T, E> {

        private TextFieldSelectorBuilder(Collection<T> elements, Supplier<E> creator) {
            super(elements, creator);
        }

        @Override
        public final TextFieldSelectorBuilder<T, E> nameBy(Function<T, String> function) {
            selectorContainer.setSearchDialogNameBy(function);
            selectorContainer.addOption(s -> s.setNameBy(function));
            return this;
        }

        @Override
        public final TextFieldSelectorBuilder<T, E> nameByProperty(Function<T, ObservableValue<String>> function) {
            selectorContainer.setSearchDialogNameByProperty(function);
            selectorContainer.addOption(s -> s.setNameByProperty(function));
            return this;
        }

        @SafeVarargs
        @Override
        public final TextFieldSelectorBuilder<T, E> searchBy(Function<T, String> function, Function<T, String>... functions) {
            selectorContainer.setSearchDialogSearchBy(function, functions);
            selectorContainer.addOption(s -> s.setSearchBy(function, functions));
            return this;
        }

        @SafeVarargs
        @Override
        public final TextFieldSelectorBuilder<T, E> searchByProperty(Function<T, ObservableValue<String>> function, Function<T, ObservableValue<String>>... functions) {
            selectorContainer.setSearchDialogSearchByProperty(function, functions);
            selectorContainer.addOption(s -> s.setSearchByProperty(function, functions));
            return this;
        }

        public final TextFieldSelectorBuilder<T, E> disableSearch() {
            selectorContainer.addOption(s -> s.setSearchEnabled(false));
            return this;
        }

        public final TextFieldSelectorBuilder<T, E> sizePercentages(double comboBox, double textField) {
            selectorContainer.addOption(s -> s.setSizePercentages(comboBox, textField));
            return this;
        }
    }


    ////////////////////////////////////////////////////
    //                                                //
    //                 TWO COMBO BOXES                //
    //                                                //
    ////////////////////////////////////////////////////


    public static class TwoComboBoxesSelectorBuilder<T, V, E extends SelectorElementTwoChoice<T, V>> extends SelectorBuilderType<T, E> {

        public TwoComboBoxesSelectorBuilder(Collection<T> mainCollection, Collection<V> secondCollection, Supplier<E> creator) {
            super(mainCollection, creator);
            addOption(n -> n.setSecondCollection(secondCollection));
        }

        @Override
        public final TwoComboBoxesSelectorBuilder<T, V, E> nameBy(Function<T, String> function) {
            selectorContainer.setSearchDialogNameBy(function);
            selectorContainer.addOption(s -> s.setNameBy(function));
            return this;
        }

        @Override
        public final TwoComboBoxesSelectorBuilder<T, V, E> nameByProperty(Function<T, ObservableValue<String>> function) {
            selectorContainer.setSearchDialogNameByProperty(function);
            selectorContainer.addOption(s -> s.setNameByProperty(function));
            return this;
        }

        @SafeVarargs
        @Override
        public final TwoComboBoxesSelectorBuilder<T, V, E> searchBy(Function<T, String> function, Function<T, String>... functions) {
            selectorContainer.setSearchDialogSearchBy(function, functions);
            selectorContainer.addOption(s -> s.setSearchBy(function, functions));
            return this;
        }

        @SafeVarargs
        @Override
        public final TwoComboBoxesSelectorBuilder<T, V, E> searchByProperty(Function<T, ObservableValue<String>> function, Function<T, ObservableValue<String>>... functions) {
            selectorContainer.setSearchDialogSearchByProperty(function, functions);
            selectorContainer.addOption(s -> s.setSearchByProperty(function, functions));
            return this;
        }

        public final TwoComboBoxesSelectorBuilder<T, V, E> disableSearch() {
            selectorContainer.addOption(s -> s.setSearchEnabled(false));
            return this;
        }

        public final TwoComboBoxesSelectorBuilder<T, V, E> sizePercentages(double comboBox, double textField) {
            selectorContainer.addOption(s -> s.setSizePercentages(comboBox, textField));
            return this;
        }

        public final TwoComboBoxesSelectorBuilder<T, V, E> secondBoxNameBy(Function<V, String> function) {
            selectorContainer.addOption(s -> s.setSecondNameBy(function));
            return this;
        }

        public final TwoComboBoxesSelectorBuilder<T, V, E> secondBoxNameByProperty(Function<V, ObservableValue<String>> function) {
            selectorContainer.addOption(s -> s.setSecondNameByProperty(function));
            return this;
        }

        @SafeVarargs
        public final TwoComboBoxesSelectorBuilder<T, V, E> secondBoxSearchBy(Function<V, String> function, Function<V, String>... functions) {
            selectorContainer.addOption(s -> s.setSecondSearchBy(function, functions));
            return this;
        }

        @SafeVarargs
        public final TwoComboBoxesSelectorBuilder<T, V, E> secondBoxSearchByProperty(Function<V, ObservableValue<String>> function, Function<V, ObservableValue<String>>... functions) {
            selectorContainer.addOption(s -> s.setSecondSearchByProperty(function, functions));
            return this;
        }
    }

    ////////////////////////////////////////////////////
    //                                                //
    //              WITH PROPERTIES WINDOW            //
    //                                                //
    ////////////////////////////////////////////////////

    public static class WithPropertiesWindowSelectorBuilder<T, P extends Node, E extends SelectorElementWindowProperties<T, P>> extends SelectorBuilderType<T, E> {

        public WithPropertiesWindowSelectorBuilder(Collection<T> mainCollection, Supplier<P> propertiesWindowCreator, Function<P, E> creator) {
            super(mainCollection, () -> creator.apply(propertiesWindowCreator.get()));
        }

        @Override
        public final WithPropertiesWindowSelectorBuilder<T, P, E> nameBy(Function<T, String> function) {
            selectorContainer.setSearchDialogNameBy(function);
            selectorContainer.addOption(s -> s.setNameBy(function));
            return this;
        }

        @Override
        public final WithPropertiesWindowSelectorBuilder<T, P, E> nameByProperty(Function<T, ObservableValue<String>> function) {
            selectorContainer.setSearchDialogNameByProperty(function);
            selectorContainer.addOption(s -> s.setNameByProperty(function));
            return this;
        }

        @SafeVarargs
        @Override
        public final WithPropertiesWindowSelectorBuilder<T, P, E> searchBy(Function<T, String> function, Function<T, String>... functions) {
            selectorContainer.setSearchDialogSearchBy(function, functions);
            selectorContainer.addOption(s -> s.setSearchBy(function, functions));
            return this;
        }

        @SafeVarargs
        @Override
        public final WithPropertiesWindowSelectorBuilder<T, P, E> searchByProperty(Function<T, ObservableValue<String>> function, Function<T, ObservableValue<String>>... functions) {
            selectorContainer.setSearchDialogSearchByProperty(function, functions);
            selectorContainer.addOption(s -> s.setSearchByProperty(function, functions));
            return this;
        }

        public final WithPropertiesWindowSelectorBuilder<T, P, E> disableSearch() {
            selectorContainer.addOption(s -> s.setSearchEnabled(false));
            return this;
        }

        public final WithPropertiesWindowSelectorBuilder<T, P, E> stageStile(StageStyle stageStyle) {
            selectorContainer.addOption(s -> s.getPropertiesWindow().initStyle(stageStyle));
            return this;
        }

        public final WithPropertiesWindowSelectorBuilder<T, P, E> stageOptions(Consumer<SelectorElementWindowProperties<?, ?>.PropertiesWindow> customize) {
            selectorContainer.addOption(s -> customize.accept(s.getPropertiesWindow()));
            return this;
        }

        public final WithPropertiesWindowSelectorBuilder<T, P, E> propertiesButtonOptions(Consumer<Button> customize) {
            selectorContainer.addOption(s -> customize.accept(s.getButton()));
            return this;
        }

    }

}
