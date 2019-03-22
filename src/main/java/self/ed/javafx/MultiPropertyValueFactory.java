package self.ed.javafx;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

import java.util.List;
import java.util.function.Function;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

public class MultiPropertyValueFactory<S, T> implements Callback<TableColumn.CellDataFeatures<S, List<T>>, ObservableValue<List<T>>> {
    private List<PropertyValueFactory<S, T>> factories;

    public MultiPropertyValueFactory(String... properties) {
        factories = stream(properties)
                .map((Function<String, PropertyValueFactory<S, T>>) PropertyValueFactory::new)
                .collect(toList());
    }

    @Override
    public ObservableValue<List<T>> call(TableColumn.CellDataFeatures<S, List<T>> param) {
        List<T> values = factories.stream()
                // It works because param.getValue() is the only thing factory.call uses internally
                .map(factory -> factory.call((TableColumn.CellDataFeatures<S, T>)param))
                .map(ObservableValue::getValue)
                .collect(toList());
        return new SimpleObjectProperty<>(values);
    }
}
