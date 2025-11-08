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

public class AuthRegisterTest {

    private String refreshToken;
    private String accessToken;
    private TestDataHelper testDataHelper;

    @Before
    public void setup() {
        RestAssured.baseURI = "https://stellarburgers.education-services.ru";
        testDataHelper = new TestDataHelper();
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
    @DisplayName("Создание уникального пользователя — успешное создание (200)")
    public void createUniqueUser_success() {
        String email = uniqueEmail();
        String password = "PassPassPass";
        String name = "RandomUser";

        Response response = testDataHelper.registerUser(email, password, name);
        response.then().log().all()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("accessToken", notNullValue())
                .body("refreshToken", notNullValue());

        refreshToken = response.path("refreshToken");
        accessToken = response.path("accessToken");
    }

    @Test
    @DisplayName("Создание уже зарегистрированного пользователя — ошибка (403)")
    public void createExistingUser_shouldFail() {
        String email = uniqueEmail();
        String password = "pass123";
        String name = "DuplicateUser";

        testDataHelper.registerUser(email, password, name);

        Response response = testDataHelper.registerUser(email, password, name);

        response.then().log().all()
                .statusCode(403)
                .body("success", equalTo(false))
                .body("message", containsString("User already exists"));
    }

    @Test
    @DisplayName("Создание пользователя без обязательного поля — ошибка (403 или 400)")
    public void createUser_missingField_shouldFail() {
        String email = uniqueEmail();
        String password = "pass123";

        Response response = given()
                .contentType(ContentType.JSON)
                .body("{\"email\":\"" + email + "\", \"password\":\"" + password + "\"}")
                .when()
                .post("/api/auth/register");

        response.then().log().all()
                .statusCode(anyOf(is(400), is(403)))
                .body("success", equalTo(false))
                .body("message", anyOf(
                        containsString("Email, password and name are required"),
                        containsString("required fields")
                ));
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