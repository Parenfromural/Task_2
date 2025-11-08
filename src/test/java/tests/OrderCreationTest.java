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

public class OrderCreationTest {

    private String accessToken;
    private String refreshToken;
    private TestDataHelper testDataHelper;
    private final String password = "PassPass123";
    private String email;
    private final String name = "OrderUser";

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
    @DisplayName("Создание заказа с авторизацией и валидными ингредиентами")
    public void createOrderAuthorizedWithIngredients_success() {
        String[] ingredients = getValidIngredients();

        given()
                .contentType(ContentType.JSON)
                .header("authorization", accessToken)
                .body("{\"ingredients\": " + toJsonArray(ingredients) + "}")
                .when()
                .post("/api/orders")
                .then()
                .log().all()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("order.number", notNullValue())
                .body("name", notNullValue());
    }

    @Test
    @DisplayName("Создание заказа без авторизации с валидными ингредиентами")
    public void createOrderWithoutAuthWithIngredients_success() {
        String[] ingredients = getValidIngredients();

        given()
                .contentType(ContentType.JSON)
                .body("{\"ingredients\": " + toJsonArray(ingredients) + "}")
                .when()
                .post("/api/orders")
                .then()
                .log().all()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("order.number", notNullValue())
                .body("name", notNullValue());
    }

    @Test
    @DisplayName("Создание заказа с авторизацией без ингредиентов — ошибка 400")
    public void createOrderAuthorizedWithoutIngredients_shouldFail() {
        given()
                .contentType(ContentType.JSON)
                .header("authorization", accessToken)
                .body("{\"ingredients\":[]}")
                .when()
                .post("/api/orders")
                .then()
                .log().all()
                .statusCode(400)
                .body("success", equalTo(false))
                .body("message", containsString("Ingredient ids must be provided"));
    }

    @Test
    @DisplayName("Создание заказа без авторизации и без ингредиентов — ошибка 400")
    public void createOrderWithoutAuthWithoutIngredients_shouldFail() {
        given()
                .contentType(ContentType.JSON)
                .body("{\"ingredients\":[]}")
                .when()
                .post("/api/orders")
                .then()
                .log().all()
                .statusCode(400)
                .body("success", equalTo(false))
                .body("message", containsString("Ingredient ids must be provided"));
    }

    @Test
    @DisplayName("Создание заказа с авторизацией с неверным хешем ингредиентов — ошибка 500")
    public void createOrderAuthorizedWithInvalidIngredientHash_shouldFail() {
        String[] invalidIngredients = {"invalidhash123"};

        given()
                .contentType(ContentType.JSON)
                .header("authorization", accessToken)
                .body("{\"ingredients\": " + toJsonArray(invalidIngredients) + "}")
                .when()
                .post("/api/orders")
                .then()
                .log().all()
                .statusCode(500);
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

    @Step("Получить валидные хеши ингредиентов")
    private String[] getValidIngredients() {
        Response response = given()
                .contentType(ContentType.JSON)
                .when()
                .get("/api/ingredients");

        response.then().statusCode(200);
        return response.jsonPath().getString("data._id").replace("[", "").replace("]", "").replace(" ", "").split(",");
    }

    @Step("Сгенерировать уникальный email с датой и временем")
    private String uniqueEmail() {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmssSSS");
        String formattedDateTime = now.format(formatter);
        return "user_" + formattedDateTime + "@mail.ru";
    }

    private String toJsonArray(String[] arr) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < arr.length; i++) {
            sb.append("\"").append(arr[i]).append("\"");
            if (i < arr.length - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }
}