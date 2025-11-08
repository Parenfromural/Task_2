package tests;

import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import utils.TestDataHelper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collection;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@RunWith(Parameterized.class)
public class AuthUserUpdateTest {

    private String accessToken;
    private String refreshToken;
    private TestDataHelper testDataHelper;
    private String email;
    private final String password = "PassPass123";
    private final String name = "UserForUpdate";

    private final String fieldName;
    private final String newValue;

    public AuthUserUpdateTest(String fieldName, String newValue) {
        this.fieldName = fieldName;
        this.newValue = newValue;
    }

    @Parameterized.Parameters(name = "Изменение поля {0} на значение {1}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {"email", uniqueEmailStatic()},
                {"name", "UpdatedName_" + System.currentTimeMillis()}
        });
    }

    @Before
    public void setup() {
        RestAssured.baseURI = "https://stellarburgers.education-services.ru";
        testDataHelper = new TestDataHelper();

        email = uniqueEmail();
        Response response = testDataHelper.registerUser(email, password, name);
        response.then().statusCode(200);

        accessToken = response.path("accessToken");
        refreshToken = response.path("refreshToken");
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
    @DisplayName("Изменение данных пользователя с авторизацией — успешное изменение")
    public void updateUserAuthorized_success() {
        String requestBody = "{\"" + fieldName + "\":\"" + newValue + "\"}";

        given()
                .contentType(ContentType.JSON)
                .header("authorization", accessToken)
                .body(requestBody)
                .when()
                .patch("/api/auth/user")
                .then()
                .log().all()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("user." + fieldName, equalTo(newValue));
    }

    @Test
    @DisplayName("Изменение данных пользователя без авторизации — ошибка 401")
    public void updateUserWithoutAuth_shouldFail() {
        String requestBody = "{\"" + fieldName + "\":\"" + newValue + "\"}";

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .patch("/api/auth/user")
                .then()
                .log().all()
                .statusCode(401)
                .body("success", equalTo(false))
                .body("message", containsString("You should be authorised"));
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
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmssSSS");
        String formattedDateTime = now.format(formatter);
        return "user_" + formattedDateTime + "@mail.ru";
    }

    private static String uniqueEmailStatic() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmssSSS");
        String formattedDateTime = now.format(formatter);
        return "user_" + formattedDateTime + "@mail.ru";
    }
}