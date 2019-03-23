package self.ed.javafx;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

import static javafx.geometry.Pos.CENTER_RIGHT;

public interface CustomFormatCellFactory <S, T> extends Callback<TableColumn<S, T>, TableCell<S, T>> {
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

    static <S, T> Callback<TableColumn<S, T>, TableCell<S, T>> alignRight(Callback<TableColumn<S, T>, TableCell<S, T>> delegate) {
        return param -> {
            TableCell<S, T> cell = delegate.call(param);
            cell.setAlignment(CENTER_RIGHT);
            return cell;
        };
    }
}