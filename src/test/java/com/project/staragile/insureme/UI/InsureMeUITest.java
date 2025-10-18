package com.project.staragile.insureme;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class InsureMeUITest {

    private static WebDriver driver;
    private static final String BASE_URL = System.getenv().getOrDefault("TEST_ENV_URL", "http://localhost:8080");

    @BeforeAll
    public static void setup() {
        System.setProperty("selenium.manager.log.level", "TRACE");
        System.setProperty("webdriver.chrome.driver", "/usr/local/bin/chromedriver");
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");
        // options.setExperimentalOption("debuggerAddress", "localhost:40321");
        // driver = new ChromeDriver(new URL("http://localhost:40321"),options);
        driver = new ChromeDriver(new URL(options);
        try {
            Files.createDirectories(Paths.get("target/screenshots"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void takeScreenshot(String testName) {
        try {
            File srcFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            File destFile = new File("target/screenshots/" + testName + ".png");
            Files.copy(srcFile.toPath(), destFile.toPath());
            System.out.println("📸 Screenshot saved: " + destFile.getAbsolutePath());
        } catch (IOException e) {
            System.out.println("⚠️ Failed to save screenshot: " + e.getMessage());
        }
    }

    @Test
    public void testHomePage() {
        driver.get(BASE_URL + "/index.html");
        String title = driver.getTitle();
        System.out.println("Opened Home Page: " + title);
        assertTrue(title != null && !title.isEmpty(), "Home page title should not be empty");
    }

    @Test
    public void testServicePage() {
        driver.get(BASE_URL + "/service.html");
        String title = driver.getTitle();
        System.out.println("Opened Service Page: " + title);
        assertTrue(driver.getPageSource().toLowerCase().contains("service"),
                "Page should contain 'Service' text");
    }

    @Test
    public void testContactPageAndForm() {
        driver.get(BASE_URL + "/contact.html");
        String title = driver.getTitle();
        System.out.println("Opened Contact Page: " + title);

        try {
            WebElement nameField = driver.findElement(By.name("name"));
            WebElement emailField = driver.findElement(By.name("email"));
            WebElement messageField = driver.findElement(By.name("message"));

            nameField.sendKeys("Harish Tester");
            emailField.sendKeys("harish@example.com");
            messageField.sendKeys("This is a Selenium headless test message.");
            messageField.submit();

            System.out.println("✅ Contact form submitted successfully (headless).");
        } catch (Exception e) {
            takeScreenshot("ContactPageFailure");
            System.out.println("⚠️ Contact form not found or could not be filled: " + e.getMessage());
        }

        assertTrue(title != null && !title.isEmpty(), "Contact page title should not be empty");
    }

    @AfterAll
    public static void tearDown() {
        if (driver != null) {
            driver.quit();
            System.out.println("Browser closed (headless).");
        }
    }
}
