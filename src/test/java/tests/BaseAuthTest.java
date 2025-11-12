package tests;

import com.github.javafaker.Faker;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import utils.ApiConfig;
import utils.ApiHelper;

public class BaseAuthTest {

    protected String refreshToken;
    protected String accessToken;
    protected ApiHelper apiHelper;
    protected String email;
    protected String password;
    protected String name;
    protected Faker faker;

    @Before
    public void setup() {
        RestAssured.baseURI = ApiConfig.getBaseUri();
        apiHelper = new ApiHelper();
        faker = new Faker();

        email = faker.internet().emailAddress();
        password = faker.internet().password(8, 16, true, true, true);
        name = faker.name().firstName();

        Response response = apiHelper.registerUser(email, password, name);
        response.then().statusCode(200);
        refreshToken = response.path("refreshToken");
        accessToken = response.path("accessToken");
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

    private void logoutUser(String token) {
        apiHelper.logoutUser(token);
    }

    private void deleteUser(String token) {
        apiHelper.deleteUser(token);
    }
}