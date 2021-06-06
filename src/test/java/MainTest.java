import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;


class MainTest extends RestAssured{

    final String getURL = "https://reqres.in/api/users";
    List<String> employeeNameList = new ArrayList<>();

    // TODO: Ok so the below code now works great, but I have to structure this better...
    //  1. Move the REST API to a Service Method and call it, the method should return the arrayList of names
    //  2. Next, fully implement Selenium -> shouldn't be that hard to do, again follow structuring


    public int getEmployeeCount() throws Exception{
        final var response = given()
                .get(getURL)
                .then()
                .contentType(ContentType.JSON)
                .extract().response();

        // Get the number of employees, and then append these employees to an arraylist
        return response.jsonPath().getInt("data.size()");
    }


    @Test
    public void getEmployeesNames() throws Exception{
        // OK! Finally something works! :)
        final var response = given()
                .get(getURL)
                .then()
                .contentType(ContentType.JSON)
                .extract().response();

        // Get the number of employees, and then append these employees to an arraylist
        int employeeCount = response.jsonPath().getInt("data.size()");
        for(int i = 0; i < employeeCount; i++){
            employeeNameList.add(response.jsonPath().getString(String.format("data[%s].first_name", i)));
        }
        // If employeeCount is <= 0, then fail the test, else open the WebDriver and start the Employee Insertion process
        if(employeeCount <= 0){
            Assertions.fail("No employees!");
        }else{
            login();
        }
    }

    // Starts WebDriver, launches URL -> Enters login information to access website
    public void login() throws Exception{
        System.setProperty("webdriver.chrome.driver", "src/main/resources/chromedriver.exe");
        String emailLoginBox = "#i0116";
        String nextLoginButton = "#idSIButton9";
        String passwordSelector = "#i0118";
        String signInLoginSelector = "#idSIButton9";
        String skipXPATH = "/html/body/div/form[1]/div/div/div[1]/div/div[3]/div/div[2]/div/div[3]/a";
        String staySignedInSelector = "#idSIButton9";
        String websiteURL = "https://trial-7v8nl0.trial.operations.dynamics.com/";

        WebDriver driver = new ChromeDriver();
        // Hmm... couldn't find a way to set this globally... Looks like it has to be defined in each individual function
        System.setProperty("webdriver.chrome.driver", "src/main/resources/chromedriver.exe");

        // Open Microsoft Login page, and wait for login element to be present
        driver.get(websiteURL);
        waitAndClick(driver, emailLoginBox);

        // Login: Insert Email
        driver.findElement(By.cssSelector(emailLoginBox)).sendKeys("ogris@paurus.si");

        // Login: Click next
        driver.findElement(By.cssSelector(nextLoginButton)).click();

        // Login: Insert Password
        waitAndClick(driver, passwordSelector);
        driver.findElement(By.cssSelector(passwordSelector)).sendKeys("8bvFcKRkMgP7Y7W");

        // Login: Clicking Sign In
        driver.findElement(By.cssSelector(signInLoginSelector)).click();

        // Login: Clicking skip for now
        driver.findElement(By.xpath(skipXPATH)).click();

        // Login: Click Yes - Stay signed in?
        // Not 100% sure if we really need a try/finally here, sometimes it asks sometimes it doesn't
        try {
            waitAndClick(driver, staySignedInSelector);
        } finally {
            navigateToEmployees(driver);
        }
    }

    // This entire expression to wait for a certain element is present is a little long, shortening it with a function...
    public void waitAndClick(WebDriver driver, String cssSelector) throws Exception{
        new WebDriverWait(driver, Duration.ofSeconds(10).getSeconds())
                .until(ExpectedConditions.elementToBeClickable(By.cssSelector(cssSelector)));
        driver.findElement(By.cssSelector(cssSelector)).click();
    }

    public void waitAndSendKeys(WebDriver driver, String cssSelector, String keys) throws Exception{
        new WebDriverWait(driver, Duration.ofSeconds(10).getSeconds())
                .until(ExpectedConditions.elementToBeClickable(By.cssSelector(cssSelector)));
        driver.findElement(By.cssSelector(cssSelector)).sendKeys(keys);
    }

    private void navigateToEmployees(WebDriver driver) throws Exception {
        waitAndClick(driver, "#DefaultDashboard_2_CompanyLogoTop > span > img");

        // navigateToEmployees: Open siderbar with shortcut combo: ALT + F1
        waitAndSendKeys(driver, "body", Keys.chord(Keys.ALT, Keys.F1));

        // navigateToEmployees: Click modules
        waitAndClick(driver, "#navPaneModuleID");

        // navgiateToEmployees: Clicking Human Resources
        waitAndClick(driver, "#mainPane > div.modulesPane > div > div.gutterList.showFullGutter > div.modulesList.modulesExpanded > a:nth-child(20)");

        // navigateToEmployees: Clickinng Human Resources -> Employees
        waitAndClick(driver, "#mainPane > div.modulesPane > div > div.modulesPane-flyout.slideInL > div > div.modulesFlyout-container > div:nth-child(9)");

        insertEmployees(driver);
    }

    private void insertEmployees(WebDriver driver) throws Exception {
        // TODO:
        //  1. Click New
        //  2. Input First name
        //  3. Input first_name
        //  4. Pick todays employement start date

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
        waitAndClick(driver, "#hcmworkerlistpage_employees_3_HRNew_Worker");

        // addEmployee: Click first_name and input the name
        new WebDriverWait(driver, Duration.ofSeconds(10).getSeconds())
                .until(ExpectedConditions.elementToBeClickable(By.xpath("/html/body/div[2]/div/div[7]/div[2]/div/div[4]/div[2]/div[1]/div[1]/div[2]/div[1]/div[1]/div[2]/div/input")));
        driver.findElement(By.xpath("/html/body/div[2]/div/div[7]/div[2]/div/div[4]/div[2]/div[1]/div[1]/div[2]/div[1]/div[1]/div[2]/div/input")).sendKeys(first_name);

        // Click calendar icon to input date
        new WebDriverWait(driver, Duration.ofSeconds(10).getSeconds())
                .until(ExpectedConditions.elementToBeClickable(By.xpath("/html/body/div[2]/div/div[7]/div[2]/div/div[4]/div[2]/div[1]/div[2]/div[2]/div[2]/div[1]/div/div[2]/div"))).click();

        // As soon as we click the calendar icon a Match Found popup shows up, we have to click cancel here
        try{
            driver.findElement(By.xpath("/html/body/div[2]/div/div[7]/div[2]/div/div[4]/div/div[2]/div[2]/button[3]")).click();
        }catch(Exception e) {
            ; //Continue with date insertion

            // Click date icon again
            new WebDriverWait(driver, Duration.ofSeconds(10)
                    .getSeconds()).until(ExpectedConditions.elementToBeClickable(By
                    .xpath("/html/body/div[2]/div/div[7]/div[2]/div/div[4]/div[2]/div[1]/div[2]/div[2]/div[2]/div[1]/div/div[2]/div")))
                    .click();

            // Select today
            new WebDriverWait(driver, Duration.ofSeconds(10)
                    .getSeconds()).until(ExpectedConditions.elementToBeClickable(By
                    .xpath("/html/body/div[9]/div[3]/button[2]")))
                    .click();
        }


        Thread.sleep(5000);
        try{
            driver.findElement(By.xpath("/html/body/div[2]/div/div[7]/div[2]/div/div[4]/div/div[2]/div[2]/button[3]")).click();
        }finally {
            ;
        }

        new WebDriverWait(driver, Duration.ofSeconds(10)
                .getSeconds()).until(ExpectedConditions.elementToBeClickable(By
                .xpath("/html/body/div[2]/div/div[7]/div[2]/div/div[4]/div[2]/div[1]/div[2]/div[2]/div[2]/div[1]/div/div[2]/div")))
                .click();

        // Select today
        new WebDriverWait(driver, Duration.ofSeconds(10)
                .getSeconds()).until(ExpectedConditions.elementToBeClickable(By
                .xpath("/html/body/div[9]/div[3]/button[2]")))
                .click();

        // Finally click hire
        waitAndClick(driver, "#HcmWorkerNewWorker_4_OkNoRedirect_label");
    }
}