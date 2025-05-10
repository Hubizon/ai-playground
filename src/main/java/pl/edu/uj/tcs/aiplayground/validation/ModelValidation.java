package pl.edu.uj.tcs.aiplayground.validation;

import pl.edu.uj.tcs.aiplayground.form.ModelForm;
import pl.edu.uj.tcs.aiplayground.exception.ModelModificationException;

public class ModelValidation {
    private static final String MODEL_NAME_REGEX = "^[A-Za-z0-9_.]{3,50}$";

    public static void validateModelForm(ModelForm modelForm) throws ModelModificationException {
        if (!modelForm.name().matches(MODEL_NAME_REGEX))
            throw new ModelModificationException("Invalid model name");
    }
}
