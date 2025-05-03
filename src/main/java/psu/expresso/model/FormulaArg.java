package psu.expresso.model;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents one parsed function argument:
 *   LITERAL, CELL_REF, or CELL_RANGE.
 */
public class FormulaArg {
    public enum DataType { LITERAL, CELL_REF, CELL_RANGE }

    // ← a static reference to your spreadsheet model, set at startup
    private static SpreadsheetModel<Object> model;

    /** Must be called once at startup so asNumber()/asText() can look up cells */
    public static void init(SpreadsheetModel<Object> m) {
        model = m;
    }

    public final DataType type;
    public final Object  raw;
    public final double  literalNum;
    public final String  literalText;
    public final int     r1, c1, r2, c2;

    private FormulaArg(DataType type,
                       Object raw,
                       double literalNum,
                       String literalText,
                       int r1, int c1,
                       int r2, int c2)
    {
        this.type        = type;
        this.raw         = raw;
        this.literalNum  = literalNum;
        this.literalText = literalText;
        this.r1 = r1; this.c1 = c1;
        this.r2 = r2; this.c2 = c2;
    }

    /** Parse any raw Object into a typed FormulaArg */
    public static FormulaArg parse(Object o) {
        if (o instanceof Number n) {
            return new FormulaArg(DataType.LITERAL, o,
                    n.doubleValue(), null,
                    0, 0, 0, 0);
        }
        String s = (o == null ? "" : o.toString()).trim().toUpperCase();

        // RANGE?
        if (s.matches("[A-Z]+\\d+:[A-Z]+\\d+")) {
            String[] parts = s.split(":");
            Point p1 = CellRef.parse(parts[0]);
            Point p2 = CellRef.parse(parts[1]);
            return new FormulaArg(DataType.CELL_RANGE, o,
                    0, null,
                    p1.x, p1.y, p2.x, p2.y);
        }
        // SINGLE REF?
        if (s.matches("[A-Z]+\\d+")) {
            Point p = CellRef.parse(s);
            return new FormulaArg(DataType.CELL_REF, o,
                    0, null,
                    p.x, p.y, p.x, p.y);
        }
        // FALLBACK LITERAL TEXT OR NUMBER
        try {
            double d = Double.parseDouble(s);
            return new FormulaArg(DataType.LITERAL, o,
                    d, null,
                    0, 0, 0, 0);
        } catch (NumberFormatException ex) {
            return new FormulaArg(DataType.LITERAL, o,
                    0, s,
                    0, 0, 0, 0);
        }
    }

    /** Expand a range into individual single‐cell refs */
    public List<FormulaArg> expand() {
        List<FormulaArg> out = new ArrayList<>();
        if (type == DataType.CELL_RANGE) {
            int rmin = Math.min(r1, r2), rmax = Math.max(r1, r2);
            int cmin = Math.min(c1, c2), cmax = Math.max(c1, c2);
            for (int r = rmin; r <= rmax; r++) {
                for (int c = cmin; c <= cmax; c++) {
                    out.add(new FormulaArg(DataType.CELL_REF,
                            toRef(r, c),
                            0, null,
                            r, c, r, c));
                }
            }
        } else {
            out.add(this);
        }
        return out;
    }

    /** Return as a Number if possible, looking up displayValue for refs */
    public Number asNumber() {
        if (type == DataType.LITERAL && literalText == null) {
            return literalNum;
        }
        if (type == DataType.CELL_REF && model != null) {
            return model.getCellIfExists(r1, c1)
                    .map(CellDataModel::getDisplayValue)
                    .filter(Number.class::isInstance)
                    .map(Number.class::cast)
                    .orElse(null);
        }
        return null;
    }

    /** Return as text, for CONCAT or similar */
    public String asText() {
        if (type == DataType.LITERAL) {
            return (literalText != null)
                    ? literalText
                    : Double.toString(literalNum);
        }
        if (type == DataType.CELL_REF && model != null) {
            Object dv = model.getCellIfExists(r1, c1)
                    .map(CellDataModel::getDisplayValue)
                    .orElse(null);
            return dv == null ? "" : dv.toString();
        }
        return "";
    }

    private static String toRef(int row, int col) {
        return CellRef.toString(row, col);
    }
}
