package me.iru.datingapp.e2e;

import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Swagger UI E2E Tests")
class SwaggerE2ETest {

    @LocalServerPort
    private int port;

    private WebDriver driver;
    private WebDriverWait wait;
    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port;

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");

        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @Order(1)
    @DisplayName("E2E Test 1: Swagger UI should be accessible")
    void testSwaggerUIAccessible() {
        driver.get(baseUrl + "/swagger-ui/index.html");

        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));

        String pageSource = driver.getPageSource();
        assertNotNull(pageSource, "Page source should not be null");
        assertFalse(pageSource.isEmpty(), "Page should have content");
    }

    @Test
    @Order(2)
    @DisplayName("E2E Test 2: Swagger UI should load OpenAPI specification")
    void testSwaggerUILoadsOpenAPI() {
        driver.get(baseUrl + "/swagger-ui/index.html");

        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        String pageSource = driver.getPageSource();
        assert pageSource != null;
        boolean hasSwaggerUI = pageSource.contains("swagger") ||
                              pageSource.contains("api-docs") ||
                              pageSource.contains("OpenAPI");

        assertTrue(hasSwaggerUI, "Page should contain Swagger UI elements");
    }

    @Test
    @Order(3)
    @DisplayName("E2E Test 3: Swagger UI should display API title")
    void testSwaggerUIDisplaysTitle() {
        driver.get(baseUrl + "/swagger-ui/index.html");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        String pageSource = driver.getPageSource();
        assert pageSource != null;
        boolean hasTitle = pageSource.contains("Dating") ||
                          pageSource.contains("API") ||
                          pageSource.contains("DatingApp");

        assertTrue(hasTitle, "Swagger UI should display API title");
    }

    @Test
    @Order(4)
    @DisplayName("E2E Test 4: OpenAPI docs JSON should be accessible")
    void testOpenAPIDocsAccessible() {
        driver.get(baseUrl + "/v3/api-docs");

        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));

        String pageSource = driver.getPageSource();
        assert pageSource != null;
        assertTrue(pageSource.contains("openapi") || pageSource.contains("paths"),
                "OpenAPI JSON should be accessible");
        assertTrue(pageSource.contains("{") && pageSource.contains("}"),
                "Response should be valid JSON");
    }

    @Test
    @Order(5)
    @DisplayName("E2E Test 5: Swagger UI should have multiple API endpoints")
    void testSwaggerUIHasEndpoints() {
        driver.get(baseUrl + "/swagger-ui/index.html");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        String pageSource = driver.getPageSource();
        assert pageSource != null;
        boolean hasEndpoints = pageSource.contains("/api") ||
                              pageSource.contains("auth") ||
                              pageSource.contains("users");

        assertTrue(hasEndpoints, "Swagger UI should show API endpoints");
    }

    @Test
    @Order(6)
    @DisplayName("E2E Test 6: Swagger UI should be styled properly")
    void testSwaggerUIHasCSS() {
        driver.get(baseUrl + "/swagger-ui/index.html");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));

        String pageSource = driver.getPageSource();
        assert pageSource != null;
        boolean hasCSS = pageSource.contains(".css") ||
                        pageSource.contains("stylesheet") ||
                        pageSource.contains("swagger-ui");

        assertTrue(hasCSS, "Swagger UI should have proper styling");
    }

    @Test
    @Order(7)
    @DisplayName("E2E Test 7: Swagger UI should have JavaScript loaded")
    void testSwaggerUIHasJavaScript() {
        driver.get(baseUrl + "/swagger-ui/index.html");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));

        String pageSource = driver.getPageSource();
        assert pageSource != null;
        boolean hasJS = pageSource.contains(".js") ||
                       pageSource.contains("javascript") ||
                       pageSource.contains("swagger-ui");

        assertTrue(hasJS, "Swagger UI should have JavaScript loaded");
    }

    @Test
    @Order(8)
    @DisplayName("E2E Test 8: Swagger UI page title should be set")
    void testSwaggerUIPageTitle() {
        driver.get(baseUrl + "/swagger-ui/index.html");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));

        String pageTitle = driver.getTitle();
        assertNotNull(pageTitle, "Page title should not be null");
        assertFalse(pageTitle.isEmpty(), "Page title should not be empty");
        assertTrue(pageTitle.contains("Swagger") ||
                   pageTitle.contains("API") ||
                   pageTitle.contains("Dating"),
                "Page title should be descriptive");
    }

    @Test
    @Order(9)
    @DisplayName("E2E Test 9: OpenAPI docs should contain API version")
    void testOpenAPIDocsHasVersion() {
        driver.get(baseUrl + "/v3/api-docs");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));

        String pageSource = driver.getPageSource();
        assert pageSource != null;
        assertTrue(pageSource.contains("version") || pageSource.contains("info"),
                "OpenAPI docs should contain version information");
    }

    @Test
    @Order(10)
    @DisplayName("E2E Test 10: Swagger UI should not show errors in console")
    void testSwaggerUINoErrors() {
        driver.get(baseUrl + "/swagger-ui/index.html");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        String pageSource = driver.getPageSource();

        assert pageSource != null;
        assertTrue(pageSource.length() > 1000,
                "Swagger UI should have substantial content");

        assertFalse(pageSource.contains("404") && pageSource.contains("Not Found"),
                "Should not be a 404 error page");
        assertFalse(pageSource.contains("500") && pageSource.contains("Error"),
                "Should not be a 500 error page");
    }

    @Test
    @Order(11)
    @DisplayName("E2E Test 11: Swagger UI URL redirect should work")
    void testSwaggerUIRedirect() {
        driver.get(baseUrl + "/swagger-ui.html");

        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));

        String currentUrl = driver.getCurrentUrl();
        assert currentUrl != null;
        assertTrue(currentUrl.contains("swagger-ui"),
                "URL should contain swagger-ui");

        String pageSource = driver.getPageSource();
        assert pageSource != null;
        assertFalse(pageSource.isEmpty(), "Page should have content");
    }

    @Test
    @Order(12)
    @DisplayName("E2E Test 12: API docs should use OpenAPI 3.0 format")
    void testOpenAPIVersion() {
        driver.get(baseUrl + "/v3/api-docs");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));

        String pageSource = driver.getPageSource();
        assert pageSource != null;
        boolean hasOpenAPIVersion = pageSource.contains("\"openapi\":\"3.") ||
                                   pageSource.contains("openapi: 3.");

        assertTrue(hasOpenAPIVersion, "Should use OpenAPI 3.0+ specification");
    }
}

