package psu.expresso;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.*;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.SelectionMode;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import psu.expresso.model.*;
import java.util.List;

public class Expresso extends Application {
    private static final int NUM_ROWS = 1000;
    private static final int NUM_COLS = 50;

    private SpreadsheetModel<Object> model;
    private FormulaEngine engine;
    private TableView<Integer> table;

    /** Parses raw text → Double or String; blank→null */
    private final StringConverter<Object> dynamicConverter = new StringConverter<>() {
        @Override public String toString(Object obj) {
            return obj == null ? "" : obj.toString();
        }
        @Override public Object fromString(String str) {
            if (str == null || str.isEmpty()) return null;
            try { return Double.parseDouble(str); }
            catch (NumberFormatException e) { return str; }
        }
    };

    @Override
    public void start(Stage stage) {
        model  = new SpreadsheetModel<>(NUM_ROWS, NUM_COLS);
        engine = new FormulaEngine(model);

        table  = createTableView();
        ToolBar ribbon = createRibbon();

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(8));
        root.setTop(ribbon);
        root.setCenter(table);

        Scene scene = new Scene(root, 1000, 700);
        scene.getStylesheets().add(
                getClass().getResource("/psu/expresso/spreadsheet.css")
                        .toExternalForm()
        );

        stage.setScene(scene);
        stage.setTitle("JavaFX Spreadsheet Prototype");
        stage.show();
    }

    private TableView<Integer> createTableView() {
        ObservableList<Integer> rows = FXCollections.observableArrayList();
        for (int r = 0; r < NUM_ROWS; r++) rows.add(r);

        TableView<Integer> tv = new TableView<>(rows);
        tv.setEditable(true);
        tv.setFixedCellSize(24);
        tv.getSelectionModel().setCellSelectionEnabled(true);

        // Allow multiple cells to be selected
        tv.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        tv.getColumns().add(rowNumberColumn());
        for (int c = 0; c < NUM_COLS; c++) {
            tv.getColumns().add(dataColumn(c));
        }
        return tv;
    }

    private TableColumn<Integer,Number> rowNumberColumn() {
        TableColumn<Integer,Number> col = new TableColumn<>("#");
        col.setPrefWidth(40);
        col.setSortable(false);
        col.setCellValueFactory(cd ->
                new ReadOnlyObjectWrapper<>(cd.getValue() + 1)
        );
        col.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item.toString());
            }
        });
        return col;
    }

    private TableColumn<Integer,Object> dataColumn(int ci) {
        TableColumn<Integer,Object> col = new TableColumn<>(toColName(ci));
        col.setPrefWidth(100);

        // display the computed displayValue
        col.setCellValueFactory(cd ->
                model.getCellIfExists(cd.getValue(), ci)
                        .<ReadOnlyObjectWrapper<Object>>map(c ->
                                new ReadOnlyObjectWrapper<>(c.getDisplayValue()))
                        .orElseGet(() ->
                                new ReadOnlyObjectWrapper<>(null))
        );

        // custom editor: edits raw value, shows displayValue
        col.setCellFactory(tc -> new TableCell<Integer,Object>() {
            private TextField editor;
            @Override protected void updateItem(Object item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null); setGraphic(null);
                } else if (isEditing()) {
                    setText(null);
                } else {
                    setText(item == null ? "" : item.toString());
                    setGraphic(null);
                }
            }
            @Override public void startEdit() {
                super.startEdit();
                if (editor == null) {
                    editor = new TextField();
                    editor.setOnAction(evt -> commitEditFromEditor());
                }
                var cell = model.getCellIfExists(getIndex(), ci).orElse(null);
                editor.setText(cell == null ? "" : String.valueOf(cell.getValue()));

                setText(null);
                setGraphic(editor);
                editor.requestFocus();
                editor.selectAll();
            }
            @Override public void cancelEdit() {
                super.cancelEdit();
                setGraphic(null);
                Object dv = getItem();
                setText(dv == null ? "" : dv.toString());
            }
            private void commitEditFromEditor() {
                String raw = editor.getText();
                int row = getIndex();
                if (raw == null || raw.isEmpty()) {
                    model.removeCell(row, ci);
                } else if (raw.startsWith("=")) {
                    engine.applyFormula(raw, row, ci);
                } else {
                    var cell = model.getOrCreateCell(row, ci);
                    cell.setValue(raw);
                    cell.setDisplayValue(dynamicConverter.fromString(raw));
                }
                cancelEdit();
                table.refresh();
            }
        });

        return col;
    }

    private String toColName(int idx) {
        StringBuilder sb = new StringBuilder();
        for (int i = idx; i >= 0; i = i/26 - 1)
            sb.append((char)('A' + (i % 26)));
        return sb.reverse().toString();
    }

    private ToolBar createRibbon() {
        TextField fx = new TextField();
        fx.setPromptText("fx:");
        fx.setPrefWidth(300);
        fx.setOnAction(ev -> {
            String text = fx.getText();
            var sel = table.getSelectionModel().getSelectedCells();
            if (sel.isEmpty()) return;
            int row = sel.get(0).getRow();
            int col = sel.get(0).getColumn() - 1;
            if (col < 0) return;
            if (text.startsWith("=")) {
                engine.applyFormula(text, row, col);
            } else {
                var cell = model.getOrCreateCell(row, col);
                cell.setValue(text);
                cell.setDisplayValue(dynamicConverter.fromString(text));
            }
            table.refresh();
        });

        Button filterButton = new Button("Filter");
        filterButton.setOnAction(ev -> {
            showFilterPopup();
        });

        return new ToolBar(
                new Button("New"),
                new Button("Open"),
                new Button("Save"),
                new Separator(),
                new Button("Undo"),
                new Button("Redo"),
                new Separator(),
                new Label("fx:"), fx,
                new Separator(), filterButton
        );
    }

    private void showFilterPopup() {
        TextField filterInput = new TextField();
        filterInput.setPromptText("Enter filter value or range");

        ButtonType valueButton = new ButtonType("Filter by Value");
        ButtonType typeButton = new ButtonType("Filter by Type");
        ButtonType rangeButton = new ButtonType("Filter by Range");
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        Alert filterAlert = new Alert(Alert.AlertType.INFORMATION, "", valueButton, typeButton, rangeButton, cancelButton);
        filterAlert.setHeaderText("Filter Selection");
        filterAlert.getDialogPane().setContent(filterInput);

        filterAlert.showAndWait().ifPresent(response -> {
            String filterCriteria = filterInput.getText().trim();
            ObservableList<TablePosition> selectedCells = table.getSelectionModel().getSelectedCells();

            if (response == valueButton) {
                applyValueFilter(selectedCells, filterCriteria);
            } else if (response == typeButton) {
                applyTypeFilter(selectedCells, filterCriteria);
            } else if (response == rangeButton) {
                applyRangeFilter(selectedCells, filterCriteria);
            }
        });
    }

    private void applyValueFilter(ObservableList<TablePosition> selectedCells, String filterCriteria) {
        for (TablePosition pos : selectedCells) {
            var cell = model.getCellIfExists(pos.getRow(), pos.getColumn()).orElse(null);
            if (cell != null && cell.getValue() != null) {
                if (!cell.getValue().toString().contains(filterCriteria)) {
                    table.getSelectionModel().clearSelection(pos.getRow());
                } else {
                    table.getSelectionModel().select(pos.getRow());
                }
            }
        }
    }

    private void applyTypeFilter(ObservableList<TablePosition> selectedCells, String filterCriteria) {
        try {
            Class<?> filterType = Class.forName(filterCriteria);

            for (TablePosition pos : selectedCells) {
                var cell = model.getCellIfExists(pos.getRow(), pos.getColumn()).orElse(null);
                if (cell != null && cell.getValue() != null) {
                    if (!filterType.isInstance(cell.getValue())) {
                        table.getSelectionModel().clearSelection(pos.getRow());
                    } else {
                        table.getSelectionModel().select(pos.getRow());
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            System.out.println("Invalid type: " + filterCriteria);
        }
    }

    private void applyRangeFilter(ObservableList<TablePosition> selectedCells, String filterCriteria) {
        String[] range = filterCriteria.split(",");
        if (range.length == 2) {
            String[] rows = range[0].split("-");
            String[] cols = range[1].split("-");

            try {
                int startRow = Integer.parseInt(rows[0]);
                int endRow = Integer.parseInt(rows[1]);
                int startCol = Integer.parseInt(cols[0]);
                int endCol = Integer.parseInt(cols[1]);

                for (TablePosition pos : selectedCells) {
                    var cell = model.getCellIfExists(pos.getRow(), pos.getColumn()).orElse(null);
                    if (cell != null) {
                        if (pos.getRow() < startRow || pos.getRow() > endRow || pos.getColumn() < startCol || pos.getColumn() > endCol) {
                            table.getSelectionModel().clearSelection(pos.getRow());
                        } else {
                            table.getSelectionModel().select(pos.getRow());
                        }
                    }
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid range format: " + filterCriteria);
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
