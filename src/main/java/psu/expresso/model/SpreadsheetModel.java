package psu.expresso.model;

import java.awt.Point;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.List;
import java.util.stream.Collectors;
import psu.expresso.model.CellFilterIF;

/**
 * A sparse, lazy grid of CellDataModel<T>, keyed by (row,col).
 * Cells are created on demand.
 */
public class SpreadsheetModel<T> {
    private final int rowCount, colCount;
    private final Map<Point,CellDataModel<T>> cells = new HashMap<>();

    public List<CellDataModel<Object>> filterCells(CellFilterIF filter) {
        return cells.values().stream()
                .filter(cell -> filter.filter(cell))
                .map(cell -> (CellDataModel<Object>) cell)  // cast to Object
                .collect(Collectors.toList());
    }

    public SpreadsheetModel(int rowCount, int colCount) {
        this.rowCount = rowCount;
        this.colCount = colCount;
    }
    public int getRowCount()    { return rowCount; }
    public int getColumnCount() { return colCount; }

    public Optional<CellDataModel<T>> getCellIfExists(int row,int col) {
        return Optional.ofNullable(cells.get(new Point(row,col)));
    }
    public CellDataModel<T> getOrCreateCell(int row, int col) {
        return cells.computeIfAbsent(new Point(row, col), p -> {
            CellDataModel<T> cell = new CellDataModel<>();
            cell.setLocation(p); // Set location on creation
            return cell;
        });
    }
    public void removeCell(int row,int col) {
        cells.remove(new Point(row,col));
    }
}
