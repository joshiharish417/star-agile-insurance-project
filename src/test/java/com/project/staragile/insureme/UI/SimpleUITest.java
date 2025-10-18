package com.project.staragile.insureme.UI;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import java.net.URL;
import org.openqa.selenium.remote.RemoteWebDriver;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class SimpleUITest {

    private static WebDriver driver;
    private static final String BASE_URL = System.getenv().getOrDefault("TEST_ENV_URL", "http://localhost:8081");

    @BeforeAll
    public static void setup() throws Exception {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless"); // optional
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");

        driver = new RemoteWebDriver(new URL("http://localhost:4444/wd/hub"), options);
}
    @Test
    public void testOpenHomePage() {
        driver.get(BASE_URL + "/");
        String title = driver.getTitle();
        System.out.println("Page title: " + title);
        assertFalse(title.isEmpty(), "Page title should not be empty");
    }

    @AfterAll
    public static void tearDown() {
        if (driver != null) {
            driver.quit();
            System.out.println("Browser closed.");
        }
    }
}
