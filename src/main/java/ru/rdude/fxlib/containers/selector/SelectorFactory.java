package ru.rdude.fxlib.containers.selector;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Function;

final class SelectorFactory {

    public static <T, V> SelectorContainer<T, SelectorElementAutocompletionTextField<T, V>> withAutocompletionTextField(
            @NotNull Collection<T> collection,
            @NotNull Collection<V> autocompletionCollection) {

        return withAutocompletionTextField(collection, autocompletionCollection, null, null);
    }


    @SafeVarargs
    public static <T, V> SelectorContainer<T, SelectorElementAutocompletionTextField<T, V>> withAutocompletionTextField(
            @NotNull Collection<T> collection,
            @NotNull Collection<V> autocompletionCollection,
            Function<T, String> nameFunction,
            Function<V, String> autocompletionNameFunction,
            @NotNull Function<T, String>... searchFunctions) {

        SelectorContainer<T, SelectorElementAutocompletionTextField<T, V>> res = new SelectorContainer<>(collection, SelectorElementAutocompletionTextField::new);
        res.addOption(e -> e.setTextFieldElements(autocompletionCollection));
        boolean emptySearchFunctions = searchFunctions == null || searchFunctions.length == 0;
        if (nameFunction != null) {
            res.getSearchDialog().getSearchPane().setNameBy(nameFunction);
            res.addOption(e -> e.setNameBy(nameFunction));
            if (emptySearchFunctions) {
                res.addOption(e -> e.setSearchBy(nameFunction));
                res.getSearchDialog().getSearchPane().setTextFieldSearchBy(nameFunction);
            }
        }
        if (!emptySearchFunctions) {
            res.getSearchDialog().getSearchPane().setTextFieldSearchBy(Arrays.asList(searchFunctions));
            res.addOption(e -> e.setSearchBy(Arrays.asList(searchFunctions)));
        }
        if (autocompletionNameFunction != null) {
            res.addOption(e -> e.setTextFieldNameBy(autocompletionNameFunction));
        }
        return res;
    }


    public static <T> SelectorContainer<T, SelectorElementPercent<T>> withPercents(@NotNull Collection<T> collection) {
        return withPercents(collection, null);
    }


    @SafeVarargs
    public static <T> SelectorContainer<T, SelectorElementPercent<T>> withPercents(
            @NotNull Collection<T> collection,
            Function<T, String> nameFunction,
            @NotNull Function<T, String>... searchFunctions) {

        SelectorContainer<T, SelectorElementPercent<T>> res = new SelectorContainer<>(collection, SelectorElementPercent::new);
        boolean emptySearchFunctions = searchFunctions == null || searchFunctions.length == 0;
        if (nameFunction != null) {
            res.getSearchDialog().getSearchPane().setNameBy(nameFunction);
            res.addOption(e -> e.setNameBy(nameFunction));
            if (emptySearchFunctions) {
                res.addOption(e -> e.setSearchBy(nameFunction));
                res.getSearchDialog().getSearchPane().setTextFieldSearchBy(nameFunction);
            }
        }
        if (!emptySearchFunctions) {
            res.getSearchDialog().getSearchPane().setTextFieldSearchBy(Arrays.asList(searchFunctions));
            res.addOption(e -> e.setSearchBy(Arrays.asList(searchFunctions)));
        }
        return res;
    }


    public static <T> SelectorContainer<T, SelectorElementTextField<T>> withTextField(@NotNull Collection<T> collection) {
        return withTextField(collection, null);
    }


    @SafeVarargs
    public static <T> SelectorContainer<T, SelectorElementTextField<T>> withTextField(
            @NotNull Collection<T> collection,
            Function<T, String> nameFunction,
            @NotNull Function<T, String>... searchFunctions) {

        SelectorContainer<T, SelectorElementTextField<T>> res = new SelectorContainer<>(collection, SelectorElementTextField::new);
        boolean emptySearchFunctions = searchFunctions == null || searchFunctions.length == 0;
        if (nameFunction != null) {
            res.getSearchDialog().getSearchPane().setNameBy(nameFunction);
            res.addOption(e -> e.setNameBy(nameFunction));
            if (emptySearchFunctions) {
                res.addOption(e -> e.setSearchBy(nameFunction));
                res.getSearchDialog().getSearchPane().setTextFieldSearchBy(nameFunction);
            }
        }
        if (!emptySearchFunctions) {
            res.getSearchDialog().getSearchPane().setTextFieldSearchBy(Arrays.asList(searchFunctions));
            res.addOption(e -> e.setSearchBy(Arrays.asList(searchFunctions)));
        }
        return res;
    }


    public static <T, V> SelectorContainer<T, SelectorElementTwoChoice<T, V>> withTwoComboBoxes(
            @NotNull Collection<T> mainCollection,
            @NotNull Collection<V> secondCollection) {
        return withTwoComboBoxes(mainCollection, secondCollection, null, null, null, null);
    }


    public static <T, V> SelectorContainer<T, SelectorElementTwoChoice<T, V>> withTwoComboBoxes(
            @NotNull Collection<T> mainCollection,
            @NotNull Collection<V> secondCollection,
            Function<T, String> mainNameFunction,
            Collection<Function<T, String>> mainSearchFunctions,
            Function<V, String> secondNameFunction,
            Collection<Function<V, String>> secondSearchFunctions) {

        SelectorContainer<T, SelectorElementTwoChoice<T, V>> res = new SelectorContainer<>(mainCollection, SelectorElementTwoChoice::new);
        boolean emptyMainSearchFunctions = mainSearchFunctions == null || mainSearchFunctions.size() < 1;
        boolean emptySecondSearchFunctions = secondSearchFunctions == null || secondSearchFunctions.size() < 1;

        // main
        if (mainNameFunction != null) {
            res.getSearchDialog().getSearchPane().setNameBy(mainNameFunction);
            res.addOption(e -> e.setNameBy(mainNameFunction));
            if (emptyMainSearchFunctions) {
                res.addOption(e -> e.setSearchBy(mainNameFunction));
                res.getSearchDialog().getSearchPane().setTextFieldSearchBy(mainNameFunction);
            }
        }
        if (!emptyMainSearchFunctions) {
            res.getSearchDialog().getSearchPane().setTextFieldSearchBy(mainSearchFunctions);
            res.addOption(e -> e.setSearchBy(mainSearchFunctions));
        }

        // second
        res.addOption(e -> e.setSecondCollection(secondCollection));
        if (secondNameFunction != null) {
            res.addOption(e -> e.setSecondNameBy(secondNameFunction));
            if (emptySecondSearchFunctions) {
                res.addOption(e -> e.setSecondSearchBy(secondNameFunction));
            }
        }
        if (!emptyMainSearchFunctions) {
            res.addOption(e -> e.setSecondSearchBy(secondSearchFunctions));
        }
        return res;
    }
}
