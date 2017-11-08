package org.vaadin.teemu.ratingstars.demo;

import javax.servlet.annotation.WebServlet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import org.vaadin.teemu.ratingstars.RatingStars;

/**
 * A demo application for the RatingStars component. For a live demo see
 *
 * 
 * @author Teemu Pöntelin
 */
@Theme("valo")
@Title("RatingStars Component Demo")
@SuppressWarnings("unused")
public class RatingStarsDemo extends UI {
    private static final long serialVersionUID = 7705972095201251401L;

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

    private final String[] movieNames = { "The Matrix", "Memento", "Kill Bill: Vol. 1" };

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
        demoPanel.setContent(demoLayout);
        this.mainLayout.addComponent(demoPanel);

        // animated checkbox
        final CheckBox animatedCheckBox = new CheckBox("Animated?");
        animatedCheckBox.setValue(true);
        animatedCheckBox.addValueChangeListener(event -> {
            for (final RatingStars rs : this.allRatingStars) {
                rs.setAnimated(event.getValue());
            }
        });

        demoLayout.addComponents(animatedCheckBox, createMovieDemo(), createThemeDemos());
    }

    private VerticalLayout createThemeDemos() {
        final VerticalLayout themeDemos = new VerticalLayout();
        themeDemos.setMargin(false);

        // theme demos
        themeDemos.addComponent(new Label("<strong>The component has two built-in styles.</strong>", ContentMode.HTML));

        final RatingStars defaultRs = new RatingStars();
        defaultRs.setDescription("Default RatingStars");
        defaultRs.setCaption("default");
        this.allRatingStars.add(defaultRs);

        final RatingStars tinyRs = new RatingStars();
        tinyRs.setMaxValue(3);
        tinyRs.setStyleName("tiny");
        tinyRs.setCaption("tiny");
        this.allRatingStars.add(tinyRs);

        themeDemos.addComponent(new HorizontalLayout(defaultRs, tinyRs));

        // component states
        themeDemos.addComponent(new Label("<strong>Component states</strong>", ContentMode.HTML));

        final RatingStars disabledRs = new RatingStars();
        disabledRs.setCaption("disabled");
        disabledRs.setValue(2.5);
        disabledRs.setEnabled(false);

        final RatingStars readonlyRs = new RatingStars();
        readonlyRs.setCaption("read-only");
        readonlyRs.setValue(2.5);
        readonlyRs.setReadOnly(true);

        themeDemos.addComponent(new HorizontalLayout(disabledRs, readonlyRs));

        return themeDemos;
    }

    private VerticalLayout createMovieDemo() {
        final VerticalLayout movieDemo = new VerticalLayout();
        movieDemo.setMargin(false);
        movieDemo.addComponent(new Label("Rate your favourite movies:"));

        for (final String movieName : this.movieNames) {
            final RatingStars averageRating = new RatingStars();
            averageRating.setMaxValue(5);
            averageRating.setValue(ThreadLocalRandom.current()
                .nextDouble(1, 5));
            averageRating.setReadOnly(true);
            this.allRatingStars.add(averageRating);

            final RatingStars userRating = new RatingStars();
            userRating.setMaxValue(5);
            userRating.setValueCaption(RatingStarsDemo.valueCaptions.values()
                .toArray(new String[5]));
            userRating.addValueChangeListener(event -> {
                final Double value = event.getValue();

                Notification.show("You voted " + value + " stars for " + movieName + ".", Notification.Type.TRAY_NOTIFICATION);

                final RatingStars changedRs = (RatingStars) event.getComponent();
                // reset value captions
                changedRs.setValueCaption(RatingStarsDemo.valueCaptions.values()
                    .toArray(new String[5]));
                // set "Your Rating" caption
                changedRs.setValueCaption((int) Math.round(value), "Your Rating");

                // dummy logic to calculate "average" value
                averageRating.setValue((averageRating.getValue() + value) / 2);
            });

            this.allRatingStars.add(userRating);

            final Label movieNameLabel = new Label(movieName);
            movieNameLabel.setWidth("100px");
            final HorizontalLayout movieRow = new HorizontalLayout(movieNameLabel, userRating, averageRating);
            movieRow.setMargin(false);
            movieDemo.addComponent(movieRow);
        }

        return movieDemo;
    }

}
