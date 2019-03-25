package self.ed.javafx;

import javafx.beans.value.ObservableValue;
import javafx.beans.value.ObservableValueBase;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

public class EntireObjectCellValueFactory<T> implements Callback<TableColumn.CellDataFeatures<T, T>, ObservableValue<T>> {
    @Override
    public ObservableValue<T> call(TableColumn.CellDataFeatures<T, T> param) {
        return new ObservableValueBase<T>() {
            @Override
            public T getValue() {
                return param.getValue();
            }
        };
    }
}
