package org.vaadin.teemu.ratingstars.demo;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.servlet.annotation.WebServlet;

import org.vaadin.teemu.ratingstars.RatingStars;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.data.Item;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

/**
 * A demo application for the RatingStars component. For a live demo see
 * <a href="http://teemu.virtuallypreinstalled.com/RatingStars">http://teemu.virtuallypreinstalled.com/RatingStars</a>
 *
 * @author Teemu Pöntelin
 */
@Theme("valo")
@Title("RatingStars Component Demo")
@SuppressWarnings("unused")
public class RatingStarsDemo extends UI {
    private static final long serialVersionUID = 7705972095201251401L;

    private Table table;
    private CheckBox animatedCheckBox;

    @WebServlet(
        urlPatterns = "/*",
        name = "MyUIServlet",
        asyncSupported = true)
    @VaadinServletConfiguration(
        ui = RatingStarsDemo.class,
        productionMode = false,
        widgetset = "org.vaadin.teemu.ratingstars.demo.DemoWidgetSet")
    public static class MyUIServlet extends VaadinServlet {
    }

    private final static Map<Integer, String> valueCaptions = new HashMap<>(5, 1);

    static {
        RatingStarsDemo.valueCaptions.put(1, "Epic Fail");
        RatingStarsDemo.valueCaptions.put(2, "Poor");
        RatingStarsDemo.valueCaptions.put(3, "OK");
        RatingStarsDemo.valueCaptions.put(4, "Good");
        RatingStarsDemo.valueCaptions.put(5, "Excellent");
    }

    private static final String[] movieNames = { "The Matrix", "Memento", "Kill Bill: Vol. 1" };

    private final Set<RatingStars> allRatingStars = new HashSet<>();

    private VerticalLayout mainLayout;

    @Override
    protected void init(final VaadinRequest request) {
        initWindowAndDescription();
        initDemoPanel();
    }

    private void initWindowAndDescription() {
        final VerticalLayout centerLayout = new VerticalLayout();

        this.mainLayout = new VerticalLayout();
        final Panel mainPanel = new Panel(this.mainLayout);
        mainPanel.setWidth("750px");
        centerLayout.addComponent(mainPanel);
        centerLayout.setComponentAlignment(mainPanel, Alignment.TOP_CENTER);
        setContent(centerLayout);

        final StringBuilder descriptionXhtml = new StringBuilder();
        descriptionXhtml.append("<h1 style=\"margin: 0;\">RatingStars Component Demo</h1>");
        descriptionXhtml.append("<p>RatingStars is a simple component for giving rating values.</p>");
        descriptionXhtml.append("<p>Download and rate this component at <a href=\"http://vaadin.com/addon/ratingstars\">Vaadin Directory</a>. ");
        descriptionXhtml.append("Get the source code at <a href=\"https://github.com/tehapo/RatingStars\">GitHub</a>.</p>");
        descriptionXhtml.append("<p>Highlights:</p>");
        descriptionXhtml.append("<ul>");
        descriptionXhtml.append("<li>Keyboard usage (focus with tab, navigate with arrow keys, select with enter)</li>");
        descriptionXhtml.append("<li>Easily customizable appearance</li>");
        descriptionXhtml.append("<li>Captions for individual values</li>");
        descriptionXhtml.append("<li>Optional transition animations</li>");
        descriptionXhtml.append("</ul>");
        descriptionXhtml.append("<div style=\"height: 10px\"></div>");

        final Label description = new Label(descriptionXhtml.toString(), ContentMode.HTML);
        this.mainLayout.addComponent(description);
    }

    private void initDemoPanel() {
        final Panel demoPanel = new Panel("Demonstration");
        final VerticalLayout demoLayout = new VerticalLayout();
        demoLayout.setSpacing(true);
        demoLayout.setMargin(true);
        demoPanel.setContent(demoLayout);
        this.mainLayout.addComponent(demoPanel);

        // animated checkbox
        this.animatedCheckBox = new CheckBox("Animated?");
        this.animatedCheckBox.setValue(true);
        this.animatedCheckBox.setImmediate(true);
        this.animatedCheckBox.addValueChangeListener(new ValueChangeListener() {

            private static final long serialVersionUID = 6001160591512323325L;

            @Override
            public void valueChange(final ValueChangeEvent event) {
                for (final RatingStars rs : RatingStarsDemo.this.allRatingStars) {
                    rs.setAnimated((Boolean) event.getProperty()
                        .getValue());
                }
            }
        });
        demoLayout.addComponent(this.animatedCheckBox);

        // create and populate the movie table
        this.table = new Table("Rate your favourite movies");
        this.table.addContainerProperty("Movie", String.class, null);
        this.table.addContainerProperty("Comment", TextField.class, null);
        this.table.addContainerProperty("Your rating", RatingStars.class, null);
        this.table.addContainerProperty("Average rating", RatingStars.class, null);
        populateTable();
        this.table.setPageLength(this.table.getItemIds()
            .size());
        demoLayout.addComponent(this.table);

        // theme demos
        demoLayout.addComponent(new Label("<strong>The component has two built-in styles.</strong>", ContentMode.HTML));
        final RatingStars defaultRs = new RatingStars();
        defaultRs.setDescription("Default RatingStars");
        defaultRs.setCaption("default");

        this.allRatingStars.add(defaultRs);

        final RatingStars tinyRs = new RatingStars();
        tinyRs.setMaxValue(3);
        tinyRs.setStyleName("tiny");
        tinyRs.setCaption("tiny");
        this.allRatingStars.add(tinyRs);

        final HorizontalLayout themeLayout = new HorizontalLayout();
        themeLayout.setSpacing(true);
        themeLayout.addComponent(defaultRs);
        themeLayout.addComponent(tinyRs);
        demoLayout.addComponent(themeLayout);

        // component states
        demoLayout.addComponent(new Label("<strong>Component states</strong>", ContentMode.HTML));
        final RatingStars disabledRs = new RatingStars();
        disabledRs.setCaption("disabled");
        disabledRs.setValue(2.5);
        disabledRs.setEnabled(false);

        final RatingStars readonlyRs = new RatingStars();
        readonlyRs.setCaption("read-only");
        readonlyRs.setValue(2.5);
        readonlyRs.setReadOnly(true);

        final HorizontalLayout stateLayout = new HorizontalLayout();
        stateLayout.setSpacing(true);
        stateLayout.addComponent(disabledRs);
        stateLayout.addComponent(readonlyRs);
        demoLayout.addComponent(stateLayout);
    }

    /**
     * Populate the table with some random data.
     */
    @SuppressWarnings("unchecked")
    private void populateTable() {
        final Random r = new Random();
        for (final String movieName : RatingStarsDemo.movieNames) {
            final TextField textField = new TextField();

            final RatingStars avgRs = new RatingStars();
            avgRs.setMaxValue(5);
            avgRs.setValue(r.nextDouble() * 4 + 1);
            avgRs.setReadOnly(true);
            this.allRatingStars.add(avgRs);

            final RatingStars yourRs = new RatingStars();
            yourRs.setMaxValue(5);
            yourRs.setImmediate(true);
            yourRs.setValueCaption(RatingStarsDemo.valueCaptions.values()
                .toArray(new String[5]));

            yourRs.addValueChangeListener(event -> {
                final Double value = (Double) event.getProperty()
                    .getValue();

                Notification.show("You voted " + value + " stars for " + movieName + ".", Notification.Type.TRAY_NOTIFICATION);

                final RatingStars changedRs = (RatingStars) event.getProperty();
                // reset value captions
                changedRs.setValueCaption(RatingStarsDemo.valueCaptions.values()
                    .toArray(new String[5]));
                // set "Your Rating" caption
                changedRs.setValueCaption((int) Math.round(value), "Your Rating");

                // dummy logic to calculate "average" value
                avgRs.setReadOnly(false);
                avgRs.setValue(((avgRs.getValue()) + value) / 2);
                avgRs.setReadOnly(true);
            });
            this.allRatingStars.add(yourRs);

            final Object itemId = this.table.addItem();
            final Item i = this.table.getItem(itemId);
            i.getItemProperty("Movie")
                .setValue(movieName);
            i.getItemProperty("Comment")
                .setValue(textField);
            i.getItemProperty("Your rating")
                .setValue(yourRs);
            i.getItemProperty("Average rating")
                .setValue(avgRs);
        }
    }

}