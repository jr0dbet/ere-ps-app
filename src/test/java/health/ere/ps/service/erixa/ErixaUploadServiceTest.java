package health.ere.ps.service.erixa;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import health.ere.ps.config.UserConfig;
import health.ere.ps.event.erixa.ErixaEvent;
import health.ere.ps.model.erixa.PrescriptionTransferEntry;
import io.quarkus.test.InjectMock;
import io.quarkus.test.Mock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.enterprise.event.Event;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.websocket.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.Base64;
import java.util.Date;

import static org.mockito.Mockito.*;

public class ErixaUploadServiceTest {

    ErixaUploadService service;

    ErixaAPIInterface apiMock;
    UserConfig configMock;
    Event<Exception> exceptionEventMock;

    @BeforeEach
    void setup() {
        service = new ErixaUploadService();

        apiMock = mock(ErixaAPIInterface.class);
        configMock = mock(UserConfig.class);
        exceptionEventMock = mock(Event.class);

        // Assign mocks manually
        service.apiInterface = apiMock;
        service.userConfig = configMock;
        service.exceptionEvent = exceptionEventMock;
    }

    @Test
    void testGeneratePrescriptionBundle_sendToPharmacy_triggersUpload() throws IOException {
        JsonObject details = Json.createObjectBuilder()
                .add("firstName", "Max")
                .add("lastName", "Mueller")
                .add("birthday", "2000-01-01T00:00:00Z")
                .add("postcode", "12345")
                .add("street", "Teststrasse 1")
                .add("city", "Berlin")
                .add("emailAddress", "max@test.com")
                .add("insuranceType", "PRIVATE")
                .add("healthInsuranceNumber", "HIN123456")
                .add("creationDateTime", "2023-08-01T12:00:00Z")
                .add("pzn", "123456")
                .add("autIdem", false)
                .add("dosage", "1x daily")
                .add("medicineDescription", "Ibuprofen")
                .add("extraPaymentNecessary", true)
                .build();

        JsonObject payload = Json.createObjectBuilder()
                .add("document", Base64.getEncoder().encodeToString("dummy-pdf-content".getBytes()))
                .add("details", details)
                .build();

        JsonObject eventJson = Json.createObjectBuilder()
                .add("payload", payload)
                .add("processType", "SendToPharmacy")
                .build();

        ErixaEvent event = new ErixaEvent(eventJson, mock(Session.class), "msg-id-1");

        when(configMock.getErixaReceiverEmail()).thenReturn("pharmacy@example.com");
        when(apiMock.getUserDetails()).thenReturn(null); // Optional: replace with mock(UserDetails.class)

        service.generatePrescriptionBundle(event);

        verify(apiMock, times(1)).uploadToDrugstore(anyString());
    }
}
