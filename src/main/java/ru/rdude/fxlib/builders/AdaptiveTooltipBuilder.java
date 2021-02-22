package ru.rdude.fxlib.builders;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Skin;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class AdaptiveTooltipBuilder<E, C extends Node> {

    private final Tooltip tooltip = new Tooltip();
    private final VBox tooltipGraphic = new VBox();
    private final ObservableList<C> controls;
    private final Set<InsideNode<?>> insideNodes = new HashSet<>();
    private final Function<C, E> controlToElement;

    public static <E, C extends Node> AdaptiveTooltipBuilder<E, C> createFor(Collection<C> tooltipHolders, Function<C, E> holderToElement) {
        return new AdaptiveTooltipBuilder<>(tooltipHolders, holderToElement);
    }

    private AdaptiveTooltipBuilder(Collection<C> controls, Function<C, E> controlToElement) {
        if (controls instanceof ObservableList) {
            this.controls = (ObservableList<C>) controls;
            ((ObservableList<C>) controls).addListener((ListChangeListener<C>) change -> {
                while (change.next()) {
                    if (change.wasAdded()) {
                        change.getAddedSubList().forEach(c -> Tooltip.install(c, tooltip));
                    } else if (change.wasRemoved()) {
                        change.getRemoved().forEach(c -> Tooltip.uninstall(c, tooltip));
                    }
                }
            });
        } else {
            this.controls = FXCollections.observableArrayList(controls);
        }
        this.controlToElement = controlToElement;

        tooltipGraphic.setAlignment(Pos.CENTER);
        tooltip.setGraphic(tooltipGraphic);
        tooltip.setOnShowing(windowEvent -> {
            final C source = (C) windowEvent.getSource();
            insideNodes.forEach(n -> n.apply(controlToElement.apply(source)));
        });

        controls.forEach(c -> Tooltip.install(c, tooltip));
    }

    public <N extends Node> AdaptiveTooltipNodePropertiesBuilder<N> addNode(N node) {
        InsideNode<N> insideNode = new InsideNode<>(node);
        tooltipGraphic.getChildren().add(insideNode.node);
        return new AdaptiveTooltipNodePropertiesBuilder<>(insideNode);
    }

    public AdaptiveTooltipBuilder<E, C> addStaticText(String text) {
        tooltipGraphic.getChildren().add(new Label(text));
        return this;
    }

    public AdaptiveTooltipBuilder<E, C> addText(Function<E, String> getter) {
        InsideNode<Label> insideNode = new InsideNode<>(new Label());
        insideNode.properties.add(new Property<>(Label::setText, getter));
        tooltipGraphic.getChildren().add(insideNode.node);
        return this;
    }

    public AdaptiveTooltipBuilder<E, C> addStaticImage(Image image) {
        tooltipGraphic.getChildren().add(new ImageView(image));
        return this;
    }

    public AdaptiveTooltipBuilder<E, C> addStaticImage(Image image, double fitWidth, double fitHeight) {
        final ImageView imageView = new ImageView(image);
        imageView.setFitWidth(fitWidth);
        imageView.setFitHeight(fitHeight);
        tooltipGraphic.getChildren().add(imageView);
        return this;
    }

    public TooltipOptions tooltipOptions() {
        return new TooltipOptions();
    }

    private class InsideNode<N extends Node> {
        N node;
        Set<Property<N, ?>> properties = new HashSet<>();

        InsideNode(N node) {
            this.node = node;
        }

        void apply(E e) {
            properties.forEach(prop -> prop.apply(node, e));
        }
    }

    private class Property<N extends Node, T> {
        BiConsumer<N, T> setter;
        Function<E, T> getter;

        public Property(BiConsumer<N, T> setter, Function<E, T> getter) {
            this.setter = setter;
            this.getter = getter;
        }

        void apply(N n, E e) {
            setter.accept(n, getter.apply(e));
        }
    }

    private abstract class SubOptions {
        public <N2 extends Node> AdaptiveTooltipNodePropertiesBuilder<N2> addNode(N2 node) {
            return AdaptiveTooltipBuilder.this.addNode(node);
        }

        public AdaptiveTooltipBuilder<E, C> addStaticText(String text) {
            return AdaptiveTooltipBuilder.this.addStaticText(text);
        }

        public AdaptiveTooltipBuilder<E, C> addStaticImage(Image image) {
            return AdaptiveTooltipBuilder.this.addStaticImage(image);
        }

        public AdaptiveTooltipBuilder<E, C> addStaticImage(Image image, double fitWidth, double fitHeight) {
            return AdaptiveTooltipBuilder.this.addStaticImage(image, fitWidth, fitHeight);
        }

        public AdaptiveTooltipBuilder<E, C> addText(Function<E, String> getter) {
            return AdaptiveTooltipBuilder.this.addText(getter);
        }
    }

    public class AdaptiveTooltipNodePropertiesBuilder<N extends Node> extends SubOptions {
        InsideNode<N> insideNode;

        AdaptiveTooltipNodePropertiesBuilder(InsideNode<N> insideNode) {
            this.insideNode = insideNode;
        }

        public <T> AdaptiveTooltipNodePropertiesBuilder<N> withProperty(BiConsumer<N, T> setter, Function<E, T> getter) {
            insideNode.properties.add(new Property<>(setter, getter));
            return this;
        }
    }

    public class TooltipOptions extends SubOptions {

        private TooltipOptions() {
        }

        public Tooltip getTooltip() {
            return tooltip;
        }

        public VBox getInsideVbox() {
            return tooltipGraphic;
        }

        public TooltipOptions setShowDelay(Duration duration) {
            tooltip.setShowDelay(duration);
            return this;
        }

        public TooltipOptions setShowDuration(Duration duration) {
            tooltip.setShowDuration(duration);
            return this;
        }

        public TooltipOptions setHideDelay(Duration duration) {
            tooltip.setHideDelay(duration);
            return this;
        }

        public TooltipOptions setStyle(String s) {
            tooltip.setStyle(s);
            return this;
        }

        public TooltipOptions setSkin(Skin<?> skin) {
            tooltip.setSkin(skin);
            return this;
        }

        public TooltipOptions setMinWidth(double v) {
            tooltip.setMinWidth(v);
            return this;
        }

        public TooltipOptions setMinHeight(double v) {
            tooltip.setMinHeight(v);
            return this;
        }

        public TooltipOptions setMinSize(double v, double v1) {
            tooltip.setMinSize(v, v1);
            return this;
        }

        public TooltipOptions setPrefWidth(double v) {
            tooltip.setPrefWidth(v);
            return this;
        }

        public TooltipOptions setPrefHeight(double v) {
            tooltip.setPrefHeight(v);
            return this;
        }

        public TooltipOptions setPrefSize(double v, double v1) {
            tooltip.setPrefSize(v, v1);
            return this;
        }

        public TooltipOptions setMaxWidth(double v) {
            tooltip.setMaxWidth(v);
            return this;
        }

        public TooltipOptions setMaxHeight(double v) {
            tooltip.setMaxHeight(v);
            return this;
        }

        public TooltipOptions setMaxSize(double v, double v1) {
            tooltip.setMaxSize(v, v1);
            return this;
        }

        public TooltipOptions setAutoHide(boolean b) {
            tooltip.setAutoHide(b);
            return this;
        }

        public void setSpacing(double v) {
            tooltipGraphic.setSpacing(v);
        }

        public void setAlignment(Pos pos) {
            tooltipGraphic.setAlignment(pos);
        }
    }
}
