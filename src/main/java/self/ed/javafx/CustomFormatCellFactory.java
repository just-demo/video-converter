package self.ed.javafx;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

import java.util.function.Consumer;
import java.util.function.Function;

import static javafx.geometry.Pos.CENTER_RIGHT;

public interface CustomFormatCellFactory<S, T> extends Callback<TableColumn<S, T>, TableCell<S, T>> {
    @Override
    default TableCell<S, T> call(TableColumn<S, T> param) {
        return new TableCell<S, T>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : format(item));
            }
        };
    }

    String format(T item);

    static <S, T> Callback<TableColumn<S, T>, TableCell<S, T>> format(Function<T, String> formatter) {
        return (CustomFormatCellFactory<S, T>) formatter::apply;
    }

    static <S, T> Callback<TableColumn<S, T>, TableCell<S, T>> alignRight(Callback<TableColumn<S, T>, TableCell<S, T>> delegate) {
        return decorate(delegate, cell -> cell.setAlignment(CENTER_RIGHT));
    }

    static <S, T> Callback<TableColumn<S, T>, TableCell<S, T>> decorate(Callback<TableColumn<S, T>, TableCell<S, T>> delegate, Consumer<TableCell<S, T>> decorator) {
        return param -> {
            TableCell<S, T> cell = delegate.call(param);
            decorator.accept(cell);
            return cell;
        };
    }
}