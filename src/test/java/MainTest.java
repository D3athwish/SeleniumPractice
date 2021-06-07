import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;


class MainTest extends RestAssured{
    // Sleep has to be extended to 5 seconds - Employee forms are ignoring await...
    public final int TIME_SLEEP = 10000;
    List<String> employeeNameList = new ArrayList<>();

    @Test
    public void testing_employee_insertion() throws Exception{
        // Extract JSON to response variable
        final var response = given()
                .get("https://reqres.in/api/users")
                .then()
                .contentType(ContentType.JSON)
                .extract()
                .response();

        // Loop over all employees and add all first names to employeeNameList
        int employeeCount = response.jsonPath().getInt("data.size()");
        for(int i = 0; i < employeeCount; i++){
            employeeNameList.add(response.jsonPath().getString(String.format("data[%s].first_name", i)));
        }
        // If employeeCount is <= 0, then fail the test, else open the WebDriver and start the Employee Insertion process
        if(employeeCount <= 0){
            Assertions.fail("No employees!");
        }else{
            chromeDriverStart();
        }
    }

    // Starts Chrome WebDriver, and begins in this order: login -> navigate to employees -> inserts new employees
    public void chromeDriverStart() throws Exception{
        System.setProperty("webdriver.chrome.driver", "src/main/resources/chromedriver.exe");
        WebDriver driver = new ChromeDriver();

        // Open Microsoft Login page, and wait for login element to be present
        driver.get("https://trial-7v8nl0.trial.operations.dynamics.com/");
        waitAndFindCSS(driver, "#i0116")
                .click();

        // Login: Insert Email
        waitAndFindCSS(driver, "#i0116")
                .sendKeys("ogris@paurus.si");

        // Login: Click next
        waitAndFindCSS(driver, "#idSIButton9")
                .click();

        // Login: Insert Password
        waitAndFindCSS(driver, "#i0118")
                .click();
        waitAndFindCSS(driver, "#i0118")
                .sendKeys("8bvFcKRkMgP7Y7W");

        // Login: Clicking Sign In
        waitAndFindCSS(driver,"#idSIButton9")
                .click();

        // Login: Clicking skip for now - having issues with using selector method so using xpath
        waitAndFindXPath(driver, "/html/body/div/form[1]/div/div/div[1]/div/div[3]/div/div[2]/div/div[3]/a")
                .click();

        // Login: Click Yes - Stay signed in
        waitAndFindCSS(driver, "#idSIButton9")
                .click();

        navigateToEmployees(driver);
    }

    // After login is complete navigate to employees screen
    private void navigateToEmployees(WebDriver driver) {
        waitAndFindCSS(driver, "#DefaultDashboard_2_CompanyLogoTop > span > img");

        // navigateToEmployees: Open siderbar with shortcut combo: ALT + F1
        waitAndSendKeys(driver, "body", Keys.chord(Keys.ALT, Keys.F1));

        // navigateToEmployees: Click modules - Weird interaction on this element, do not resize screen
        waitAndFindCSS(driver, "#navPaneModuleID")
                .click();

        // navgiateToEmployees: Clicking Human Resources
        waitAndFindCSS(driver, "#mainPane > div.modulesPane > div > div.gutterList.showFullGutter" +
                " > div.modulesList.modulesExpanded > a:nth-child(20)")
                .click();

        // navigateToEmployees: Clicking Human Resources -> Employees
        waitAndFindCSS(driver, "#mainPane > div.modulesPane > div > div.modulesPane-flyout.slideInL > div" +
                " > div.modulesFlyout-container > div:nth-child(9)")
                .click();

        insertEmployees(driver);
    }

    private void insertEmployees(WebDriver driver) {
        employeeNameList.forEach(name -> {
            try {
                addEmployee(driver, name);
            } catch (Exception e) {
                System.out.println("Employee add error!");
                e.printStackTrace();
            }
        });
    }

    private void addEmployee(WebDriver driver, String first_name) throws Exception {
        System.setProperty("webdriver.chrome.driver", "src/main/resources/chromedriver.exe");

        // addEmployee: Click New
        waitAndFindXPath(driver, "/html/body/div[2]/div/div[6]/div/form[2]/div[2]/div[1]/div[3]/button[1]")
                .click();

        // Click calendar icon to input date
        waitAndFindXPath(driver, "/html/body/div[2]/div/div[7]/div[2]/div/div[4]/div[2]/div[1]/div[2]/div[2]" +
                "/div[2]/div[1]/div/div[2]/div")
                .click();

        // Click today
        waitAndFindXPath(driver, "/html/body/div[9]/div[3]/button[2]")
                .click();

        // addEmployee: Click first_name and input the name
        waitAndFindXPath(driver, "/html/body/div[2]/div/div[7]/div[2]/div/div[4]/div[2]/div[1]/div[1]/div[2]" +
                "/div[1]/div[1]/div[2]/div/input")
                .click();

        waitAndFindXPath(driver, "/html/body/div[2]/div/div[7]/div[2]/div/div[4]/div[2]/div[1]/div[1]/div[2]" +
                "/div[1]/div[1]/div[2]/div/input")
                .sendKeys(first_name);

        // Click middle name so Hire button becomes active
        waitAndFindXPath(driver, "/html/body/div[2]/div/div[7]/div[2]/div/div[4]/div[2]/div[1]/div[1]/div[2]" +
                "/div[1]/div[2]/div/input")
                .click();

        // Finally click Hire
        waitAndFindXPath(driver, "/html/body/div[2]/div/div[7]/div[2]/div/div[4]/div[2]/div[2]/div[2]/button[1]")
                .click();

        Thread.sleep(10000);

        // The idea here is to check if a cancel button is present, if it is we use the ESC key to close the menu
        // Having issues with detecting this...
        List<WebElement> cancelElement = driver
                .findElements(By.xpath("/html/body/div[2]/div/div[7]/div[2]/div/div[4]/div/div[2]/div[2]/button[3]"));
        if(cancelElement.size() > 0){
            waitAndFindCSS(driver, "#DirPartyVerification_10_DirPartyTable_Name_input").click();
            waitAndSendKeys(driver, "#DirPartyVerification_10_DirPartyTable_Name_input", Keys.chord(Keys.ESCAPE));
        }
    }

    // Wait till (CSS) element is present then continue...
    public WebElement waitAndFindCSS(WebDriver driver, String cssSelector) {
        new WebDriverWait(driver, Duration.ofSeconds(10).getSeconds())
                .until(ExpectedConditions.elementToBeClickable(By.cssSelector(cssSelector)));
        return driver.findElement(By.cssSelector(cssSelector));
    }

    // Adding Thread.sleep here, WebDriverWait doesn't seem to work consistently on Employee Forms :/
    public WebElement waitAndFindXPath(WebDriver driver, String XPath) throws InterruptedException {
        new WebDriverWait(driver, Duration.ofSeconds(10).getSeconds())
                .until(ExpectedConditions.elementToBeClickable(By.xpath(XPath)));
        Thread.sleep(TIME_SLEEP);
        return driver.findElement(By.xpath(XPath));
    }

    // Wait till element is present then continue adding keys...
    public void waitAndSendKeys(WebDriver driver, String cssSelector, String keys) {
        new WebDriverWait(driver, Duration.ofSeconds(10).getSeconds())
                .until(ExpectedConditions.elementToBeClickable(By.cssSelector(cssSelector)));
        driver.findElement(By.cssSelector(cssSelector)).sendKeys(keys);
    }
}