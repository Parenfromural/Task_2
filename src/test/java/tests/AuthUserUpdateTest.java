package tests;

import com.github.javafaker.Faker;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@RunWith(Parameterized.class)
public class AuthUserUpdateTest extends BaseAuthTest {

    private final String fieldName;
    private final String newValue;

    public AuthUserUpdateTest(String fieldName, String newValue) {
        this.fieldName = fieldName;
        this.newValue = newValue;
    }

    @Parameterized.Parameters(name = "Изменение поля {0} на значение {1}")
    public static Collection<Object[]> data() {
        Faker faker = new Faker();
        return Arrays.asList(new Object[][]{
                {"email", faker.internet().emailAddress()},
                {"name", "UpdatedName_" + faker.name().firstName()}
        });
    }

    @Test
    @DisplayName("Изменение данных пользователя с авторизацией — успешное изменение")
    public void updateUserAuthorized_success() {
        String requestBody = "{\"" + fieldName + "\":\"" + newValue + "\"}";

        Response response = apiHelper.updateUser(accessToken, requestBody);
        response.then()
                .log().all()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("user." + fieldName, equalTo(newValue));
    }

    @Test
    @DisplayName("Изменение данных пользователя без авторизации — ошибка 401")
    public void updateUserWithoutAuth_shouldFail() {
        String requestBody = "{\"" + fieldName + "\":\"" + newValue + "\"}";

        Response response = apiHelper.updateUser(null, requestBody);
        response.then()
                .log().all()
                .statusCode(401)
                .body("success", equalTo(false))
                .body("message", containsString("You should be authorised"));
    }

}