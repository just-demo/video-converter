package self.ed.javafx;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import static javafx.geometry.Pos.CENTER_RIGHT;

public class CustomFormatCellFactory<S, T> implements Callback<TableColumn<S, T>, TableCell<S, T>> {
    private List<Consumer<TableCell<S, T>>> decorators = new ArrayList<>();

    @Override
    public TableCell<S, T> call(TableColumn<S, T> param) {
        return new TableCell<S, T>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                decorators.forEach(decorator -> decorator.accept(this));
            }
        };
    }

    public static <S, T> CustomFormatCellFactory<S, T> format(Function<T, String> formatter) {
        return decorate(new CustomFormatCellFactory<>(), cell -> cell.setText(cell.isEmpty() ? null : formatter.apply(cell.getItem())));
    }

    public static <S, T> CustomFormatCellFactory<S, T> alignRight(CustomFormatCellFactory<S, T> delegate) {
        return decorate(delegate, cell -> cell.setAlignment(CENTER_RIGHT));
    }

    public static <S, T> CustomFormatCellFactory<S, T> decorate(CustomFormatCellFactory<S, T> delegate, Consumer<TableCell<S, T>> decorator) {
        delegate.decorators.add(decorator);
        return delegate;
    }
}