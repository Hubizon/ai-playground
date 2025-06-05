package pl.edu.uj.tcs.aiplayground.dto.architecture;

import org.json.JSONObject;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.List;

public sealed interface LayerParams permits DropoutParams, EmptyParams, LeakyReLUParams, LinearParams {
    List<String> getParamNames();

    default List<Class<?>> getParamTypes() {
        List<Class<?>> types = new ArrayList<>();
        for (RecordComponent rc : this.getClass().getRecordComponents()) {
            types.add(rc.getType());
        }
        return types;
    }

    default List<Object> getParamValues() {
        RecordComponent[] components = this.getClass().getRecordComponents();
        List<Object> values = new ArrayList<>(components.length);

        try {
            for (RecordComponent rc : components) {
                Method accessor = rc.getAccessor();
                values.add(accessor.invoke(this));
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }

        return values;
    }

    default LayerParams updated(String name, Object value) {
        Class<?> rc = this.getClass();
        List<String> names = getParamNames();

        RecordComponent[] comps = rc.getRecordComponents();
        int idx = -1;
        for (int i = 0; i < comps.length; i++) {
            if (names.get(i).equals(name))
                idx = i;
        }

        try {
            Object[] args = new Object[comps.length];
            for (int i = 0; i < comps.length; i++)
                args[i] = comps[i].getAccessor().invoke(this);
            args[idx] = value;

            Constructor<?> ctor = rc.getDeclaredConstructor(getParamTypes().toArray(new Class<?>[0]));
            return (LayerParams) ctor.newInstance(args);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    default JSONObject toJson() {
        List<String> names = getParamNames();
        List<Object> values = getParamValues();
        int size = names.size();

        JSONObject json = new JSONObject();
        for (int i = 0; i < size; i++) {
            json.put(names.get(i), values.get(i));
        }
        return json;
    }

    default LayerParams loadFromJson(JSONObject json) {
        Class<?> rc = this.getClass();
        List<String> names = getParamNames();
        int size = names.size();
        Object[] args = new Object[size];
        for (int i = 0; i < size; i++) {
            args[i] = json.get(names.get(i));
        }

        try {
            Constructor<?> ctor = rc.getDeclaredConstructor(getParamTypes().toArray(new Class<?>[0]));
            return (LayerParams) ctor.newInstance(args);
        } catch (InvocationTargetException | NoSuchMethodException | InstantiationException |
                 IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}