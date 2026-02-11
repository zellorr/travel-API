package com.travelapi.repository;

import java.util.List;
import java.util.Optional;

public interface CrudRepository<T, ID> {

    T create(T entity);

    List<T> findAll();

    Optional<T> findById(ID id);

    T update(ID id, T entity);

    void delete(ID id);
}
