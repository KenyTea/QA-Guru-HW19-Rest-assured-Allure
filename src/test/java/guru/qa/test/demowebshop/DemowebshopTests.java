package guru.qa.test.demowebshop;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Configuration;
import guru.qa.config.demowebshop.App;
import io.qameta.allure.Story;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import org.junit.jupiter.api.*;
import org.openqa.selenium.Cookie;

import java.time.Duration;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.value;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;
import static com.codeborne.selenide.WebDriverRunner.getWebDriver;
import static guru.qa.filters.CustomLogFilter.customLogFilter;
import static io.qameta.allure.Allure.step;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

@Story("Login tests")
public class DemowebshopTests {

    // Jenkins job - QA.Guru9_HW19-Rest-assured-Allure

    @BeforeAll
    static void configureBaseUrl() {
        RestAssured.baseURI = App.config.apiUrl();
        Configuration.baseUrl = App.config.webUrl();
        Configuration.remote = App.config.remoteUrl();
    }

    @Test
    @Tag("demowebshop")
    @DisplayName("Authorization to demowebshop (API + UI)")
    void loginWithCookieTest() {

        step("Add product to cart", () -> {
                    given()
                            .filter(new AllureRestAssured())
                            .contentType("application/x-www-form-urlencoded; charset=UTF-8")
                            .body("product_attribute_74_5_26=81&product_attribute_74_6_27=83&product_attribute_74_3_28=86&addtocart_74.EnteredQuantity=1")
                            .cookie("Nop.customer=cd69ccd7-a87f-4c76-9f7a-5fe3355552e1")
                            .when()
                            .log().uri()
                            .log().body()
                            .post("/addproducttocart/details/74/1")
                            .then()
                            .log().body()
                            .statusCode(200)
                            .body("success", is(true))
                            .body("updatetopcartsectionhtml", is("(1)"))
                            .body("message", is("The product has been added to your <a href=\"/cart\">shopping cart</a>"));

                });

        step("Get cookie by api and set it to browser", () -> {
            String authorizationCookie =
                    given()
                            .filter(customLogFilter().withCustomTemplates())
                            .contentType("application/x-www-form-urlencoded; charset=UTF-8")
                            .formParam("Email", App.config.userLogin())
                            .formParam("Password", App.config.userPassword())
                            .when()
                            .log().uri()
                            .log().body()
                            .post("/login")
                            .then()
                            .log().body()
                            .statusCode(302)
                            .extract()
                            .cookie("NOPCOMMERCE.AUTH");

            step("Open minimal content, because cookie can be set when site is opened", () ->
                    open("/Themes/DefaultClean/Content/images/logo.png"));

            step("Set cookie to browser", () ->
                    getWebDriver().manage().addCookie(
                            new Cookie("NOPCOMMERCE.AUTH", authorizationCookie)));
        });

        step("Open main page", () ->
                open(""));

        step("Verify successful authorization", () ->
                $(".account").shouldHave(text(App.config.userLogin())));

        step("Check client cart", () -> {
                    $(".cart-label").click();
                    $(".page-title").shouldHave(text("Shopping cart"));
                    $(".product-unit-price").shouldHave(text("815.00"));});

        step("Check client account", () -> {
                    $(".account").click();
                    $(".page-title").should(Condition.visible, Duration.ofSeconds(30));
                    $(".page-title").shouldHave(text("My account - Customer info"));
                    $("#FirstName").shouldHave(value(App.config.FirstName()));
                    $("#LastName").shouldHave(value(App.config.LastName()));
                    $("#Email").shouldHave(value(App.config.userLogin()));
                }
        );


    }
}
