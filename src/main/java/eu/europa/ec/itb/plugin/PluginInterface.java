package eu.europa.ec.itb.plugin;

import com.gitb.core.AnyContent;
import com.gitb.core.ValidationModule;
import com.gitb.tr.*;
import com.gitb.tr.ObjectFactory;
import com.gitb.vs.*;
import com.gitb.vs.Void;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.GregorianCalendar;

/**
 * This class acting as the entry point of the validator plugin.
 *
 * The key point here is the implementation of the ValidationService interface. This is what the core validator expects
 * and calls so that the plugin carries out its validations. The validation service matches the
 * <a href="https://www.itb.ec.europa.eu/docs/services/latest/validation/index.html">GITB validation service API</a>.
 *
 * In terms of inputs, these are received from the core validator as specified in the validator's README. The output is
 * a <a href="https://www.itb.ec.europa.eu/docs/services/latest/common/index.html#constructing-a-validation-report-tar">TAR validation report</a>
 * that includes the items to report.
 *
 * The current sample implementation simply checks the content to validate and reports a result based on the size of the
 * input:
 * - An error if more that 10KBs.
 * - A warning if more that 1KBs.
 * - A information message otherwise.
 */
public class PluginInterface implements ValidationService {

    protected static final String INPUT_CONTENT_TO_VALIDATE = "contentToValidate";
    private static final long ONE_KB = 1024;
    private static final long TEN_KB = ONE_KB * 10;

    private final com.gitb.tr.ObjectFactory objectFactory = new ObjectFactory();

    /**
     * This method is called by the validator to identify the plugin by means of the returned ID.
     *
     * Implementing this method is not necessary (you may simply return null), but returning an ID allows the
     * validator to reference the specific plugin if unexpected errors come up.
     *
     * @param aVoid To be provided as an instance given that no inputs are expected here.
     * @return The plugin's identification (can be null).
     */
    @Override
    public GetModuleDefinitionResponse getModuleDefinition(Void aVoid) {
        GetModuleDefinitionResponse response = new GetModuleDefinitionResponse();
        response.setModule(new ValidationModule());
        response.getModule().setId("SamplePlugin");
        return response;
    }

    /**
     * This method is the entry point for the plugin which is called by the validator.
     *
     * The method receives from the validator a standard (per validator type) set of inputs that the plugin can consider
     * when carrying out its assertions. The resulting response includes a report that in turn contains any findings that
     * need to be added to the overall validation report.
     *
     * @param request The plugin's inputs.
     * @return The plugin's contribution to the overall report.
     */
    @Override
    public ValidationResponse validate(ValidateRequest request) {
        /*
        See https://github.com/ISAITB/json-validator#plugin-development for supported inputs and general development
        tips (JSON validator taken as an example).
        */
        var inputFilePath = request.getInput().stream().filter((input) -> INPUT_CONTENT_TO_VALIDATE.equals(input.getName())).findFirst().orElseThrow(() -> new IllegalArgumentException(String.format("The [%s] input is required", INPUT_CONTENT_TO_VALIDATE))).getValue();
        var inputFile = Path.of(inputFilePath);
        var fileSize = readFileSize(inputFile);
        var report = createReport();
        int errorCount = 0, warningCount = 0, infoCount = 0;
        if (fileSize > TEN_KB) {
            var error = new BAR();
            error.setDescription("The provided content exceeded 10KB in size");
            error.setLocation(String.format("%s:%s:0", INPUT_CONTENT_TO_VALIDATE, 0));
            report.getReports().getInfoOrWarningOrError().add(objectFactory.createTestAssertionGroupReportsTypeError(error));
            errorCount += 1;
        } else if (fileSize > ONE_KB) {
            var warning = new BAR();
            warning.setDescription("The provided content exceeded 1KB in size");
            warning.setLocation(String.format("%s:%s:0", INPUT_CONTENT_TO_VALIDATE, 0));
            report.getReports().getInfoOrWarningOrError().add(objectFactory.createTestAssertionGroupReportsTypeWarning(warning));
            warningCount += 1;
        } else {
            var info = new BAR();
            info.setDescription("The provided content is less than 1KB");
            info.setLocation(String.format("%s:%s:0", INPUT_CONTENT_TO_VALIDATE, 0));
            report.getReports().getInfoOrWarningOrError().add(objectFactory.createTestAssertionGroupReportsTypeInfo(info));
            infoCount += 1;
        }
        report.getCounters().setNrOfErrors(BigInteger.valueOf(errorCount));
        report.getCounters().setNrOfWarnings(BigInteger.valueOf(warningCount));
        report.getCounters().setNrOfAssertions(BigInteger.valueOf(infoCount));
        if (errorCount > 0) {
            report.setResult(TestResultType.FAILURE);
        } else if (warningCount > 0) {
            report.setResult(TestResultType.WARNING);
        }
        var response = new ValidationResponse();
        response.setReport(report);
        return response;
    }

    /**
     * Create an empty TAR report with default values.
     *
     * @return The report instance.
     */
    private TAR createReport() {
        TAR report = new TAR();
        report.setResult(TestResultType.SUCCESS);
        report.setCounters(new ValidationCounters());
        report.setReports(new TestAssertionGroupReportsType());
        report.setContext(new AnyContent());
        try {
            report.setDate(DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar()));
        } catch (DatatypeConfigurationException e) {
            throw new IllegalStateException("Unable to construct data type factory for date", e);
        }
        return report;
    }

    /**
     * Return the size of the provided file.
     *
     * @param file The file.
     * @return The size (bytes).
     */
    private long readFileSize(Path file) {
        try {
            return Files.size(file);
        } catch (IOException e) {
            throw new IllegalStateException("Error reading file size", e);
        }
    }

}
