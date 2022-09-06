package io.github.quarkussocial.users.rest;

import io.github.quarkussocial.domain.model.Follower;
import io.github.quarkussocial.domain.model.Post;
import io.github.quarkussocial.domain.model.User;
import io.github.quarkussocial.domain.repository.FollowerRepository;
import io.github.quarkussocial.domain.repository.PostRepository;
import io.github.quarkussocial.domain.repository.UserRepository;
import io.github.quarkussocial.users.rest.dto.CreatePostRequest;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.net.URL;

import static io.restassured.RestAssured.given;

@QuarkusTest
@TestHTTPEndpoint(PostResource.class)
class PostResourceTest {

    @TestHTTPResource("/users")
    URL apiURL;

    Long userID;
    Long userNotFollowerId;
    Long userFollowerId;

    @Inject
    UserRepository userRepository;
    @Inject
    FollowerRepository followerRepository;
    @Inject
    PostRepository postRepository;

    @BeforeEach
    @Transactional
    public void setUP(){
        // usuario padrão dos testes
        var user = new User();
        user.setAge(30);
        user.setName("Fulano");

        userRepository.persist(user);
        userID = user.getId();

        // cria postagem para usuario
        Post post = new Post();
        post.setText("Hello");
        post.setUser(user);
        postRepository.persist(post);

        // usuario que não segue ninguém
        var userNotFollower = new User();
        userNotFollower.setAge(33);
        userNotFollower.setName("Cicrano");

        userRepository.persist(userNotFollower);
        userNotFollowerId = userNotFollower.getId();

        // usuário seguidor
        var userFollower = new User();
        userFollower.setAge(31);
        userFollower.setName("beltrano");

        userRepository.persist(userFollower);
        userNotFollowerId = userFollower.getId();

        Follower follower = new Follower();
        follower.setUser(user);
        follower.setFollower(userFollower);
        followerRepository.persist(follower);


    }

    @Test
    @DisplayName("should create a post for a user")
    public void createPostTest(){
        var postRequest = new CreatePostRequest();
        postRequest.setText("Some text");

        given()
                .contentType(ContentType.JSON)
                .body(postRequest)
                .pathParam("userId", userID)
                .when().post().then().statusCode(201);
    }

    @Test
    @DisplayName("should return 404 when trying to make a post for an inexistent user")
    public void postForAnInexistentUserTest(){
        var postRequest = new CreatePostRequest();
        postRequest.setText("Some text");

        var inestitentUserId = 999;

        given()
                .contentType(ContentType.JSON)
                .body(postRequest)
                .pathParam("userId", inestitentUserId)
        .when()
                .post()
        .then()
                .statusCode(404);

    }

    @Test
    @DisplayName("should return 404 when user doesn't exist")
    public void listPostUserNotFoundTest(){
        var inexistentUserId = 999;

        given()
                .pathParam("userId", inexistentUserId)
        .when()
                .get()
        .then()
                .statusCode(404);
    }


    @Test
    @DisplayName("should return 400 when followerId header is not present")
    public void listPostFollowerHeaderNotSendTest(){
        given()
                .pathParam("userId", userID)
       .when()
                .get()
       .then()
                .statusCode(400)
                .body(Matchers.is("You forgot the header followerId"));
    }


    @Test
    @DisplayName("should return 400 when follower doesn't exist")
    public void listPostFollowerNotFoundTest(){
        var inexistentFollowerId = 999;

        given()
                .pathParam("userId", userID)
                .header("followerId", inexistentFollowerId)
        .when()
                .get()
        .then()
                .statusCode(400)
                .body(Matchers.is("inexistent followerId"));
    }

    @Test
    @DisplayName("should return 403 when follower isn't a follower")
    public void listPostNotAFollowerTest(){

        given()
                .pathParam("userId", userID)
                .header("followerId", userNotFollowerId)
        .when()
                .get()
        .then()
                .statusCode(403)
                .body(Matchers.is("You can't see thse posts"));
    }

    @Test
    @DisplayName("should list posts")
    public void listPostsTest(){
        given()
                .pathParam("userId", userID)
                .header("followerId", userFollowerId)
        .when()
                .get()
        .then()
                .statusCode(200)
                .body("size()", Matchers.is(0));
    }

}