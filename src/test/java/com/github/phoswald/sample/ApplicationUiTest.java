package com.github.phoswald.sample;

import static com.microsoft.playwright.options.AriaRole.BUTTON;
import static com.microsoft.playwright.options.AriaRole.LINK;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType.LaunchOptions;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Page.GetByRoleOptions;
import com.microsoft.playwright.Playwright;

@EnabledIfSystemProperty(named = "test.ui", matches = "true")
class ApplicationUiTest {

    private static final ApplicationModule module = new TestModule();

    private final Application testee = module.getApplication();

    @BeforeEach
    void start() {
        testee.start();
    }

    @AfterEach
    void cleanup() {
        testee.stop();
    }

    @Test
    void samplePage() throws InterruptedException {
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new LaunchOptions().setHeadless(false));

            Page page = browser.newPage();
            page.navigate("http://localhost:8080/");
            assertEquals("RSTM Sample Service", page.title());

            page.getByRole(LINK, new GetByRoleOptions().setName("Sample")).click();
            page.waitForURL("**/login.html");
            assertEquals("Login", page.title());

            page.getByLabel("Username:").fill("username1");
            page.getByLabel("Password:").fill("password1");
            page.getByRole(BUTTON, new GetByRoleOptions().setName("Login")).click();
            page.waitForURL("**/");
            assertEquals("RSTM Sample Service", page.title());

            page.getByRole(LINK, new GetByRoleOptions().setName("Sample")).click();
            page.waitForURL("**/pages/sample");
            assertEquals("Sample Page", page.title());

            // assertTrue(page.getByText("APP_SAMPLE_CONFIX").isVisible());

            Thread.sleep(5*1000); // TODO (playwright) remove sleep, test more stuff
        }
    }
}
