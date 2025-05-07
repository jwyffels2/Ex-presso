package psu.expresso.model;

import java.awt.Point;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.List;
import java.util.stream.Collectors;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReadWriteLock;
import java.io.*;

/**
 * A sparse, lazy grid of CellDataModel<T>, keyed by (row,col).
 * Cells are created on demand.
 */
public class SpreadsheetModel<T> {
    private final int rowCount, colCount;
    private final Map<Point, CellDataModel<T>> cells = new HashMap<>();

    public SpreadsheetModel(int rowCount, int colCount) {
        this.rowCount = rowCount;
        this.colCount = colCount;
    }

    public int getRowCount() {
        return rowCount;
    }

    public int getColumnCount() {
        return colCount;
    }

    public Optional<CellDataModel<T>> getCellIfExists(int row, int col) {
        return Optional.ofNullable(cells.get(new Point(row, col)));
    }

    public CellDataModel<T> getOrCreateCell(int row, int col) {
        return cells.computeIfAbsent(new Point(row, col), p -> {
            CellDataModel<T> cell = new CellDataModel<>();
            cell.setLocation(p);
            return cell;
        });
    }

    public void removeCell(int row, int col) {
        cells.remove(new Point(row, col));
    }

    public List<CellDataModel<Object>> filterCells(CellFilterIF filter) {
        return cells.values().stream()
                .filter(cell -> filter.filter(cell))
                .map(cell -> (CellDataModel<Object>) cell)
                .collect(Collectors.toList());
    }

    /**
     * Manages saving and loading spreadsheet data with thread-safe locking.
     */
    public class SpreadsheetIOManager {
        private final ReadWriteLock lock = new ReentrantReadWriteLock();
        private final SpreadsheetModel<T> model;

        public SpreadsheetIOManager(SpreadsheetModel<T> model) {
            this.model = model;
        }

        public void saveToFile(File file) {
            lock.writeLock().lock();
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                for (Map.Entry<Point, CellDataModel<T>> entry : model.cells.entrySet()) {
                    Point location = entry.getKey();
                    CellDataModel<T> cell = entry.getValue();
                    T value = cell.getValue();
                    if (value != null) {
                        writer.write(location.x + "," + location.y + "," + value.toString());
                        writer.newLine();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                lock.writeLock().unlock();
            }
        }

        public void loadFromFile(File file) {
            lock.writeLock().lock();
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                model.cells.clear(); // clear existing data
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(",", 3);
                    if (parts.length == 3) {
                        int row = Integer.parseInt(parts[0]);
                        int col = Integer.parseInt(parts[1]);
                        String rawValue = parts[2];
                        CellDataModel<T> cell = model.getOrCreateCell(row, col);
                        cell.setValue((T) rawValue);
                        cell.setDisplayValue((T) rawValue);
                    }
                }
            } catch (IOException | NumberFormatException e) {
                e.printStackTrace();
            } finally {
                lock.writeLock().unlock();
            }
        }

        public ReadWriteLock getLock() {
            return lock;
        }
    }
}
