package io.github.quarkussocial.users.rest;

import io.github.quarkussocial.domain.model.Follower;
import io.github.quarkussocial.domain.model.User;
import io.github.quarkussocial.domain.repository.FollowerRepository;
import io.github.quarkussocial.domain.repository.UserRepository;
import io.github.quarkussocial.users.rest.dto.FollowerRequest;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.core.Response;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
@TestHTTPEndpoint(FollowerResource.class)
@TestMethodOrder(MethodOrderer.class)
class FollowerResourceTest {
    @Inject
    UserRepository userRepository;
    @Inject
    FollowerRepository followerRepository;

    Long userId;
    Long followerId;

    @BeforeEach
    @Transactional
    void setUp(){
        //usuario padrão dos testes
        var user = new User();
        user.setAge(30);
        user.setName("Fulano");

        userRepository.persist(user);
        userId = user.getId();

        // o seguidor
        var follower = new User();
        follower.setAge(31);
        follower.setName("Cicrano");

        userRepository.persist(follower);
        followerId = follower.getId();

        //cria um follower
        var followerEntity = new Follower();
        followerEntity.setFollower(follower);
        followerEntity.setUser(user);
        followerRepository.persist(followerEntity);

    }

    @Test
    @DisplayName("should return 409 when follower Id is equal to User id")
    @Order(1)
    public void sameUserAsFollowerTest(){
        var body = new FollowerRequest();
        body.setFollowerId(userId);

        given()
                .contentType(ContentType.JSON)
                .body(body)
                .pathParam("userId", userId)
        .when()
                .put()
        .then()
                .statusCode(Response.Status.CONFLICT.getStatusCode())
                .body(Matchers.is("you can't follow yourself"));
    }

    @Test
    @DisplayName("should return 404 on follow a user when User id does't exist")
    @Order(2)
    public void userNotFoundWHenTryingToFollowTest(){

        var body = new FollowerRequest();
        body.setFollowerId(userId);

        var inexistentUserId = 999;

        given()
                .contentType(ContentType.JSON)
                .body(body)
                .pathParam("userId", userId)
        .when()
                .put()
        .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    @DisplayName("should follow user")
    @Order(3)
    public void followUserTest(){

        var body = new FollowerRequest();
        body.setFollowerId(followerId);

        var inexistentUserId = 999;

        given()
                .contentType(ContentType.JSON)
                .body(body)
                .pathParam("userId", userId)
        .when()
                .put()
        .then()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());
    }

    @Test
    @DisplayName("should return 404 on list user followers and a user when User id does't exist")
    @Order(4)
    public void userNotFoundWHenListingFollowersTest(){
        var inexistentUserId = 999;

        given()
                .contentType(ContentType.JSON)
                .pathParam("userId", userId)
        .when()
                .get()
        .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    @DisplayName("should list a user's followers")
    @Order(5)
    public void listFollowersTest(){
        var response =  given()
                .contentType(ContentType.JSON)
                .pathParam("userId", userId)
        .when()
                .put()
        .then()
                .extract().response();

        var followersCount = response.jsonPath().get("followersCount");
        assertEquals(Response.Status.OK.getStatusCode(), response.statusCode());
        assertEquals(1, followersCount);

    }

    @Test
    @DisplayName("should unfollow an User")
    public void unfollowUseTest(){
        given()
                .pathParam("userId", userId)
                .queryParam("followerId", followerId)
        .when()
                .delete()
        .then()
            .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }



}