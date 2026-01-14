package me.iru.datingapp.controller.api;

import me.iru.datingapp.service.ExportImportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ExportController.class)
@AutoConfigureMockMvc
class ExportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ExportImportService exportImportService;

    private byte[] testJsonData;
    private byte[] testXmlData;
    private byte[] testCsvData;

    @BeforeEach
    void setUp() {
        testJsonData = "{\"id\":1,\"email\":\"test@example.com\"}".getBytes();
        testXmlData = "<user><id>1</id><email>test@example.com</email></user>".getBytes();
        testCsvData = "id,email\n1,test@example.com".getBytes();
    }

    @Test
    void testExportUserData_Json_Success() throws Exception {
        Long userId = 1L;
        when(exportImportService.exportUserData(userId, "json")).thenReturn(testJsonData);

        mockMvc.perform(get("/api/export/profile/{userId}", userId)
                        .with(httpBasic("test@example.com", "password"))
                        .param("format", "json"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(header().string("Content-Disposition", "form-data; name=\"attachment\"; filename=\"user_1_profile.json\""))
                .andExpect(content().bytes(testJsonData));

        verify(exportImportService, times(1)).exportUserData(userId, "json");
    }

    @Test
    void testExportUserData_Xml_Success() throws Exception {
        Long userId = 1L;
        when(exportImportService.exportUserData(userId, "xml")).thenReturn(testXmlData);

        mockMvc.perform(get("/api/export/profile/{userId}", userId)
                        .with(httpBasic("test@example.com", "password"))
                        .param("format", "xml"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_XML))
                .andExpect(header().string("Content-Disposition", "form-data; name=\"attachment\"; filename=\"user_1_profile.xml\""))
                .andExpect(content().bytes(testXmlData));

        verify(exportImportService, times(1)).exportUserData(userId, "xml");
    }

    @Test
    void testExportUserData_DefaultFormat() throws Exception {
        Long userId = 1L;
        when(exportImportService.exportUserData(userId, "json")).thenReturn(testJsonData);

        mockMvc.perform(get("/api/export/profile/{userId}", userId)
                        .with(httpBasic("test@example.com", "password")))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        verify(exportImportService, times(1)).exportUserData(userId, "json");
    }

    @Test
    void testExportUserDataToCsv_Success() throws Exception {
        Long userId = 1L;
        when(exportImportService.exportToCsv(userId)).thenReturn(testCsvData);

        mockMvc.perform(get("/api/export/profile/{userId}/csv", userId)
                        .with(httpBasic("test@example.com", "password")))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/csv"))
                .andExpect(header().string("Content-Disposition", "form-data; name=\"attachment\"; filename=\"user_1_profile.csv\""))
                .andExpect(content().bytes(testCsvData));

        verify(exportImportService, times(1)).exportToCsv(userId);
    }

    @Test
    void testImportUserData_Success() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.json",
                "application/json",
                testJsonData
        );

        doNothing().when(exportImportService).importUserData(any());

        mockMvc.perform(multipart("/api/export/import/profile")
                        .file(file)
                        .with(httpBasic("test@example.com", "password")))
                .andExpect(status().isOk())
                .andExpect(content().string("User data imported successfully"));

        verify(exportImportService, times(1)).importUserData(any());
    }
}

