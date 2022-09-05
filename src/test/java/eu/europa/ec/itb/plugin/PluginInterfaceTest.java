package eu.europa.ec.itb.plugin;

import com.gitb.core.AnyContent;
import com.gitb.tr.TestResultType;
import com.gitb.vs.ValidateRequest;
import com.gitb.vs.ValidationResponse;
import com.gitb.vs.Void;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class PluginInterfaceTest {

    @Test
    void testGetMethodDefinition() {
        var plugin = new PluginInterface();
        var definition = plugin.getModuleDefinition(new Void());
        assertNotNull(definition);
        assertNotNull(definition.getModule());
        assertNotNull(definition.getModule().getId());
    }

    @Test
    void testValidate(@TempDir File tempDirectory) throws IOException {
        var largeFile = Path.of(tempDirectory.toString(),"largeFile.json");
        var mediumFile = Path.of(tempDirectory.toString(),"mediumFile.json");
        var smallFile = Path.of(tempDirectory.toString(),"smallFile.json");
        Files.copy(Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResourceAsStream("largeFile.json")), largeFile);
        Files.copy(Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResourceAsStream("mediumFile.json")), mediumFile);
        Files.copy(Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResourceAsStream("smallFile.json")), smallFile);
        var plugin = new PluginInterface();
        testWithInput(largeFile, plugin, (response) -> {
            assertEquals(TestResultType.FAILURE, response.getReport().getResult());
            assertEquals(BigInteger.ONE, response.getReport().getCounters().getNrOfErrors());
            assertEquals(BigInteger.ZERO, response.getReport().getCounters().getNrOfWarnings());
            assertEquals(BigInteger.ZERO, response.getReport().getCounters().getNrOfAssertions());
            assertEquals("error", response.getReport().getReports().getInfoOrWarningOrError().get(0).getName().getLocalPart());
        });
        testWithInput(mediumFile, plugin, (response) -> {
            assertEquals(TestResultType.WARNING, response.getReport().getResult());
            assertEquals(BigInteger.ZERO, response.getReport().getCounters().getNrOfErrors());
            assertEquals(BigInteger.ONE, response.getReport().getCounters().getNrOfWarnings());
            assertEquals(BigInteger.ZERO, response.getReport().getCounters().getNrOfAssertions());
            assertEquals("warning", response.getReport().getReports().getInfoOrWarningOrError().get(0).getName().getLocalPart());
        });
        testWithInput(smallFile, plugin, (response) -> {
            assertEquals(TestResultType.SUCCESS, response.getReport().getResult());
            assertEquals(BigInteger.ZERO, response.getReport().getCounters().getNrOfErrors());
            assertEquals(BigInteger.ZERO, response.getReport().getCounters().getNrOfWarnings());
            assertEquals(BigInteger.ONE, response.getReport().getCounters().getNrOfAssertions());
            assertEquals("info", response.getReport().getReports().getInfoOrWarningOrError().get(0).getName().getLocalPart());
        });
    }

    private void testWithInput(Path inputFile, PluginInterface plugin, Consumer<ValidationResponse> extraAssertionProvider) {
        var request = new ValidateRequest();
        request.getInput().add(createContentInput(inputFile));
        var response = plugin.validate(request);
        assertNotNull(response);
        assertNotNull(response.getReport());
        assertNotNull(response.getReport().getCounters());
        assertNotNull(response.getReport().getReports());
        assertEquals(1, response.getReport().getReports().getInfoOrWarningOrError().size());
        extraAssertionProvider.accept(response);
    }

    private AnyContent createContentInput(Path inputFile) {
        var input = new AnyContent();
        input.setName(PluginInterface.INPUT_CONTENT_TO_VALIDATE);
        input.setValue(inputFile.toString());
        return input;
    }

}
