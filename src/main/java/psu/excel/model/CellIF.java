package psu.excel.model;

// Here, the cell's value type is Type, and every cell will use CellIF<?> as its observer type.
public interface CellIF<Type>{
    void setValue(Type value);
    Type getValue();
}
