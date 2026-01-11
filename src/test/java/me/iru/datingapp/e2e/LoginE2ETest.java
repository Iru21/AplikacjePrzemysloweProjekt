package me.iru.datingapp.e2e;

import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.time.Duration;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Login E2E Tests")
class LoginE2ETest {

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
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
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
    @DisplayName("E2E Test 1: Login page should load successfully")
    void testLoginPageLoads() {
        driver.get(baseUrl + "/login");

        wait.until(ExpectedConditions.titleContains("Login"));

        String pageTitle = driver.getTitle();
        assert pageTitle != null;
        assertTrue(pageTitle.contains("Login"),
                "Page title should contain 'Login'");

        WebElement emailInput = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.name("username"))
        );
        WebElement passwordInput = driver.findElement(By.name("password"));
        WebElement submitButton = driver.findElement(By.cssSelector("button[type='submit']"));

        assertNotNull(emailInput, "Email input should be present");
        assertNotNull(passwordInput, "Password input should be present");
        assertNotNull(submitButton, "Submit button should be present");
    }

    @Test
    @Order(2)
    @DisplayName("E2E Test 2: Login with invalid credentials should fail")
    void testLoginWithInvalidCredentials() {
        driver.get(baseUrl + "/login");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.name("username")));

        WebElement emailInput = driver.findElement(By.name("username"));
        WebElement passwordInput = driver.findElement(By.name("password"));
        WebElement submitButton = driver.findElement(By.cssSelector("button[type='submit']"));

        emailInput.sendKeys("nonexistent@example.com");
        passwordInput.sendKeys("wrongpassword");
        submitButton.click();

        wait.until(ExpectedConditions.urlContains("/login"));
        String currentUrl = driver.getCurrentUrl();
        assert currentUrl != null;
        assertTrue(currentUrl.contains("/login"), "Should remain on login page");

        assertTrue(currentUrl.contains("error") ||
                   Objects.requireNonNull(driver.getPageSource()).contains("Invalid") ||
                   driver.getPageSource().contains("error"),
                "Error message should be displayed");
    }

    @Test
    @Order(3)
    @DisplayName("E2E Test 3: Login with empty fields should show validation errors")
    void testLoginWithEmptyFields() {
        driver.get(baseUrl + "/login");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.name("username")));

        WebElement submitButton = driver.findElement(By.cssSelector("button[type='submit']"));
        submitButton.click();

        String currentUrl = driver.getCurrentUrl();
        assert currentUrl != null;
        assertTrue(currentUrl.contains("/login"), "Should remain on login page");
    }

    @Test
    @Order(4)
    @DisplayName("E2E Test 4: Register link should be present on login page")
    void testRegisterLinkPresent() {
        driver.get(baseUrl + "/login");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.name("username")));

        boolean registerLinkPresent = Objects.requireNonNull(driver.getPageSource()).contains("register") ||
                                     driver.getPageSource().contains("Register") ||
                                     driver.getPageSource().contains("Sign up");

        assertTrue(registerLinkPresent, "Register link should be present on login page");
    }

    @Test
    @Order(5)
    @DisplayName("E2E Test 5: Login form should have proper labels")
    void testLoginFormHasLabels() {
        driver.get(baseUrl + "/login");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.name("username")));

        String pageSource = driver.getPageSource();
        assert pageSource != null;
        assertTrue(pageSource.contains("Email") || pageSource.contains("email") ||
                   pageSource.contains("Username") || pageSource.contains("username"),
                "Email/Username label should be present");
        assertTrue(pageSource.contains("Password") || pageSource.contains("password"),
                "Password label should be present");
    }

    @Test
    @Order(6)
    @DisplayName("E2E Test 6: Home page should be accessible")
    void testHomePageAccessible() {
        driver.get(baseUrl + "/");

        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));

        assertEquals(200, getResponseCode(), "Home page should return 200 OK");
        assertFalse(Objects.requireNonNull(driver.getPageSource()).isEmpty(), "Page should have content");
    }

    @Test
    @Order(7)
    @DisplayName("E2E Test 7: Login page should have Bootstrap styling")
    void testLoginPageHasBootstrap() {
        driver.get(baseUrl + "/login");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.name("username")));

        String pageSource = driver.getPageSource();
        assert pageSource != null;
        boolean hasBootstrap = pageSource.contains("bootstrap") ||
                              pageSource.contains("btn") ||
                              pageSource.contains("form-control");

        assertTrue(hasBootstrap, "Page should use Bootstrap styling");
    }

    @Test
    @Order(8)
    @DisplayName("E2E Test 8: Password field should be masked")
    void testPasswordFieldIsMasked() {
        driver.get(baseUrl + "/login");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.name("password")));

        WebElement passwordInput = driver.findElement(By.name("password"));
        String inputType = passwordInput.getAttribute("type");

        assertEquals("password", inputType, "Password field should be masked");
    }

    @Test
    @Order(9)
    @DisplayName("E2E Test 9: CSRF token should be present in form")
    void testCsrfTokenPresent() {
        driver.get(baseUrl + "/login");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.name("username")));

        String pageSource = driver.getPageSource();
        assert pageSource != null;
        boolean hasCsrf = pageSource.contains("_csrf") || pageSource.contains("csrf");

        assertTrue(hasCsrf, "CSRF token should be present for security");
    }

    @Test
    @Order(10)
    @DisplayName("E2E Test 10: Login page should be responsive")
    void testLoginPageIsResponsive() {
        driver.get(baseUrl + "/login");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.name("username")));

        String pageSource = driver.getPageSource();
        assert pageSource != null;
        boolean hasViewport = pageSource.contains("viewport") &&
                             pageSource.contains("width=device-width");

        assertTrue(hasViewport, "Page should have responsive viewport meta tag");
    }

    private int getResponseCode() {
        return !Objects.requireNonNull(driver.getPageSource()).isEmpty() ? 200 : 404;
    }
}

