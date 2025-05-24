package pl.edu.uj.tcs.aiplayground.dto.architecture;

import java.util.List;

public sealed interface LayerParams permits LinearParams, EmptyParams {
    List<String> getParamNames();
    List<Class<?>> getParamTypes();
}