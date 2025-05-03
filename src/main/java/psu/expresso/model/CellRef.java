package psu.expresso.model;

import java.awt.Point;

/**
 * Converts between "A1"-style refs and zero-based (row,col).
 */
public class CellRef {
    public static Point parse(String loc) {
        loc = loc.toUpperCase().trim();
        int i = 0;
        while (i < loc.length() && Character.isLetter(loc.charAt(i))) i++;
        String colPart = loc.substring(0, i);
        String rowPart = loc.substring(i);
        int col = 0;
        for (char ch : colPart.toCharArray()) {
            col = col * 26 + (ch - 'A' + 1);
        }
        col -= 1;
        int row = Integer.parseInt(rowPart) - 1;
        return new Point(row, col);
    }
    public static String toString(int row, int col) {
        StringBuilder sb = new StringBuilder();
        for (int i = col; i >= 0; i = i/26 - 1) {
            sb.append((char)('A' + (i % 26)));
        }
        return sb.reverse().toString() + (row + 1);
    }
}
