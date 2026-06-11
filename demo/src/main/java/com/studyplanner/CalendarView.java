package com.studyplanner;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class CalendarView {

    private YearMonth currentYearMonth;
    private GridPane calendarGrid;
    private Label monthTitle;
    public Map<LocalDate, String> getNotesMap() { return notesMap; }
    
    //In-memory storage for notes
    private Map<LocalDate, String> notesMap = new HashMap<>();

    public VBox getView() {
        VBox root = new VBox(20);
        root.setAlignment(Pos.TOP_LEFT);
        root.getStyleClass().add("calendar-view");

        Label title = new Label("Calendar");
        title.getStyleClass().add("calendar-title");

        //Set current month to right now
        currentYearMonth = YearMonth.now();

        //Navigation Header
        HBox header = createNavigationHeader();

        //Calendar Grid
        calendarGrid = new GridPane();
        calendarGrid.setHgap(10);
        calendarGrid.setVgap(10);
        VBox.setVgrow(calendarGrid, Priority.ALWAYS);

        //Ensure the columns divide the space equally
        for (int i = 0; i < 7; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(100.0 / 7.0);
            calendarGrid.getColumnConstraints().add(cc);
        }

        populateCalendar();

        root.getChildren().addAll(title, header, calendarGrid);

        return root;
    }

    private HBox createNavigationHeader() {
        monthTitle = new Label();
        monthTitle.getStyleClass().add("calendar-month-title");
        monthTitle.setPrefWidth(150);
        monthTitle.setAlignment(Pos.CENTER);

        Button prevBtn = new Button("<");
        Button nextBtn = new Button(">");
        
        //Reusing the dark purple button utility class from AssignmentsView
        prevBtn.getStyleClass().add("dark-purple-btn");
        nextBtn.getStyleClass().add("dark-purple-btn");

        prevBtn.setOnAction(e -> changeMonth(-1));
        nextBtn.setOnAction(e -> changeMonth(1));

        HBox header = new HBox(15, prevBtn, monthTitle, nextBtn);
        header.setAlignment(Pos.CENTER_LEFT);
        return header;
    }

    private void changeMonth(int offset) {
        currentYearMonth = currentYearMonth.plusMonths(offset);
        populateCalendar();
    }

    private void populateCalendar() {
        calendarGrid.getChildren().clear();
        
        //Update title to show current Month and Year
        monthTitle.setText(currentYearMonth.getMonth().toString() + " " + currentYearMonth.getYear());

        //Add Day of Week Headers
        String[] days = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (int i = 0; i < 7; i++) {
            Label dayLabel = new Label(days[i]);
            dayLabel.getStyleClass().add("calendar-day-header");
            dayLabel.setMaxWidth(Double.MAX_VALUE);
            dayLabel.setAlignment(Pos.CENTER);
            calendarGrid.add(dayLabel, i, 0);
        }

        //Calculate offset and days in month
        LocalDate firstOfMonth = currentYearMonth.atDay(1);
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue() % 7; // Convert 1-7 (Mon-Sun) to 0-6 (Sun-Sat)
        int daysInMonth = currentYearMonth.lengthOfMonth();

        int row = 1;
        int col = dayOfWeek;

        //Populate the actual dates
        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = currentYearMonth.atDay(day);
            VBox dayCell = createDayCell(date);
            calendarGrid.add(dayCell, col, row);

            col++;
            if (col == 7) { 
                col = 0;
                row++;
            }
        }
    }

    private VBox createDayCell(LocalDate date) {
        VBox cell = new VBox(5);
        cell.setPrefSize(100, 100); 
        cell.getStyleClass().add("calendar-day-cell");

        Label dayLbl = new Label(String.valueOf(date.getDayOfMonth()));
        dayLbl.getStyleClass().add("calendar-day-number");

        // The text area for user notes
        TextArea textArea = new TextArea();
        textArea.setWrapText(true);
        textArea.getStyleClass().add("calendar-cell-textarea");
        VBox.setVgrow(textArea, Priority.ALWAYS);

        // Load existing note if it exists
        if (notesMap.containsKey(date)) {
            textArea.setText(notesMap.get(date));
        }

        // Automatically save the note to the map whenever the user types
        textArea.textProperty().addListener((obs, oldVal, newVal) -> {
            notesMap.put(date, newVal);
        });

        cell.getChildren().addAll(dayLbl, textArea);
        return cell;
    }
}