package edu.destination.interfaces;

import java.sql.SQLException;
import java.util.List;

public interface IService<T> {
    void addEntity(T t) throws SQLException;
    void addEntity2(T t) throws SQLException;
    void deleteEntity(T t);
    void update(int id, T t);
    List<T> getData();
}
