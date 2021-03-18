package utils;

import javafx.beans.value.ObservableValue;

import java.util.function.Function;

public class FunctionRawOrProperty<T, P> implements Function<T, P> {

    private Function<T, P> raw;
    private Function<T, ObservableValue<P>> property;

    public static <T, P> FunctionRawOrProperty<T, P> raw(Function<T, P> function) {
        FunctionRawOrProperty<T, P> res = new FunctionRawOrProperty<>();
        res.setRaw(function);
        return res;
    }

    public static <T, P> FunctionRawOrProperty<T, P> property(Function<T, ObservableValue<P>> function) {
        FunctionRawOrProperty<T, P> res = new FunctionRawOrProperty<>();
        res.setProperty(function);
        return res;
    }

    private FunctionRawOrProperty() { }

    public void setRaw(Function<T, P> raw) {
        this.raw = raw;
        this.property = null;
    }

    public void setProperty(Function<T, ObservableValue<P>> property) {
        this.property = property;
        this.raw = null;
    }

    public boolean isEmpty() {
        return raw == null && property == null;
    }

    public boolean isNotEmpty() {
        return !isEmpty();
    }

    public P apply(T t) {
        if (isEmpty()) return null;
        else return raw != null ? raw.apply(t) : property.apply(t).getValue();
    }

}
