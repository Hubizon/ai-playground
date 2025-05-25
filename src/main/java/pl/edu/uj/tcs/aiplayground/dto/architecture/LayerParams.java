package pl.edu.uj.tcs.aiplayground.dto.architecture;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public sealed interface LayerParams permits LinearParams, EmptyParams {
    List<String> getParamNames();

    List<Class<?>> getParamTypes();

    default List<Object> getParamValues() {
        RecordComponent[] components = this.getClass().getRecordComponents();
        List<Object> values = new ArrayList<>(components.length);

        try {
            for (RecordComponent rc : components) {
                Method accessor = rc.getAccessor();
                values.add(accessor.invoke(this));
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(); // TODO
        }

        return values;
    }

    default LayerParams updated(String name, Object value) {
        Class<?> rc = this.getClass();

        RecordComponent[] comps = rc.getRecordComponents();
        int idx = -1;
        for (int i = 0; i < comps.length; i++) {
            if (getParamNames().get(i).equals(name))
                idx = i;
        }

        try {
            Object[] args = new Object[comps.length];
            for (int i = 0; i < comps.length; i++)
                args[i] = comps[i].getAccessor().invoke(this);

            args[idx] = value;

            Constructor<?> ctor = rc.getDeclaredConstructor(
                    Arrays.stream(comps)
                            .map(RecordComponent::getType)
                            .toArray(Class[]::new)
            );
            return (LayerParams) ctor.newInstance(args);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(); // TODO
        }
    }
}