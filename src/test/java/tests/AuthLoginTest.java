package tests;

import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import org.junit.Test;

import static org.hamcrest.Matchers.*;

public class AuthLoginTest extends BaseAuthTest {

    @Test
    @DisplayName("Успешный логин под существующим пользователем (200)")
    public void loginExistingUser_success() {
        Response response = apiHelper.loginUser(email, password);
        response.then().log().all()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("accessToken", notNullValue())
                .body("refreshToken", notNullValue());
    }

    @Test
    @DisplayName("Логин с неверными данными — ошибка (401)")
    public void loginWithWrongCredentials_shouldFail() {
        String wrongEmail = "wrong_" + faker.internet().emailAddress();
        String wrongPassword = faker.internet().password(8, 16);

        Response response = apiHelper.loginUser(wrongEmail, wrongPassword);

        response.then().log().all()
                .statusCode(401)
                .body("success", equalTo(false))
                .body("message", containsString("email or password are incorrect"));
    }
}