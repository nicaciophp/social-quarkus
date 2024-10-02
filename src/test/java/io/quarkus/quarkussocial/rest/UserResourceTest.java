package io.quarkus.quarkussocial.rest;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

import java.net.URL;
import java.util.List;
import java.util.Map;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import io.quarkus.quarkussocial.dto.CreateUserRequest;
import io.quarkus.quarkussocial.dto.ResponseError;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.json.bind.JsonbBuilder;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserResourceTest {

    @TestHTTPResource("/users")
    URL apiUrl;

    @Test
    @DisplayName("Should create an user successfully")
    @Order(1)
    public void createUserTest() {
        var user = new CreateUserRequest();
        user.setName("Fulano");
        user.setAge(30);

        var response = given()
                            .contentType(ContentType.JSON)
                            .body(JsonbBuilder.create().toJson(user))
                        .when()
                            .post(apiUrl)
                        .then()
                            .extract().response();
        assertEquals(201, response.statusCode());
        assertNotNull(response.jsonPath().getString("id"));

    }

    @Test
    @DisplayName("Should return error when json is not valid")
    @Order(2)
    public void createUserValidationErrorTest() {
        var user = new CreateUserRequest();
        user.setAge(null);
        user.setName(null);

        var response = given()
                    .contentType(ContentType.JSON)
                    .body(JsonbBuilder.create().toJson(user))
                .when()
                    .post(apiUrl)
                .then()
                    .extract().response();
        assertEquals(ResponseError.UNPROCESSABLE_ENTITY_STATUS, response.statusCode());
        assertEquals("Validation Error", response.jsonPath().getString("message"));

        List<Map<String, String>> errors = response.jsonPath().getList("errors");
        assertNotNull(errors.get(0).get("message"));
        assertEquals("Name is Required", errors.get(0).get("message"));
        assertEquals("Age is Required", errors.get(1).get("message"));
    }

    @Test
    @DisplayName("Sould list all users")
    @Order(3)
    public void listAllUsersTest() {
        given()
            .contentType(ContentType.JSON)
        .when()
            .get(apiUrl)
        .then()
            .statusCode(200)
            .body("size()", Matchers.is(1));
    }
}
