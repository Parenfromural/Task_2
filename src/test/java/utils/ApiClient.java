package utils;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;

public class ApiClient {
    public static void init() {
        RestAssured.baseURI = System.getProperty("api.baseUri", ApiConfig.getBaseUri());
        RestAssured.requestSpecification = new RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .build();
    }
}