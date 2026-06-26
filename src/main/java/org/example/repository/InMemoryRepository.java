package org.example.repository;

import java.util.function.Function;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import java.util.List;

public class InMemoryRepository<T, ID> implements Repository<T, ID> {

    private final HashMap<ID, T> store = new HashMap<>();
    private final Function<T, ID> idExtractor;

    public InMemoryRepository(Function<T, ID> idExtractor) {
        this.idExtractor = idExtractor;
    }

    @Override
    public T save(T entity) {
        ID id = idExtractor.apply(entity);
        store.put(id, entity);
        return entity;
    }

    @Override
    public Optional<T> findById(ID id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<T> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public void deleteById(ID id) {
        store.remove(id);
    }

    public boolean existsById(ID id) {
        return store.containsKey(id);
    }
}
