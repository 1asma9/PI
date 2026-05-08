package hebergement.interfaces;

import java.sql.SQLException;
import java.util.List;

public interface Iservice<T> {
    void addEntity(T t) throws SQLException;   // version officielle
    void update(int id, T t) throws SQLException;
    void deleteEntity(T t) throws SQLException;
    List<T> getData() throws SQLException;
}
