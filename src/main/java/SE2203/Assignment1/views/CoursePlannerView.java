package SE2203.Assignment1.views;

import SE2203.Assignment1.Assessment;
import SE2203.Assignment1.CoursePlannerService;
import SE2203.Assignment1.GradeCalculator;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.Route;
import java.util.List;

@Route("")
public class CoursePlannerView extends VerticalLayout {

    private final CoursePlannerService service;
    private final Grid<Assessment> grid = new Grid<>(Assessment.class);
    private final Binder<Assessment> binder = new Binder<>(Assessment.class);
    private Assessment selectedAssessment;

    private final VerticalLayout plannerSection = new VerticalLayout();
    private final VerticalLayout summarySection = new VerticalLayout();
    private final Span sectionLabel = new Span("Current section: Planner");
    private final H1 headerTitle = new H1("Planner");

    // Summary Components
    private final Span totalMarkedWeightTxt = new Span();
    private final Span weightedGradeSoFarTxt = new Span();
    private final Span remainingWeightTxt = new Span();
    private final NumberField targetOverallField = new NumberField("Target Overall (%)");
    private final Span requiredAverageTxt = new Span();

    public CoursePlannerView(CoursePlannerService service) {
        this.service = service;
        setSpacing(true);
        setPadding(true);
        setSizeFull(); // Ensures the app takes the full browser height

        setupNavigation();
        setupPlannerSection();
        setupSummarySection();

        summarySection.setVisible(false);
        add(sectionLabel, headerTitle, plannerSection, summarySection);

        refreshGrid();
    }

    private void setupNavigation() {
        Button plannerBtn = new Button("Planner", e -> switchView("Planner"));
        Button summaryBtn = new Button("Summary", e -> switchView("Summary"));
        Button helpBtn = new Button("Help", e -> openHelpDialog());

        HorizontalLayout navBar = new HorizontalLayout(plannerBtn, summaryBtn, helpBtn);
        add(navBar);
    }

    private void switchView(String viewName) {
        boolean isPlanner = viewName.equals("Planner");
        plannerSection.setVisible(isPlanner);
        summarySection.setVisible(!isPlanner);
        sectionLabel.setText("Current section: " + viewName);
        headerTitle.setText(viewName);
        if (!isPlanner) updateSummaryCalculations();
    }

    private void setupPlannerSection() {
        // --- Side Form ---
        TextField nameField = new TextField("Assessment name *");
        nameField.setWidthFull();

        ComboBox<String> typeField = new ComboBox<>("Type");
        typeField.setItems("LAB", "QUIZ", "ASSIGNMENT", "MIDTERM", "FINAL", "PROJECT", "OTHER");
        typeField.setWidthFull();

        NumberField weightField = new NumberField("Weight (%)");
        weightField.setWidthFull();

        Checkbox markedCheck = new Checkbox("Marked?");

        NumberField markField = new NumberField("Mark (%)");
        markField.setWidthFull();
        markField.setEnabled(false);

        markedCheck.addValueChangeListener(e -> {
            markField.setEnabled(e.getValue());
            if (!e.getValue()) markField.clear();
        });

        // Binding
        binder.forField(nameField).asRequired().withValidator(n -> service.isNameUnique(n, selectedAssessment), "Duplicate name").bind(Assessment::getName, Assessment::setName);
        binder.forField(weightField).asRequired().bind(Assessment::getWeight, Assessment::setWeight);
        binder.forField(markField).bind(Assessment::getMark, Assessment::setMark);
        binder.bind(markedCheck, Assessment::isMarked, Assessment::setMarked);
        binder.bind(typeField, Assessment::getType, Assessment::setType);

        // Buttons
        Button saveBtn = new Button("Save", e -> {
            // 1. Check if the name is unique manually to trigger the specific notification
            String currentName = nameField.getValue();
            if (!service.isNameUnique(currentName, selectedAssessment)) {
                // This creates the "small box at the bottom"
                Notification notification = Notification.show(
                        "An assessment with the name '" + currentName + "' already exists.",
                        3000, // duration in milliseconds
                        Notification.Position.BOTTOM_CENTER
                );
                return; // Stop execution
            }

            // 2. Proceed with standard validation and saving
            Assessment a = (selectedAssessment != null) ? selectedAssessment : new Assessment();
            if (binder.writeBeanIfValid(a)) {
                service.save(a);
                Notification.show("Assessment saved successfully!", 3000, Notification.Position.BOTTOM_CENTER);
                refreshGrid();
                clearForm();
            } else {
                Notification.show("Please correct the errors in the form.", 3000, Notification.Position.BOTTOM_CENTER);
            }
        });

        Button clearBtn = new Button("Clear", e -> clearForm());
        Button deleteBtn = new Button("Delete", e -> {
            if (selectedAssessment != null) {
                service.delete(selectedAssessment);
                refreshGrid();
                clearForm();
            }
        });
        deleteBtn.setEnabled(false);

        HorizontalLayout actions = new HorizontalLayout(saveBtn, clearBtn, deleteBtn);

        VerticalLayout formColumn = new VerticalLayout(nameField, typeField, weightField, markedCheck, markField, actions);
        formColumn.setWidth("300px"); // Exact width to match the visual sidebar
        formColumn.setPadding(false);

        // --- Grid ---
        grid.setColumns("name", "type", "weight");
        grid.addColumn(a -> a.isMarked() ? "Yes" : "No").setHeader("Marked?");
        grid.addColumn(a -> a.getMark() != null ? String.format("%.1f", a.getMark()) : "").setHeader("Mark (%)");
        grid.setSizeFull();

        grid.asSingleSelect().addValueChangeListener(e -> {
            selectedAssessment = e.getValue();
            deleteBtn.setEnabled(selectedAssessment != null);
            if (selectedAssessment != null) binder.readBean(selectedAssessment);
        });

        // The Layout Secret: Use a HorizontalLayout that expands
        HorizontalLayout layoutWrapper = new HorizontalLayout(formColumn, grid);
        layoutWrapper.setSizeFull();
        layoutWrapper.setFlexGrow(1, grid); // Grid eats the rest of the space

        plannerSection.add(layoutWrapper);
        plannerSection.setSizeFull();
        plannerSection.setPadding(false);
    }

    private void setupSummarySection() {
        targetOverallField.setValue(80.0);
        targetOverallField.addValueChangeListener(e -> updateSummaryCalculations());

        summarySection.setSpacing(true);
        summarySection.add(totalMarkedWeightTxt, weightedGradeSoFarTxt, remainingWeightTxt,
                new Span("---------------------------"), targetOverallField, requiredAverageTxt);
    }

    private void updateSummaryCalculations() {
        List<Assessment> data = service.findAll();
        double weight = GradeCalculator.calculateTotalMarkedWeight(data);
        double grade = GradeCalculator.calculateWeightedGradeSoFar(data);
        double req = GradeCalculator.calculateRequiredAverage(data, targetOverallField.getValue() != null ? targetOverallField.getValue() : 0);

        totalMarkedWeightTxt.setText(String.format("Total Marked Weight: %.1f%%", weight));
        weightedGradeSoFarTxt.setText(String.format("Weighted Grade So Far (marked only): %.1f%%", grade));
        remainingWeightTxt.setText(String.format("Remaining Weight to Reach 100: %.1f%%", 100.0 - weight));
        requiredAverageTxt.setText(String.format("Required Average on Remaining for Goal: %.1f%%", req));
    }

    private void openHelpDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Application Instructions");
        dialog.add(new VerticalLayout(new Span("Planner: add assessments with weights and marks."),
                new Span("Summary: see totals and required average for a target grade."),
                new Span("Rules: total planned weight cannot exceed 100%; names must be unique.")));
        dialog.setCloseOnOutsideClick(false);
        dialog.setCloseOnEsc(false);
        dialog.getFooter().add(new Button("Close", e -> dialog.close()));
        dialog.open();
    }

    private void refreshGrid() { grid.setItems(service.findAll()); }

    private void clearForm() {
        selectedAssessment = null;
        binder.readBean(new Assessment());
        grid.deselectAll();
    }
}