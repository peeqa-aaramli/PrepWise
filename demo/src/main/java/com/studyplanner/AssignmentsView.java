package com.studyplanner;

import java.util.HashMap;
import java.util.Map;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class AssignmentsView {

    private Map<String, ObservableList<Assignment>> database = new HashMap<>();
    private ObservableList<String> subjectsList = FXCollections.observableArrayList();
    public Map<String, ObservableList<Assignment>> getDatabase() { return database; }
    public ObservableList<String> getSubjectsList() { return subjectsList; }    

    public VBox getView() {
        VBox root = new VBox(20);
        root.getStyleClass().add("assignments-view");

        Label title = new Label("Assignments");
        title.getStyleClass().add("assignments-title");

        HBox mainLayout = new HBox(20);
        VBox.setVgrow(mainLayout, Priority.ALWAYS);

        //SUBJECTS
        VBox subjectsBox = new VBox(10);
        subjectsBox.setPrefWidth(250);
        
        Label subjectsLabel = new Label("Subjects");
        subjectsLabel.getStyleClass().add("assignments-section-label");

        ListView<String> subjectListView = new ListView<>(subjectsList);
        VBox.setVgrow(subjectListView, Priority.ALWAYS);
        subjectListView.getStyleClass().add("subject-list-view");

        //Subject Controls
        HBox addSubjectBox = new HBox(5);
        TextField newSubjectField = new TextField();
        newSubjectField.setPromptText("New Subject...");
        HBox.setHgrow(newSubjectField, Priority.ALWAYS);
        
        Button addSubjectBtn = createStyledButton("+");
        Button removeSubjectBtn = createStyledButton("-");
        addSubjectBox.getChildren().addAll(newSubjectField, addSubjectBtn, removeSubjectBtn);

        subjectsBox.getChildren().addAll(subjectsLabel, subjectListView, addSubjectBox);

        //ASSIGNMENTS TABLE
        VBox assignmentsBox = new VBox(10);
        HBox.setHgrow(assignmentsBox, Priority.ALWAYS);

        Label assignmentsLabel = new Label("Select a subject to view assignments");
        assignmentsLabel.getStyleClass().add("assignments-section-label");

        TableView<Assignment> table = new TableView<>();
        table.setEditable(true);
        VBox.setVgrow(table, Priority.ALWAYS);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Assignment, String> titleCol = new TableColumn<>("Document / Task");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        titleCol.setCellFactory(TextFieldTableCell.forTableColumn());
        
        TableColumn<Assignment, String> dueDateCol = new TableColumn<>("Due Date");
        dueDateCol.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
        dueDateCol.setCellFactory(TextFieldTableCell.forTableColumn());

        TableColumn<Assignment, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setCellFactory(TextFieldTableCell.forTableColumn());

        table.getColumns().addAll(titleCol, dueDateCol, statusCol);

        //Assignment Controls
        HBox addAssignmentBox = new HBox(10);
        TextField titleField = new TextField();
        titleField.setPromptText("Document Name...");
        TextField dueField = new TextField();
        dueField.setPromptText("Due Date (e.g. Oct 12)");
        
        Button addAssignBtn = createStyledButton("Add Assignment");
        Button deleteAssignBtn = new Button("Delete Selected");
        deleteAssignBtn.getStyleClass().add("danger-btn");
        
        addAssignmentBox.getChildren().addAll(titleField, dueField, addAssignBtn, deleteAssignBtn);
        addAssignmentBox.setDisable(true); 

        assignmentsBox.getChildren().addAll(assignmentsLabel, table, addAssignmentBox);
        mainLayout.getChildren().addAll(subjectsBox, assignmentsBox);
        root.getChildren().addAll(title, mainLayout);

        //EVENT HANDLERS

        //Add new subject
        addSubjectBtn.setOnAction(e -> {
            String subject = newSubjectField.getText().trim();
            if (!subject.isEmpty() && !subjectsList.contains(subject)) {
                subjectsList.add(subject);
                database.put(subject, FXCollections.observableArrayList());
                newSubjectField.clear();
            }
        });

        //Delete selected subject
        removeSubjectBtn.setOnAction(e -> {
            String selectedSubj = subjectListView.getSelectionModel().getSelectedItem();
            if (selectedSubj != null) {
                database.remove(selectedSubj);
                subjectsList.remove(selectedSubj);
                
                table.setItems(null);
                assignmentsLabel.setText("Select a subject to view assignments");
                addAssignmentBox.setDisable(true);
            }
        });

        //Select a subject
        subjectListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                assignmentsLabel.setText("Assignments for: " + newVal);
                table.setItems(database.get(newVal));
                addAssignmentBox.setDisable(false);
            }
        });

        //Add an assignment
        addAssignBtn.setOnAction(e -> {
            String selectedSubj = subjectListView.getSelectionModel().getSelectedItem();
            if (selectedSubj != null) {
                String task = titleField.getText().isEmpty() ? "Untitled" : titleField.getText();
                String due = dueField.getText().isEmpty() ? "TBD" : dueField.getText();
                
                database.get(selectedSubj).add(new Assignment(task, due, "Not Started"));
                
                titleField.clear();
                dueField.clear();
            }
        });

        //Delete selected assignment row
        deleteAssignBtn.setOnAction(e -> {
            String selectedSubj = subjectListView.getSelectionModel().getSelectedItem();
            Assignment selectedAssignment = table.getSelectionModel().getSelectedItem();
            
            if (selectedSubj != null && selectedAssignment != null) {
                database.get(selectedSubj).remove(selectedAssignment);
            }
        });

        return root;
    }

    private Button createStyledButton(String text) {
        Button btn = new Button(text);
        btn.getStyleClass().add("dark-purple-btn");
        return btn;
    }

    //DATA MODEL
    public static class Assignment {
        private final SimpleStringProperty title;
        private final SimpleStringProperty dueDate;
        private final SimpleStringProperty status;

        public Assignment(String title, String dueDate, String status) {
            this.title = new SimpleStringProperty(title);
            this.dueDate = new SimpleStringProperty(dueDate);
            this.status = new SimpleStringProperty(status);
            
            this.title.addListener((obs, oldVal, newVal) -> setTitle(newVal));
            this.dueDate.addListener((obs, oldVal, newVal) -> setDueDate(newVal));
            this.status.addListener((obs, oldVal, newVal) -> setStatus(newVal));
        }

        public String getTitle() { return title.get(); }
        public void setTitle(String title) { this.title.set(title); }
        public SimpleStringProperty titleProperty() { return title; }

        public String getDueDate() { return dueDate.get(); }
        public void setDueDate(String dueDate) { this.dueDate.set(dueDate); }
        public SimpleStringProperty dueDateProperty() { return dueDate; }

        public String getStatus() { return status.get(); }
        public void setStatus(String status) { this.status.set(status); }
        public SimpleStringProperty statusProperty() { return status; }
    }
}