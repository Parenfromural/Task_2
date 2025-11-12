package tests;

import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class AuthRegisterTest extends BaseAuthTest {

    @Test
    @DisplayName("Создание уникального пользователя — успешное создание (200)")
    public void createUniqueUser_success() {
        String newEmail = faker.internet().emailAddress();
        String newPassword = faker.internet().password(8, 16, true, true, true);
        String newName = faker.name().firstName();

        Response response = apiHelper.registerUser(newEmail, newPassword, newName);
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
        apiHelper.registerUser(email, password, name);

        Response response = apiHelper.registerUser(email, password, name);

        response.then().log().all()
                .statusCode(403)
                .body("success", equalTo(false))
                .body("message", containsString("User already exists"));
    }

    @Test
    @DisplayName("Создание пользователя без обязательного поля — ошибка (403 или 400)")
    public void createUser_missingField_shouldFail() {
        String email = faker.internet().emailAddress();
        String password = faker.internet().password(8, 16);

        Map<String, Object> body = new HashMap<>();
        body.put("email", email);
        body.put("password", password);
        Response response = apiHelper.registerUserPartial(body);

        response.then().log().all()
                .statusCode(anyOf(is(400), is(403)))
                .body("success", equalTo(false))
                .body("message", anyOf(
                        containsString("Email, password and name are required"),
                        containsString("required fields")
                ));
    }
}