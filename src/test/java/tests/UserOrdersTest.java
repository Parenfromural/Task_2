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

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class UserOrdersTest {

    private String accessToken;
    private String refreshToken;
    private TestDataHelper testDataHelper;
    private final String password = "PassPass123";
    private String email;
    private final String name = "OrdersUser";

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
    @DisplayName("Получение заказов конкретного пользователя с авторизацией")
    public void getUserOrdersAuthorized_success() {
        given()
                .contentType(ContentType.JSON)
                .header("authorization", accessToken)
                .when()
                .get("/api/orders")
                .then()
                .log().all()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("orders", notNullValue());
    }

    @Test
    @DisplayName("Получение заказов конкретного пользователя без авторизации")
    public void getUserOrdersUnauthorized_shouldFail() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/api/orders")
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

    private String uniqueEmail() {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmssSSS");
        String formattedDateTime = now.format(formatter);
        return "user_" + formattedDateTime + "@mail.ru";
    }
}