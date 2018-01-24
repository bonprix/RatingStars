package org.vaadin.teemu.ratingstars.gwt.client;

import java.util.Map;

import com.google.gwt.animation.client.Animation;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.HasAnimation;
import com.google.gwt.user.client.ui.HasValue;

/**
 * RatingStarsWidget is the client-side implementation of the RatingStars component.
 *
 * The DOM tree for this component is constructed as follows:
 *
 * <pre>
 *    div.v-ratingstars-wrapper
 *        div.v-ratingstars
 *            div.v-ratingstars-star
 *            div.v-ratingstars-star
 *            div.v-ratingstars-star
 *            ...
 *            div.v-ratingstars-star
 *            div.v-ratingstars-bar
 * </pre>
 *
 * The idea behind the DOM tree is that {@code .v-ratingstars-star} elements always have a partially transparent background image and the width of the
 * {@code .v-ratingstars-bar} element behind these star elements is changed according to the current value.
 *
 * @author Teemu Pöntelin
 */
public class RatingStarsWidget extends FocusWidget implements HasAnimation, HasValue<Double>, HasValueChangeHandlers<Double> {

    /** Set the CSS class names to allow styling. */
    public static final String CLASSNAME = "v-ratingstars";
    public static final String STAR_CLASSNAME = RatingStarsWidget.CLASSNAME + "-star";
    public static final String BAR_CLASSNAME = RatingStarsWidget.CLASSNAME + "-bar";
    public static final String WRAPPER_CLASSNAME = RatingStarsWidget.CLASSNAME + "-wrapper";

    private static final int ANIMATION_DURATION_IN_MS = 150;

    // DOM elements
    private Element barDiv;
    private Element element;
    private Element[] starElements;

    /** Currently focused star (by keyboard focus). */
    private int focusIndex = -1;

    private int maxValue = 5;
    private double value;

    private boolean animated;
    private boolean readonly;

    public RatingStarsWidget() {
        setElement(Document.get()
            .createDivElement());
        setStyleName(RatingStarsWidget.WRAPPER_CLASSNAME);
        initDom();
    }

    private void initDom() {
        if (this.element != null) {
            // Remove previous element.
            getElement().removeChild(this.element);
        }

        this.element = Document.get()
            .createDivElement();
        this.element.setClassName(RatingStarsWidget.CLASSNAME);
        getElement().appendChild(this.element);

        createStarElements();

        this.barDiv = createBarDiv();
        this.element.appendChild(this.barDiv);

        DOM.sinkEvents(getElement(), Event.ONCLICK | Event.ONMOUSEOVER | Event.ONMOUSEOUT | Event.ONFOCUS | Event.ONBLUR | Event.ONKEYUP);
    }

    void updateValueCaptions(final Map<Integer, String> valueCaptions) {
        for (final Element starElement : this.starElements) {
            final int rating = starElement.getPropertyInt("rating");
            final String caption = valueCaptions.get(rating);
            if (caption != null) {
                starElement.setPropertyString("caption", caption);

                if (StarCaptionUtil.isVisibleForStarElement(starElement)) {
                    // update currently visible caption
                    StarCaptionUtil.showAroundElement(starElement, caption);
                }
            }
        }
    }

    private void createStarElements() {
        this.starElements = new Element[this.maxValue];
        for (int i = 0; i < this.maxValue; i++) {
            final DivElement starDiv = createStarDiv(i + 1);
            this.starElements[i] = starDiv;
            this.element.appendChild(starDiv);
        }
    }

    @Override
    public void onBrowserEvent(final Event event) {
        if (!isEnabled() || this.readonly) {
            return; // Do nothing if disabled or read-only.
        }

        super.onBrowserEvent(event);

        final Element target = Element.as(event.getEventTarget());
        switch (DOM.eventGetType(event)) {
            case Event.ONCLICK:
                // update value
                setValueFromElement(target);
                break;
            case Event.ONMOUSEOVER:
                // animate
                if (target.getClassName()
                    .contains(RatingStarsWidget.STAR_CLASSNAME)) {
                    final int rating = target.getPropertyInt("rating");
                    setFocusIndex(rating - 1);
                    setFocus(true);
                    StarCaptionUtil.showAroundElement(target, target.getPropertyString("caption"));
                }
                break;
            case Event.ONMOUSEOUT:
                setBarWidth(calcBarWidth(this.value));
                setFocusIndex(-1);
                StarCaptionUtil.hide();
                break;
            case Event.ONFOCUS:
                getElement().addClassName(RatingStarsWidget.WRAPPER_CLASSNAME + "-focus");
                if (this.focusIndex < 0) {
                    if (Math.round(this.value) > 0) {
                        // focus the current value (or the closest int)
                        setFocusIndex((int) (Math.round(this.value) - 1));
                    }
                    else {
                        // focus the first value
                        setFocusIndex(0);
                    }
                }
                break;
            case Event.ONBLUR:
                getElement().removeClassName(RatingStarsWidget.WRAPPER_CLASSNAME + "-focus");
                setFocusIndex(-1);
                setBarWidth(calcBarWidth(this.value));
                StarCaptionUtil.hide();
                break;
            case Event.ONKEYUP:
                handleKeyUp(event);
                break;
        }
    }

    private void setFocusIndex(final int index) {
        // remove old focus class
        if (this.focusIndex >= 0 && this.focusIndex < this.starElements.length) {
            this.starElements[this.focusIndex].removeClassName(RatingStarsWidget.STAR_CLASSNAME + "-focus");
        }
        // update focusIndex and add class
        this.focusIndex = index;
        if (this.focusIndex >= 0 && this.focusIndex < this.starElements.length) {
            final Element focusedStar = this.starElements[this.focusIndex];

            focusedStar.addClassName(RatingStarsWidget.STAR_CLASSNAME + "-focus");
            setBarWidth(calcBarWidth(focusedStar.getPropertyInt("rating")));
            StarCaptionUtil.showAroundElement(focusedStar, focusedStar.getPropertyString("caption"));
        }
    }

    private void changeFocusIndex(final int delta) {
        final int newFocusIndex = this.focusIndex + delta;

        // check for boundaries
        if (newFocusIndex >= 0 && newFocusIndex < this.starElements.length) {
            setFocusIndex(newFocusIndex);
        }
    }

    private void setValueFromElement(final Element target) {
        if (target.getClassName()
            .contains(RatingStarsWidget.STAR_CLASSNAME)) {
            final int ratingValue = target.getPropertyInt("rating");
            setValue((double) ratingValue, true);
        }
    }

    public void handleKeyUp(final Event event) {
        if (event.getKeyCode() == KeyCodes.KEY_RIGHT) {
            changeFocusIndex(+1);
        }
        else if (event.getKeyCode() == KeyCodes.KEY_LEFT) {
            changeFocusIndex(-1);
        }
        else if (event.getKeyCode() == KeyCodes.KEY_ENTER) {
            setValueFromElement(this.starElements[this.focusIndex]);
        }
    }

    /**
     * Creates the DivElement of the bar representing the current value.
     *
     * @return the newly created DivElement representing the bar.
     * @see #setBarWidth(byte)
     */
    private Element createBarDiv() {
        final DivElement barDiv = Document.get()
            .createDivElement();
        barDiv.setClassName(RatingStarsWidget.BAR_CLASSNAME);
        barDiv.getStyle()
            .setProperty("width", calcBarWidth(this.value) + "%");
        return barDiv;
    }

    /**
     * Sets the width of the bar div instantly or via animated progress depending on the value of the <code>animated</code> property.
     */
    private void setBarWidth(final byte widthPercentage) {
        if (this.barDiv == null) {
            return;
        }

        final byte currentWidthPercentage = getCurrentBarWidth();
        if (currentWidthPercentage != widthPercentage) {
            if (!isAnimationEnabled()) {
                this.barDiv.getStyle()
                    .setProperty("width", widthPercentage + "%");
            }
            else {
                final Animation animation = new Animation() {
                    @Override
                    protected void onUpdate(final double progress) {
                        final byte newWidth = (byte) (currentWidthPercentage + (progress * (widthPercentage - currentWidthPercentage)));
                        RatingStarsWidget.this.barDiv.getStyle()
                            .setProperty("width", newWidth + "%");
                    }
                };
                animation.run(RatingStarsWidget.ANIMATION_DURATION_IN_MS);
            }
        }
    }

    private byte getCurrentBarWidth() {
        final String currentWidth = this.barDiv.getStyle()
            .getProperty("width");
        byte currentWidthPercentage = 0;
        if (currentWidth != null && currentWidth.length() > 0) {
            currentWidthPercentage = Byte.valueOf(currentWidth.substring(0, currentWidth.length() - 1));
        }
        return currentWidthPercentage;
    }

    /**
     * Calculates the bar width for the given <code>forValue</code> as a percentage of the <code>maxValue</code>. Returned value is from 0 to 100.
     *
     * @return width percentage (0..100)
     */
    private byte calcBarWidth(final double forValue) {
        return (byte) (forValue * 100 / this.maxValue);
    }

    /**
     * Creates a DivElement representing a single star. Given <code>rating</code> value is set as an int property for the div.
     *
     * @param rating rating value of this star.
     * @return a DivElement representing a single star.
     */
    private DivElement createStarDiv(final int rating) {
        final DivElement starDiv = Document.get()
            .createDivElement();
        starDiv.setClassName(RatingStarsWidget.STAR_CLASSNAME);
        starDiv.setPropertyInt("rating", rating);
        return starDiv;
    }

    @Override
    public boolean isAnimationEnabled() {
        return this.animated;
    }

    @Override
    public void setAnimationEnabled(final boolean enable) {
        this.animated = enable;
    }

    public void setMaxValue(final int maxValue) {
        if (this.maxValue != maxValue) {
            this.maxValue = maxValue;
            initDom(); // Recreate the DOM.
        }
    }

    private void internalSetValue(final double value) {
        this.value = value;
        setBarWidth(calcBarWidth(this.value));
    }

    public void setReadOnly(final boolean readonly) {
        if (this.readonly != readonly) {
            this.readonly = readonly;
        }
    }

    @Override
    public HandlerRegistration addValueChangeHandler(final ValueChangeHandler<Double> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }

    @Override
    public Double getValue() {
        return this.value;
    }

    @Override
    public void setValue(final Double value) {
        setValue(value, false);
        internalSetValue(value);
    }

    @Override
    public void setValue(Double value, final boolean fireEvents) {
        // Null not supported -> convert to zero.
        if (value == null) {
            value = 0.0;
        }

        // if the selected Star will be klicked again, the selection will be set to 0 (no stars selected).
        if (value.equals(this.value)) {
            value = 0.0;
        }
        if (fireEvents)
            ValueChangeEvent.fireIfNotEqual(this, this.value, value);
    }
}
