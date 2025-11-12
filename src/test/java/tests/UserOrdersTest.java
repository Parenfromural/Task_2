package tests;

import io.qameta.allure.junit4.DisplayName;
import org.junit.Test;
import utils.ApiHelper;

import static org.hamcrest.Matchers.*;

public class UserOrdersTest extends BaseAuthTest {

    @Test
    @DisplayName("Получение заказов конкретного пользователя с авторизацией")
    public void getUserOrdersAuthorized_success() {
        ApiHelper apiHelper = new ApiHelper();

        apiHelper.getUserOrders(accessToken)
                .then()
                .log().all()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("orders", notNullValue());
    }

    @Test
    @DisplayName("Получение заказов конкретного пользователя без авторизации")
    public void getUserOrdersUnauthorized_shouldFail() {
        ApiHelper apiHelper = new ApiHelper();

        apiHelper.getUserOrders(null)
                .then()
                .log().all()
                .statusCode(401)
                .body("success", equalTo(false))
                .body("message", containsString("You should be authorised"));
    }
}