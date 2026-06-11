package com.studyplanner;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

public class App extends Application {

    // Global layout components
    private StackPane contentArea;
    private VBox sidebar;
    private Label topMenuIcon;
    private boolean isSidebarVisible = true;
    private VBox taskList;
    private VBox reminderList;
    private VBox finishedList;
    private GridPane deadlinesGrid;

    // Persistent View instances managed as fields
    private AssignmentsView assignmentsView;
    private CalendarView calendarView;

    @Override
    public void start(Stage stage) {
        HBox root = new HBox();
    
        // Initialize independent views first so navigation and managers can share references safely
        assignmentsView = new AssignmentsView();
        calendarView = new CalendarView();

        // Main Content Area
        contentArea = new StackPane();
        contentArea.getChildren().add(createDashboardView());
        VBox.setVgrow(contentArea, Priority.ALWAYS);

        // Sidebar (Uses the instantiated view fields)
        sidebar = createSidebar();

        // Top Bar + Main Content
        VBox rightSide = new VBox();
        HBox.setHgrow(rightSide, Priority.ALWAYS);
    
        HBox topBar = createTopBar();
    
        rightSide.getChildren().addAll(topBar, contentArea);
        root.getChildren().addAll(sidebar, rightSide);

        Scene scene = new Scene(root, 1000, 600);
        scene.getStylesheets().add(getClass().getResource("appstyle.css").toExternalForm());
        
        // --- DATA PERSISTENCE HOOKS ---
        // 1. Load existing text file items back into UI maps
        DataManager.loadAllData(this, assignmentsView, calendarView);

        // 2. Refresh initial dashboard display if data populated
        contentArea.getChildren().setAll(createDashboardView());

        // 3. Intercept app close requests to serialize local state parameters
        stage.setOnCloseRequest(e -> {
            DataManager.saveAllData(this, assignmentsView, calendarView);
        });
        // ------------------------------

        stage.setTitle("PrepWise Dashboard");
        stage.setScene(scene);
        stage.show();
    }

    private VBox createSidebar() {
        VBox newSidebar = new VBox(15);
        newSidebar.getStyleClass().add("sidebar");

        // Menu Icon to CLOSE the sidebar
        Label menuIcon = new Label("≡");
        menuIcon.getStyleClass().addAll("menu-icon", "sidebar-menu-icon");
        menuIcon.setOnMouseClicked(e -> toggleSidebar());
        
        // Create Navigation Buttons
        Label dashboardBtn = createNavButton("🏠   Dashboard");
        Label assignmentsBtn = createNavButton("✏   Assignments");
        Label calendarBtn = createNavButton("📅   Calendar");

        List<Label> navButtons = Arrays.asList(dashboardBtn, assignmentsBtn, calendarBtn);

        // Set Initial Active State
        updateNavButtonStyle(dashboardBtn, true);

        // Add Click Events for View Switching (Linked directly to clean views)
        dashboardBtn.setOnMouseClicked(e -> switchView(dashboardBtn, navButtons, createDashboardView()));
        assignmentsBtn.setOnMouseClicked(e -> switchView(assignmentsBtn, navButtons, assignmentsView.getView()));
        calendarBtn.setOnMouseClicked(e -> switchView(calendarBtn, navButtons, calendarView.getView()));

        VBox navItems = new VBox(5);
        navItems.getChildren().addAll(navButtons);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        
        newSidebar.getChildren().addAll(menuIcon, navItems, spacer);
        return newSidebar;
    }

    private HBox createTopBar() {
        HBox topBar = new HBox();
        topBar.getStyleClass().add("top-bar");
        topBar.setAlignment(Pos.CENTER_LEFT);

        // Side nav bar
        topMenuIcon = new Label("≡");
        topMenuIcon.getStyleClass().addAll("menu-icon", "top-menu-icon");
        topMenuIcon.setVisible(false);
        topMenuIcon.setManaged(false);
        topMenuIcon.setOnMouseClicked(e -> toggleSidebar());

        Label welcome = new Label("Welcome!💮");
        welcome.getStyleClass().add("welcome-label");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Date at top right
        DatePicker datePicker = new DatePicker(LocalDate.now());
        datePicker.setEditable(false);

        topBar.getChildren().addAll(topMenuIcon, welcome, spacer, datePicker);
        return topBar;
    }

    private void toggleSidebar() {
        isSidebarVisible = !isSidebarVisible;
    
        sidebar.setVisible(isSidebarVisible);
        sidebar.setManaged(isSidebarVisible);
    
        topMenuIcon.setVisible(!isSidebarVisible);
        topMenuIcon.setManaged(!isSidebarVisible);
    }

    private void switchView(Label activeBtn, List<Label> allBtns, Node newContent) {
        for (Label btn : allBtns) {
            updateNavButtonStyle(btn, btn == activeBtn);
        }
        contentArea.getChildren().setAll(newContent);
    }

    private Label createNavButton(String text) {
        Label btn = new Label(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.getStyleClass().add("nav-btn");
        return btn;
    }

    private void updateNavButtonStyle(Label btn, boolean isActive) {
        if (isActive) {
            if (!btn.getStyleClass().contains("active")) {
                btn.getStyleClass().add("active");
            }
        } else {
            btn.getStyleClass().remove("active");
        }
    }
 
    private ScrollPane createDashboardView() {
        VBox dashboard = new VBox(20);
        dashboard.getStyleClass().add("dashboard-view");

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("Dashboard");
        title.getStyleClass().add("dashboard-title");
    
        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);
    
        header.getChildren().addAll(title, headerSpacer);

        HBox topRow = new HBox(20);
        topRow.getChildren().addAll(createOverviewCard(), createFocusModeCard());
    
        HBox bottomRow = new HBox(20);
        bottomRow.getChildren().addAll(createDeadlinesCard());

        dashboard.getChildren().addAll(header, topRow, bottomRow);

        ScrollPane scrollPane = new ScrollPane(dashboard);
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("dashboard-scroll-pane");
    
        return scrollPane;
    }

    private VBox createOverviewCard() {
        VBox card = createBaseCard();
        HBox.setHgrow(card, Priority.ALWAYS);

        Label title = new Label("Today's Overview");
        title.getStyleClass().add("card-title");

        HBox columns = new HBox(40);
        columns.setPadding(new Insets(15, 0, 0, 0));

        // TASKS SECTION
        VBox tasks = new VBox(10);
        Label tasksTitle = new Label("Tasks");
        tasksTitle.getStyleClass().add("column-title");
        taskList = new VBox(5);

        // REMINDERS SECTION
        VBox reminders = new VBox(10);
        Label remindersTitle = new Label("Reminders");
        remindersTitle.getStyleClass().add("column-title");
        reminderList = new VBox(5);

        // FINISHED TASKS SECTION
        VBox finishedTasks = new VBox(10);
        Label finishedTitle = new Label("Finished Tasks");
        finishedTitle.getStyleClass().add("column-title");
        finishedList = new VBox(5);

        finishedTasks.getChildren().addAll(finishedTitle, finishedList);

        // ADD TASK BUTTON
        Button addTaskBtn = new Button("+ Add Task");
        addTaskBtn.getStyleClass().add("purple-btn");
        addTaskBtn.setOnAction(e -> addOverviewItem(taskList, finishedList, "New Task"));

        // ADD REMINDER BUTTON
        Button addReminderBtn = new Button("+ Add Reminder");
        addReminderBtn.getStyleClass().add("purple-btn");
        addReminderBtn.setOnAction(e -> addOverviewItem(reminderList, finishedList, "New Reminder"));

        // DEFAULT DATA (These get cleared dynamically if a local file save exists)
        taskList.getChildren().addAll(
            createOverviewItem(taskList, finishedList, "Assignment 1"),
            createOverviewItem(taskList, finishedList, "Assignment 2"),
            createOverviewItem(taskList, finishedList, "Assignment 3")
        );

        reminderList.getChildren().addAll(
            createOverviewItem(reminderList, finishedList, "Reminder 1"),
            createOverviewItem(reminderList, finishedList, "Reminder 2"),
            createOverviewItem(reminderList, finishedList, "Reminder 3")
        );

        tasks.getChildren().addAll(tasksTitle, taskList, addTaskBtn);
        reminders.getChildren().addAll(remindersTitle, reminderList, addReminderBtn);

        Separator s1 = new Separator(javafx.geometry.Orientation.VERTICAL);
        Separator s2 = new Separator(javafx.geometry.Orientation.VERTICAL);

        columns.getChildren().addAll(tasks, s1, reminders, s2, finishedTasks);
        card.getChildren().addAll(title, columns);

        return card;
    }

    private HBox createOverviewItem(VBox originalParent, VBox finishedParent, String text) {
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);

        CheckBox checkBox = new CheckBox();

        TextField nameField = new TextField(text);
        nameField.getStyleClass().add("item-text-field");
        HBox.setHgrow(nameField, Priority.ALWAYS);

        Button deleteBtn = new Button("❌");
        deleteBtn.getStyleClass().add("delete-btn");
        deleteBtn.setOnAction(e -> {
            Pane parent = (Pane) row.getParent();
            if (parent != null) {
                parent.getChildren().remove(row);
            }
        });

        final VBox[] home = new VBox[]{originalParent};

        checkBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            Pane currentParent = (Pane) row.getParent();
            if (currentParent == null) return;

            currentParent.getChildren().remove(row);

            if (newVal) {
                finishedParent.getChildren().add(row);
            } else {
                home[0].getChildren().add(row);
            }
        });

        row.getChildren().addAll(checkBox, nameField, deleteBtn);
        return row;
    }

    private void addOverviewItem(VBox parent, VBox finishedParent, String text) {
        parent.getChildren().add(createOverviewItem(parent, finishedParent, text));
    }

    private VBox createFocusModeCard() {
        VBox card = createBaseCard();
        card.getStyleClass().add("focus-card");

        final int[] timeSeconds = {1500};

        TextField timeInput = new TextField("25");
        timeInput.getStyleClass().add("timer-display");
    
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            timeSeconds[0]--;
            int minutes = timeSeconds[0] / 60;
            int seconds = timeSeconds[0] % 60;
            timeInput.setText(String.format("%02d : %02d", minutes, seconds));
        
            if (timeSeconds[0] <= 0) {
                timeInput.setText("00 : 00");
            }
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
    
        HBox headerBox = new HBox();
        Label title = new Label("Focus Mode");
        title.getStyleClass().add("card-title");
        headerBox.getChildren().add(title);
    
        HBox buttons = new HBox(15);
        buttons.setAlignment(Pos.CENTER);
        Button play = new Button("▶");
        Button pause = new Button("⏸");
        Button stop = new Button("⏹");
    
        play.setOnAction(e -> {
    if (timeline.getStatus() != javafx.animation.Animation.Status.RUNNING) {
        try {
            if (!timeInput.getText().contains(":")) {
                int minutes = Integer.parseInt(timeInput.getText().trim());
                
                // Explicitly throw an exception if the user enters zero or negative numbers
                if (minutes <= 0 || minutes > 180) {
                    throw new IllegalArgumentException("Please enter a time between 1 and 180 minutes.");
                }
                
                timeSeconds[0] = minutes * 60;
            }
            timeline.play();
            timeInput.setEditable(false);
            
        } catch (NumberFormatException ex) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Invalid Input");
            alert.setHeaderText("Numbers Only");
            alert.setContentText("The focus timer field only accepts whole numbers (e.g., 25).");
            alert.showAndWait();
            timeInput.setText("25");
            
        } catch (IllegalArgumentException ex) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Invalid Range");
            alert.setHeaderText("Unrealistic Focus Time");
            alert.setContentText(ex.getMessage());
            alert.showAndWait();
            timeInput.setText("25");
        }
    }
});
    
        pause.setOnAction(e -> timeline.pause());
    
        stop.setOnAction(e -> {
            timeline.stop();
            timeSeconds[0] = 1500;
            timeInput.setText("25");
            timeInput.setEditable(true);
        });

        play.getStyleClass().add("timer-btn");
        pause.getStyleClass().add("timer-btn");
        stop.getStyleClass().add("timer-btn");
    
        buttons.getChildren().addAll(play, pause, stop);
        card.getChildren().addAll(headerBox, timeInput, buttons);
        return card;
    }

    private VBox createDeadlinesCard() {
        VBox card = createBaseCard();
        HBox.setHgrow(card, Priority.ALWAYS);
    
        Label title = new Label("Deadlines");
        title.getStyleClass().add("card-title");
    
        VBox innerBox = new VBox(10);
        innerBox.getStyleClass().add("deadlines-inner-box");
    
        deadlinesGrid = new GridPane();
        deadlinesGrid.getStyleClass().add("deadlines-grid");
    
        Label h1 = new Label("Name"); 
        Label h2 = new Label("Date"); 
        Label h3 = new Label("Priority");
        h1.getStyleClass().add("grid-header"); 
        h2.getStyleClass().add("grid-header"); 
        h3.getStyleClass().add("grid-header");
        deadlinesGrid.addRow(0, h1, h2, h3);
    
        Button addBtn = new Button("+ Add Deadline");
        addBtn.getStyleClass().add("add-deadline-btn");
        
        // FIX: Changed local variable parameter from "grid" to matching class field target "deadlinesGrid"
        addBtn.setOnAction(e -> addEditableRow(deadlinesGrid, "New Deadline", "DD/MM/YYYY", "Medium"));
    
        addEditableRow(deadlinesGrid, "• Deadline 1", "1/1/2026", "High");
        addEditableRow(deadlinesGrid, "• Deadline 2", "1/1/2026", "Low");
    
        innerBox.getChildren().addAll(deadlinesGrid, addBtn);
        card.getChildren().addAll(title, innerBox);
        return card;
    }

    private void addEditableRow(GridPane grid, String name, String date, String priority) {
        int rowIndex = grid.getRowCount();
        TextField nameField = createEditableTextField(name);
        TextField dateField = createEditableTextField(date);
    
        ComboBox<String> priorityCombo = new ComboBox<>();
        priorityCombo.getItems().addAll("High", "Medium", "Low");
        priorityCombo.setValue(priority);
        priorityCombo.getStyleClass().add("row-combo-box");
    
        Button deleteBtn = new Button("×");
        deleteBtn.getStyleClass().add("row-delete-btn");
        deleteBtn.setOnAction(e -> {
            grid.getChildren().removeIf(node -> GridPane.getRowIndex(node) == rowIndex);
            grid.getChildren().forEach(node -> {
                Integer r = GridPane.getRowIndex(node);
                if (r != null && r > rowIndex) GridPane.setRowIndex(node, r - 1);
            });
        });
    
        grid.addRow(rowIndex, nameField, dateField, priorityCombo, deleteBtn);
    }

    private TextField createEditableTextField(String text) {
        TextField field = new TextField(text);
        field.getStyleClass().add("grid-text-field");
        return field;
    }

    private VBox createBaseCard() {
        VBox card = new VBox();
        card.getStyleClass().add("base-card");
        return card;
    }

    // Accessors and dynamic loaders for the persistence DataManager
    public VBox getTaskList() { return taskList; }
    public VBox getReminderList() { return reminderList; }
    public VBox getFinishedList() { return finishedList; }
    public GridPane getDeadlinesGrid() { return deadlinesGrid; }

    public void clearOverviewAndDeadlines() {
        if (taskList != null) taskList.getChildren().clear();
        if (reminderList != null) reminderList.getChildren().clear();
        if (finishedList != null) finishedList.getChildren().clear();
        if (deadlinesGrid != null) {
            deadlinesGrid.getChildren().removeIf(node -> {
                Integer r = GridPane.getRowIndex(node);
                return r != null && r > 0;
            });
        }
    }

    public void loadTaskItem(String text) { taskList.getChildren().add(createOverviewItem(taskList, finishedList, text)); }
    public void loadReminderItem(String text) { reminderList.getChildren().add(createOverviewItem(reminderList, finishedList, text)); }
    public void loadFinishedItem(String text) { 
        HBox item = createOverviewItem(taskList, finishedList, text);
        for(Node n : item.getChildren()) { 
            if(n instanceof CheckBox) ((CheckBox)n).setSelected(true); 
        }
    }
    public void loadDeadlineRow(String name, String date, String prio) { addEditableRow(deadlinesGrid, name, date, prio); }

    public static void main(String[] args) {
        launch();
    }
}