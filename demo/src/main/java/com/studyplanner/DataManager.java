package com.studyplanner;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Map;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class DataManager {
    private static final String FILE_NAME = "study_planner_data.txt";

    public static void saveAllData(App app, AssignmentsView assignmentsView, CalendarView calendarView) {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(FILE_NAME), StandardCharsets.UTF_8))) {

            // 1. SAVE DASHBOARD OVERVIEW
            writer.write("[TASKS]\n");
            serializeVBoxList(app.getTaskList(), writer);
            
            writer.write("[REMINDERS]\n");
            serializeVBoxList(app.getReminderList(), writer);
            
            writer.write("[FINISHED_TASKS]\n");
            serializeVBoxList(app.getFinishedList(), writer);

            // 2. SAVE DEADLINES GRID
            writer.write("[DEADLINES]\n");
            serializeGridPane(app.getDeadlinesGrid(), writer);

            // 3. SAVE ASSIGNMENTS DATABASE
            writer.write("[ASSIGNMENTS]\n");
            for (Map.Entry<String, javafx.collections.ObservableList<AssignmentsView.Assignment>> entry : assignmentsView.getDatabase().entrySet()) {
                String subject = entry.getKey();
                for (AssignmentsView.Assignment assign : entry.getValue()) {
                    writer.write(subject + "|" + assign.getTitle() + "|" + assign.getDueDate() + "|" + assign.getStatus() + "\n");
                }
            }

            // 4. SAVE CALENDAR NOTES
            writer.write("[CALENDAR]\n");
            for (Map.Entry<LocalDate, String> entry : calendarView.getNotesMap().entrySet()) {
                String cleanNote = entry.getValue().replace("\n", "%%BR%%");
                writer.write(entry.getKey().toString() + "|" + cleanNote + "\n");
            }

        } catch (IOException e) {
            showErrorAlert("Save Failed", "Could not save your data to disk. Check file permissions.\nDetails: " + e.getMessage());
        }
    }

    public static void loadAllData(App app, AssignmentsView assignmentsView, CalendarView calendarView) {
        File file = new File(FILE_NAME);
        if (!file.exists()) return; // First run, no data file exists yet. Return gracefully.

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new FileInputStream(file), StandardCharsets.UTF_8))) {

            String line;
            String currentSection = "";

            app.clearOverviewAndDeadlines();

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                if (line.startsWith("[")) {
                    currentSection = line;
                    continue;
                }

                // Inner try-catch protects the loop: if one line is corrupted, 
                // the app skips it and continues loading everything else safely.
                try {
                    switch (currentSection) {
                        case "[TASKS]":
                            app.loadTaskItem(line);
                            break;
                        case "[REMINDERS]":
                            app.loadReminderItem(line);
                            break;
                        case "[FINISHED_TASKS]":
                            app.loadFinishedItem(line);
                            break;
                        case "[DEADLINES]":
                            String[] dParts = line.split("\\|", -1);
                            if (dParts.length < 3) throw new IllegalArgumentException("Missing column data");
                            app.loadDeadlineRow(dParts[0], dParts[1], dParts[2]);
                            break;
                        case "[ASSIGNMENTS]":
                            String[] aParts = line.split("\\|", -1);
                            if (aParts.length < 4) throw new IllegalArgumentException("Missing column data");
                            String subj = aParts[0];
                            if (!assignmentsView.getSubjectsList().contains(subj)) {
                                assignmentsView.getSubjectsList().add(subj);
                                assignmentsView.getDatabase().put(subj, FXCollections.observableArrayList());
                            }
                            assignmentsView.getDatabase().get(subj).add(
                                new AssignmentsView.Assignment(aParts[1], aParts[2], aParts[3])
                            );
                            break;
                        case "[CALENDAR]":
                            String[] cParts = line.split("\\|", -1);
                            if (cParts.length < 2) throw new IllegalArgumentException("Missing column data");
                            LocalDate date = LocalDate.parse(cParts[0]);
                            String note = cParts[1].replace("%%BR%%", "\n");
                            calendarView.getNotesMap().put(date, note);
                            break;
                    }
                } catch (DateTimeParseException | IllegalArgumentException ex) {
                    System.err.println("Skipping malformed data line in " + currentSection + ": " + line);
                }
            }
        } catch (IOException e) {
            showErrorAlert("Load Error", "An error occurred while loading your saved file.\nDetails: " + e.getMessage());
        }
    }

    private static void serializeVBoxList(VBox container, BufferedWriter writer) throws IOException {
        if (container == null) return;
        for (Node node : container.getChildren()) {
            if (node instanceof HBox) {
                HBox row = (HBox) node;
                for (Node child : row.getChildren()) {
                    if (child instanceof TextField) {
                        writer.write(((TextField) child).getText() + "\n");
                        break;
                    }
                }
            }
        }
    }

    private static void serializeGridPane(GridPane grid, BufferedWriter writer) throws IOException {
        if (grid == null) return;
        int rows = grid.getRowCount();
        for (int r = 1; r < rows; r++) {
            String name = "";
            String date = "";
            String priority = "Medium";
            
            for (Node node : grid.getChildren()) {
                Integer rIdx = GridPane.getRowIndex(node);
                Integer cIdx = GridPane.getColumnIndex(node);
                if (rIdx != null && rIdx == r && cIdx != null) {
                    if (cIdx == 0 && node instanceof TextField) name = ((TextField) node).getText();
                    if (cIdx == 1 && node instanceof TextField) date = ((TextField) node).getText();
                    if (cIdx == 2 && node instanceof ComboBox) {
                        Object val = ((ComboBox<?>) node).getValue();
                        if (val != null) priority = val.toString();
                    }
                }
            }
            if (!name.isEmpty() || !date.isEmpty()) {
                writer.write(name + "|" + date + "|" + priority + "\n");
            }
        }
    }

    // Helper method to display clean UI alerts to the user
    private static void showErrorAlert(String title, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("System Error");
            alert.setHeaderText(title);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }
}