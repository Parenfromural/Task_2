package tests;

import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import utils.TestDataHelper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class AuthLoginTest {

    private String refreshToken;
    private String accessToken;
    private TestDataHelper testDataHelper;
    private String email;
    private String password;
    private String name;

    @Before
    public void setup() {
        RestAssured.baseURI = "https://stellarburgers.education-services.ru";
        testDataHelper = new TestDataHelper();

        email = uniqueEmail();
        password = "Password123";
        name = "TestLoginUser";

        Response response = testDataHelper.registerUser(email, password, name);
        response.then().statusCode(200);
        refreshToken = response.path("refreshToken");
        accessToken = response.path("accessToken");
    }

    @After
    public void tearDown() {
        if (refreshToken != null) {
            logoutUser(refreshToken);
        }
        if (accessToken != null) {
            deleteUser(accessToken);
        }
    }

    @Test
    @DisplayName("Успешный логин под существующим пользователем (200)")
    public void loginExistingUser_success() {
        Response response = loginUser(email, password);

        response.then().log().all()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("accessToken", notNullValue())
                .body("refreshToken", notNullValue());
    }


    @Test
    @DisplayName("Логин с неверными данными — ошибка (401)")
    public void loginWithWrongCredentials_shouldFail() {
        String wrongEmail = "wrong_" + uniqueEmail();
        String wrongPassword = "badPass123";

        Response response = loginUser(wrongEmail, wrongPassword);

        response.then().log().all()
                .statusCode(401)
                .body("success", equalTo(false))
                .body("message", containsString("email or password are incorrect"));
    }


    @Step("Выполнить логин пользователя {email}")
    private Response loginUser(String email, String password) {
        String body = String.format("{\"email\":\"%s\",\"password\":\"%s\"}", email, password);
        return given()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post("/api/auth/login");
    }

    @Step("Выйти из системы по refreshToken")
    private void logoutUser(String refreshToken) {
        given()
                .contentType(ContentType.JSON)
                .body("{\"token\": \"" + refreshToken + "\"}")
                .when()
                .post("/api/auth/logout")
                .then()
                .statusCode(200);
    }

    @Step("Удалить пользователя по accessToken")
    private void deleteUser(String accessToken) {
        given()
                .header("authorization", accessToken)
                .when()
                .delete("/api/auth/user")
                .then()
                .statusCode(anyOf(is(200), is(202)));
    }

    @Step("Сгенерировать уникальный email с датой и временем")
    private String uniqueEmail() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
        String formattedDateTime = now.format(formatter);
        return "user_" + formattedDateTime + "@mail.ru";
    }
}
