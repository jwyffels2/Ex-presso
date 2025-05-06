/** Code: Luke Stine
 * Filters cells that fall within a specified rectangular range (inclusive).
 */

package psu.expresso.model;

import java.awt.Point;

public class RangeCellFilter implements CellFilterIF {

    private final int startRow, startCol, endRow, endCol;

    /**
     * Constructs a filter for a rectangular region in the spreadsheet.
     *
     * @param startRow starting row index (inclusive)
     * @param startCol starting column index (inclusive)
     * @param endRow   ending row index (inclusive)
     * @param endCol   ending column index (inclusive)
     */
    public RangeCellFilter(int startRow, int startCol, int endRow, int endCol) {
        // Normalize to ensure start <= end
        this.startRow = Math.min(startRow, endRow);
        this.startCol = Math.min(startCol, endCol);
        this.endRow   = Math.max(startRow, endRow);
        this.endCol   = Math.max(startCol, endCol);
    }

    @Override
    public boolean filter(CellDataModel<?> cell) {
        Point cellLocation = cell.getLocation(); // This will need to exist
        if (cellLocation == null) return false;

        int row = cellLocation.x;
        int col = cellLocation.y;

        return row >= startRow && row <= endRow &&
                col >= startCol && col <= endCol;
    }
}
