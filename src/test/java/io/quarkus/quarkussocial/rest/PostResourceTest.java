package io.quarkus.quarkussocial.rest;

import static io.restassured.RestAssured.given;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.quarkus.quarkussocial.domain.model.Follower;
import io.quarkus.quarkussocial.domain.model.Post;
import io.quarkus.quarkussocial.domain.model.User;
import io.quarkus.quarkussocial.domain.repository.FollowerRepository;
import io.quarkus.quarkussocial.domain.repository.PostRepository;
import io.quarkus.quarkussocial.domain.repository.UserRespository;
import io.quarkus.quarkussocial.dto.CreatePostRequest;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.json.bind.JsonbBuilder;
import jakarta.transaction.Transactional;

@QuarkusTest
@TestHTTPEndpoint(PostResource.class)
class PostResourceTest {

    @Inject
    UserRespository userRespository;

    @Inject
    FollowerRepository followerRespository;

    @Inject
    PostRepository postRepository;

    Long userId;
    Long userNotFollowerId;
    Long userFollowerId;

    @BeforeEach
    @Transactional
    public void setUP() {
        var user = new User();
        user.setName("Fulano");
        user.setAge(30);
        userRespository.persist(user);
        userId = user.getId();

        Post post = new Post();
        post.setText("Hellow WordPress");
        post.setUser(user);
        postRepository.persist(post);

        var userNotFollower = new User();
        userNotFollower.setName("Ciclano");
        userNotFollower.setAge(32);
        userRespository.persist(userNotFollower);
        userNotFollowerId = userNotFollower.getId();

        var userFollower = new User();
        userFollower.setName("Claciclano");
        userFollower.setAge(31);
        userRespository.persist(userFollower);
        userFollowerId = userFollower.getId();

        Follower follower = new Follower();
        follower.setUser(user);
        follower.setFollower(userFollower);
        followerRespository.persist(follower);
    }
    
    @Test
    @DisplayName("sould create a post for a user")
    public void createPostUser() {
        var postRequest = new CreatePostRequest();
        postRequest.setText("Some text");

        given()
            .contentType(ContentType.JSON)
            .body(JsonbBuilder.create().toJson(postRequest))
            .pathParam("userId", userId)
        .when()
            .post()
        .then()
            .statusCode(201);
    }

    @Test
    @DisplayName("sould return 404 when trying to make for an inexistent user")
    public void postForAnInexistentUserTest() {
        var postRequest = new CreatePostRequest();
        postRequest.setText("Some text");

        var inexistentUserId = 999;

        given()
            .contentType(ContentType.JSON)
            .body(JsonbBuilder.create().toJson(postRequest))
            .pathParam("userId", inexistentUserId)
        .when()
            .post()
        .then()
            .statusCode(404);
    }

    @Test
    @DisplayName("Should return 404 when user doesn't exist")
    public void listPostUserNotOfundTest() {
        var inexistentUserId = 999;

        given()
            .pathParam("userId", inexistentUserId)
        .when()
            .get()
        .then()
            .statusCode(404);
    }

    @Test
    @DisplayName("Should return 400 when followerId header is not present")
    public void listPostFollowerHeaderNotSendTest() {
        given()
            .pathParam("userId", userId)
        .when()
            .get()
        .then()
            .statusCode(400)
            .body(Matchers.is("Você esqueceu de informar o id do seguidor no cabeçalho"));
    }

    @Test
    @DisplayName("Should return 404 when follower isn't a follower")
    public void listPostFollowerNotFoundTest() {
        var inexistentFollowerId = 999;

        given()
        .pathParam("userId", userId)
        .header("followerId", inexistentFollowerId)
    .when()
        .get()
    .then()
        .statusCode(404)
        .body(Matchers.is("Follower inexistente"));
    }

    @Test
    @DisplayName("Should return 403 when follower isn't a folloer")
    public void listPostNotAfollowerTest() {
        given()
        .pathParam("userId", userId)
        .header("followerId", userNotFollowerId)
    .when()
        .get()
    .then()
        .statusCode(403)
        .body(Matchers.is("Você não pode ver esses posts"));
    }

    @Test
    @DisplayName("Should return posts")
    public void listPostTest() {
        given()
        .pathParam("userId", userId)
        .header("followerId", userFollowerId)
    .when()
        .get()
    .then()
        .statusCode(200)
        .body("size()", Matchers.is(1));
    }
}
