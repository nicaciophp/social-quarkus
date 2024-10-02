package io.quarkus.quarkussocial.rest;

import static io.restassured.RestAssured.given;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import io.quarkus.quarkussocial.domain.model.Follower;
import io.quarkus.quarkussocial.domain.model.User;
import io.quarkus.quarkussocial.domain.repository.FollowerRepository;
import io.quarkus.quarkussocial.domain.repository.UserRespository;
import io.quarkus.quarkussocial.dto.FollowerRequest;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.json.bind.JsonbBuilder;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import static org.junit.jupiter.api.Assertions.*;


@QuarkusTest
@TestHTTPEndpoint(FollowerResource.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FollowerResourceTest {
    @Inject
    UserRespository userRepository;

    @Inject
    FollowerRepository followerRepository;

    Long userId;
    Long followerId;

    @BeforeEach
    @Transactional
    void setUp() {
        var user = new User();
        user.setName("Fulano");
        user.setAge(30);
        userRepository.persist(user);
        userId = user.getId();

        var follower = new User();
        follower.setName("Fulano");
        follower.setAge(30);
        userRepository.persist(follower);
        followerId = follower.getId();

        var followerEntity = new Follower();
        followerEntity.setFollower(follower);
        followerEntity.setUser(user);
        followerRepository.persist(followerEntity);
    }

    @Test
    @DisplayName("sould return 409 when followerId is equal to User id")
    @Order(1)
    public void sameUserAsFollowerTest() {
        var body = new FollowerRequest();
        body.setFollowerId(userId);

        given()
            .contentType(ContentType.JSON)
            .body(JsonbBuilder.create().toJson(body))
            .pathParam("userId", userId)
        .when()
            .put()
        .then()
            .statusCode(Response.Status.CONFLICT.getStatusCode())
            .body(Matchers.is("Não pode seguir a si mesmo"));
    }

    
    @Test
    @DisplayName("sould return 404 on follow a user when User id doen't exist")
    @Order(2)
    public void userNotFoundWhenTryingToFollowTest() {
        var body = new FollowerRequest();
        body.setFollowerId(userId);

        var inexistentUserId = 999;


        given()
            .contentType(ContentType.JSON)
            .body(JsonbBuilder.create().toJson(body))
            .pathParam("userId", inexistentUserId)
        .when()
            .put()
        .then()
            .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    @DisplayName("sould follow a user")
    @Order(3)
    public void followUserTest() {
        var body = new FollowerRequest();
        body.setFollowerId(followerId);

        given()
            .contentType(ContentType.JSON)
            .body(JsonbBuilder.create().toJson(body))
            .pathParam("userId", userId)
        .when()
            .put()
        .then()
            .statusCode(Response.Status.NO_CONTENT.getStatusCode());
    }

    @Test
    @DisplayName("sould return 404 on list user followers and User id doen't exist")
    @Order(4)
    public void userNotFoundWhenListingFollowersTest() {
        var inexistentUserId = 999;

        given()
            .contentType(ContentType.JSON)
            .pathParam("userId", inexistentUserId)
        .when()
            .get()
        .then()
            .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    @DisplayName("sould list a user's follower")
    @Order(5)
    public void ListFollowersTest() {
        var response = given()
            .contentType(ContentType.JSON)
            .pathParam("userId", userId)
        .when()
            .get()
        .then()
            .extract().response();

        var followerCount = response.jsonPath().get("followersCount");
        var followerContent = response.jsonPath().getList("content");
        assertEquals(Response.Status.OK.getStatusCode(), response.statusCode());
        assertEquals(1, followerCount);
        assertEquals(1, followerContent.size());
    }

    @Test
    @DisplayName("sould return 404 on unfollow user and User id doen't exist")
    @Order(5)
    public void userNotFoundWhenUnfollowingAUserTest() {
        var inexistentUserId = 999;

        given()
            .contentType(ContentType.JSON)
            .pathParam("userId", inexistentUserId)
            .queryParam("followerId", followerId)
        .when()
            .delete()
        .then()
            .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    @DisplayName("sould Unfollow an user")
    @Order(5)
    public void unfollowUserTest() {
        given()
            .pathParam("userId", userId)
            .queryParam("followerId", followerId)
        .when()
            .delete()
        .then()
            .statusCode(Response.Status.NO_CONTENT.getStatusCode());
    }
}
