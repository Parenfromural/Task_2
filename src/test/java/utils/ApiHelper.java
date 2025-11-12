package utils;

import io.qameta.allure.Step;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import utils.dto.LoginRequest;
import utils.dto.LogoutRequest;
import utils.dto.User;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;

public class ApiHelper {

    private static final String REGISTER_ENDPOINT = "/api/auth/register";
    private static final String LOGOUT_ENDPOINT = "/api/auth/logout";
    private static final String DELETE_USER_ENDPOINT = "/api/auth/user";
    private static final String LOGIN_ENDPOINT = "/api/auth/login";
    private static final String UPDATE_USER_ENDPOINT = "/api/auth/user";
    private static final String INGREDIENTS_ENDPOINT = "/api/ingredients";
    private static final String ORDERS_ENDPOINT = "/api/orders";

    @Step("Создать пользователя с email={email}, password={password}, name={name}")
    public Response registerUser(String email, String password, String name) {
        User user = new User(email, password, name);
        return given()
                .header("Content-Type", "application/json")
                .body(user)
                .when()
                .post(REGISTER_ENDPOINT);
    }

    @Step("Выйти из системы по refreshToken")
    public void logoutUser(String refreshToken) {
        LogoutRequest logoutRequest = new LogoutRequest(refreshToken);
        given()
                .contentType(ContentType.JSON)
                .body(logoutRequest)
                .when()
                .post(LOGOUT_ENDPOINT)
                .then()
                .statusCode(200);
    }

    public void deleteUser(String accessToken) {
        Response response = given()
                .header("authorization", accessToken)
                .when()
                .delete(DELETE_USER_ENDPOINT);

        System.out.println("Delete user response status: " + response.getStatusCode());
        System.out.println("Delete user response body: " + response.getBody().asString());

        response.then()
                .statusCode(anyOf(is(200), is(202)));
    }

    @Step("Выполнить логин пользователя {email}")
    public Response loginUser(String email, String password) {
        LoginRequest loginRequest = new LoginRequest(email, password);
        return given()
                .contentType(ContentType.JSON)
                .body(loginRequest)
                .when()
                .post(LOGIN_ENDPOINT);
    }

    @Step("Создать пользователя с произвольным телом")
    public Response registerUserPartial(Object body) {
        return given()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post(REGISTER_ENDPOINT);
    }

    @Step("Обновить данные пользователя")
    public Response updateUser(String accessToken, Object updateBody) {
        io.restassured.specification.RequestSpecification request = given()
                .contentType(ContentType.JSON)
                .body(updateBody);

        if (accessToken != null && !accessToken.isEmpty()) {
            request.header("authorization", accessToken);
        }

        return request.when().patch(UPDATE_USER_ENDPOINT);
    }
    @Step("Получить валидные хеши ингредиентов")
    public String[] getValidIngredients() {
        Response response = given()
                .contentType(ContentType.JSON)
                .when()
                .get(INGREDIENTS_ENDPOINT);

        response.then().statusCode(200);
        return response.jsonPath().getString("data._id")
                .replace("[", "")
                .replace("]", "")
                .replace(" ", "")
                .split(",");
    }

    public Response getUserOrders(String accessToken) {
        io.restassured.specification.RequestSpecification request = RestAssured.given()
                .contentType(ContentType.JSON);

        if (accessToken != null && !accessToken.isEmpty()) {
            request.header("authorization", accessToken);
        }

        return request.when().get("/api/orders");
    }

    public Response createOrder(String[] ingredients, String accessToken) {
        io.restassured.specification.RequestSpecification request = given()
                .contentType(ContentType.JSON)
                .body("{\"ingredients\": " + toJsonArray(ingredients) + "}")
                .log().all();
        if (accessToken != null && !accessToken.isEmpty()) {
            request.header("Authorization", accessToken);
        }

        return request.when().post(ORDERS_ENDPOINT);
    }

    public Response createOrderWithoutAuth(String[] ingredients) {
        return RestAssured.given()
                .contentType(ContentType.JSON)
                .body("{\"ingredients\": " + toJsonArray(ingredients) + "}")
                .log().all()
                .when()
                .post(ORDERS_ENDPOINT);
    }
    public String toJsonArray(String[] arr) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < arr.length; i++) {
            sb.append("\"").append(arr[i]).append("\"");
            if (i < arr.length - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }
}
