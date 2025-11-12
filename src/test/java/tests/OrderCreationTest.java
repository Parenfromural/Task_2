package tests;

import io.qameta.allure.junit4.DisplayName;
import org.junit.Test;

import static org.hamcrest.Matchers.*;

public class OrderCreationTest extends BaseAuthTest {

    // Это api содержит дефект - при отправке валидных ингредиентов без хедера авторизации запрос все равно проходит
    // По этой причине тест падает, считаю некорректным выставлять в ожидаемых 400/200
    @Test
    @DisplayName("Создание заказа без авторизации с валидными ингредиентами")
    public void createOrderWithoutAuthWithIngredients_shouldFailUnauthorized() {
        String[] ingredients = apiHelper.getValidIngredients();

        apiHelper.createOrderWithoutAuth(ingredients)
                .then()
                .log().all()
                .statusCode(401)
                .body("success", equalTo(false))
                .body("message", containsString("You should be authorised"));
    }

    @Test
    @DisplayName("Создание заказа c авторизацией и валидными ингридиентами")
    public void createOrderWithoutAuthWithIngredients_success() {
        String[] ingredients = apiHelper.getValidIngredients();
        apiHelper.createOrder(ingredients, accessToken)
                .then()
                .log().all()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("order.number", notNullValue())
                .body("name", notNullValue());
    }
    @Test
    @DisplayName("Создание заказа без ингредиентов")
    public void createOrderWithoutAuthWithoutIngredients_shouldFailUnauthorized() {
        apiHelper.createOrder(new String[0], accessToken)
                .then()
                .log().all()
                .statusCode(400)
                .body("success", equalTo(false))
                .body("message", containsString("Ingredient ids must be provided"));
    }

    @Test
    @DisplayName("Создание заказа с авторизацией с неверным хешем ингредиентов")
    public void createOrderAuthorizedWithInvalidIngredientHash_shouldFail() {
        String[] invalidIngredients = {"invalidhash123"};
        apiHelper.createOrder(invalidIngredients, accessToken)
                .then()
                .log().all()
                .statusCode(500);
    }
}