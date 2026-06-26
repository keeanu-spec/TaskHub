package org.example.repository.json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import java.util.function.Function;
import java.util.List;
import java.nio.file.Files;
import java.nio.file.Path;
import com.google.gson.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.example.repository.Repository;
import com.google.gson.stream.JsonWriter;
import com.google.gson.stream.JsonReader;
import java.io.IOException;
import com.google.gson.reflect.TypeToken;


public class JsonRepository<T, ID> implements Repository<T, ID> {
    private HashMap<ID,T> store =  new HashMap<>();
    private final Function<T,ID> idExtractor;
    private Path filePatch ;
    private final Class<T> type;

    public JsonRepository(Function<T,ID> idExtractor, Path filePatch,Class<T> type) {
        this.idExtractor = idExtractor;
        this.filePatch = filePatch;
        this.type = type;
       
        if (Files.exists(filePatch) == true) {
            try {
                String json = Files.readString(filePatch);
                Gson gson = buidGson();
                List<T> lista = gson.fromJson(json, TypeToken.getParameterized(List.class, type).getType());
                for(T element :  lista) {
                    store.put(idExtractor.apply(element),element);
                }
            } catch (Exception e) {
                // 
            }  
        }
    }


    public T save(T entity){
        ID id = idExtractor.apply(entity); 
        store.put(id, entity);
        Persistir();
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
        Persistir();
        
   }



   // Method perist Write and save 

   private void Persistir() {
    Gson gson = buidGson(); 
    String json = gson.toJson(store.values());
    try {
        Files.writeString(filePatch, json);
    } catch (Exception e) {
        // nothing its works . .
    }
    
    }
    private Gson buidGson() {
        return new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new TypeAdapter<LocalDate>() {
                @Override public void write(JsonWriter out, LocalDate v) throws IOException {
                    out.value(v == null ? null : v.toString());
                }
                @Override public LocalDate read(JsonReader in) throws IOException {
                    return LocalDate.parse(in.nextString());
                }
            }.nullSafe())
            .registerTypeAdapter(LocalDateTime.class, new TypeAdapter<LocalDateTime>() {
                @Override public void write(JsonWriter out, LocalDateTime v) throws IOException {
                    out.value(v == null ? null : v.toString());
                }
                @Override public LocalDateTime read(JsonReader in) throws IOException {
                    return LocalDateTime.parse(in.nextString());
                }
            }.nullSafe())
            .create();
    }

 }

