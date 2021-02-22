package ru.rdude.fxlib.containers.selector;

import javafx.application.Platform;

public class SelectorElementPercent<T> extends SelectorElementTextField<T> {

    public SelectorElementPercent() {
        super();
        getColumnConstraints().get(0).setPercentWidth(70);
        getColumnConstraints().get(1).setPercentWidth(30);
        getTextField().textProperty().setValue("100 %");
        getTextField().textProperty().addListener((observableValue, oldV, newV) -> {
            if (!newV.matches("\\d* %")) {
                getTextField().textProperty().setValue(oldV);
            }
        });
        getTextField().caretPositionProperty().addListener((observableValue, oldV, newV) -> {
            int max = getTextField().getText().length() - 2;
            if (newV.intValue() > max && getTextField().selectedTextProperty().get().isEmpty()) {
                Platform.runLater(() -> {
                    getTextField().positionCaret(max);
                });
            }
        });
    }

    public void setPercents(double percents) {
        getTextField().textProperty().setValue(String.valueOf(percents).replaceAll("\\.0+", "") + " %");
    }

    public void setPercentsAsCoefficient(double coefficient) {
        setPercents(coefficient / 100);
    }

    public double getPercents() {
        String s = getTextField().getText().replaceAll(" %", "");
        return s.isEmpty() ? 0d : Double.parseDouble(s);
    }

    public double getCoefficient() {
        return getPercents() * 100;
    }
}
