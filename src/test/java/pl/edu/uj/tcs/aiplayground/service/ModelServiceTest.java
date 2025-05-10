package pl.edu.uj.tcs.aiplayground.service;

import org.jooq.JSONB;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.edu.uj.tcs.aiplayground.dto.ModelDto;
import pl.edu.uj.tcs.aiplayground.exception.DatabaseException;
import pl.edu.uj.tcs.aiplayground.exception.ModelModificationException;
import pl.edu.uj.tcs.aiplayground.form.ModelForm;
import pl.edu.uj.tcs.aiplayground.repository.IModelRepository;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ModelServiceTest {

    private IModelRepository repo;
    private ModelService service;

    private final UUID userId = UUID.randomUUID();
    private final UUID modelId = UUID.randomUUID();
    private final UUID modelVersionId = UUID.randomUUID();
    private final String modelName = "TestModel";
    private final Integer versionNumber = 1;
    private final JSONB architecture = JSONB.valueOf("{\"layers\":[]}");
    private final ModelDto dto = new ModelDto(
            modelId, userId, modelVersionId, modelName, versionNumber, architecture
    );
    private final ModelForm form = new ModelForm(userId, modelName, architecture);

    @BeforeEach
    void setUp() {
        repo = mock(IModelRepository.class);
        service = new ModelService(repo);
    }

    @Test
    void getUserModelNamesShouldCallRepository() throws DatabaseException {
        List<String> names = List.of("A", "B");
        when(repo.getUserModelNames(userId)).thenReturn(names);

        List<String> result = service.getUserModelNames(userId);

        assertEquals(names, result);
        verify(repo).getUserModelNames(userId);
    }

    @Test
    void getUserModelNamesShouldThrowDatabaseException() {
        when(repo.getUserModelNames(userId)).thenThrow(new RuntimeException("db"));

        DatabaseException ex = assertThrows(DatabaseException.class,
                () -> service.getUserModelNames(userId));

        assertInstanceOf(RuntimeException.class, ex.getCause());
    }

    @Test
    void getModelRecentShouldCallRepository() throws DatabaseException {
        when(repo.getRecentModel(userId, modelName)).thenReturn(dto);

        ModelDto result = service.getModel(userId, modelName);

        assertEquals(dto, result);
        verify(repo).getRecentModel(userId, modelName);
    }

    @Test
    void getModelShouldCallRepository() throws DatabaseException {
        when(repo.getModel(userId, modelName, versionNumber)).thenReturn(dto);

        ModelDto result = service.getModel(userId, modelName, versionNumber);

        assertEquals(dto, result);
        verify(repo).getModel(userId, modelName, versionNumber);
    }

    @Test
    void getModelShouldThrowDatabaseException() {
        when(repo.getModel(userId, modelName, versionNumber))
                .thenThrow(new NullPointerException("db"));

        DatabaseException ex = assertThrows(DatabaseException.class,
                () -> service.getModel(userId, modelName, versionNumber));

        assertInstanceOf(NullPointerException.class, ex.getCause());
    }

    @Test
    void addModelShouldCallRepository() throws Exception {
        when(repo.insertModel(form)).thenReturn(dto);

        ModelDto result = service.addModel(form);

        assertEquals(dto, result);
        verify(repo).insertModel(form);
    }

    @Test
    void addModelShouldThrowWhenValidationFail() {
        ModelForm bad = new ModelForm(userId, "", architecture);
        assertThrows(ModelModificationException.class,
                () -> service.addModel(bad));
        verifyNoInteractions(repo);
    }

    @Test
    void addModelShouldThrowDatabaseException() {
        when(repo.insertModel(form)).thenThrow(new RuntimeException("db"));

        DatabaseException ex = assertThrows(DatabaseException.class,
                () -> service.addModel(form));

        assertInstanceOf(RuntimeException.class, ex.getCause());
    }

    @Test
    void updateModelShouldCallRepository() throws Exception {
        when(repo.insertModelVersion(form)).thenReturn(dto);

        ModelDto result = service.updateModel(form);

        assertEquals(dto, result);
        verify(repo).insertModelVersion(form);
    }

    @Test
    void updateModelShouldThrowDatabaseException() {
        when(repo.insertModelVersion(form)).thenThrow(new IllegalArgumentException("bad"));

        DatabaseException ex = assertThrows(DatabaseException.class,
                () -> service.updateModel(form));

        assertInstanceOf(IllegalArgumentException.class, ex.getCause());
    }
}
