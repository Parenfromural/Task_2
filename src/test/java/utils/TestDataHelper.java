package utils;

import io.qameta.allure.Step;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

public class TestDataHelper {
    @Step("Создать пользователя с email={email}, password={password}, name={name}")
    public Response registerUser(String email, String password, String name) {
        String body = "{\"email\":\"" + email + "\",\"password\":\"" + password + "\",\"name\":\"" + name + "\"}";
        return given()
                .header("Content-Type", "application/json")
                .body(body)
                .when()
                .post("/api/auth/register");
    }
}