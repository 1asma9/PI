package interfaces;

import java.sql.SQLException;
import java.util.List;

public interface IService<T> {
    void addEntity(T t) throws SQLException;

    void deleteEntity(int id) throws SQLException;

    void updateEntity(T t) throws SQLException;

    List<T> getAllEntities() throws SQLException;
}
