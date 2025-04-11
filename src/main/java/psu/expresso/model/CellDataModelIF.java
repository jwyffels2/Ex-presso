package psu.expresso.model;

// Here, the cell's value type is Type, and every cell will use CellDataModelIF<?> as its observer type.
public interface CellDataModelIF<Type>{
    void setValue(Type value);
    Type getValue();
}
