package ru.yandex.practicum;

import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.response.ValidatableResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ru.yandex.practicum.dto.request.UserCreateRequest;
import ru.yandex.practicum.dto.request.UserLoginRequest;
import ru.yandex.practicum.dto.response.UserLoginResponse;
import ru.yandex.practicum.util.UserUtils;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;

public class UserChangeApiTest {

    private String email = UUID.randomUUID() + "@ya.ru";
    private String password = "Qwerty231";

    @Before
    public void setUp() {
        RestAssured.baseURI = "https://stellarburgers.nomoreparties.site";
        UserCreateRequest userCreateRequest = new UserCreateRequest(email, password, "test");
        UserUtils.createUser(userCreateRequest);
    }

    @Test
    @DisplayName("Change authorized user should return ok")
    public void changeUserShouldReturnOk() {
        UserLoginRequest userLoginRequest = new UserLoginRequest(email, password);

        UserLoginResponse userLoginResponse = UserUtils.loginUser(userLoginRequest);

        email = UUID.randomUUID() + "@ya.ru";
        String newName = UUID.randomUUID().toString();
        password = UUID.randomUUID().toString();

        UserCreateRequest changeUserRequest = new UserCreateRequest(email, password, newName);
        ValidatableResponse response = changeUserDataWithAuthorization(userLoginResponse, changeUserRequest);

        checkSuccessfulUserChangeResponseFieldsAndStatus(response, changeUserRequest);
    }

    @Test
    @DisplayName("Change authorized user should return failed if user with new email already exists")
    public void changeUserShouldReturnErrorIfUserWithNewEmailAlreadyExists() {
        UserLoginRequest userLoginRequest = new UserLoginRequest(email, password);

        UserLoginResponse userLoginResponse = UserUtils.loginUser(userLoginRequest);

        String newEmail = UUID.randomUUID() + "@ya.ru";
        String newName = UUID.randomUUID().toString();
        String newPassword = UUID.randomUUID().toString();

        UserCreateRequest newUserCreateRequest = new UserCreateRequest(newEmail, password, "test");
        UserUtils.createUser(newUserCreateRequest);

        UserCreateRequest changeUserRequest = new UserCreateRequest(newEmail, newPassword, newName);
        ValidatableResponse response = changeUserDataWithAuthorization(userLoginResponse, changeUserRequest);

        checkFailedUserChangedResponseFieldsAndStatus(response, 403, "User with such email already exists");

        UserUtils.deleteUser(newUserCreateRequest);
    }

    @Test
    @DisplayName("Change not authorized user should return error")
    public void changeUserShouldReturnError() {
        String newEmail = UUID.randomUUID() + "@ya.ru";
        String newName = UUID.randomUUID().toString();
        String newPassword = UUID.randomUUID().toString();

        UserCreateRequest changeUserRequest = new UserCreateRequest(newEmail, newPassword, newName);
        ValidatableResponse response = changeUserDataWithoutAuthorization(changeUserRequest);

        checkFailedUserChangedResponseFieldsAndStatus(response, 401, "You should be authorised");
    }

    @Step("Check response fields and status of successfully changed user")
    private static void checkSuccessfulUserChangeResponseFieldsAndStatus(ValidatableResponse response, UserCreateRequest changeUserRequest) {
        response.assertThat()
                .body("success", equalTo(true))
                .body("user.email", equalTo(changeUserRequest.getEmail()))
                .body("user.name", equalTo(changeUserRequest.getName()))
                .and()
                .statusCode(200);
    }

    @Step("Check response fields and status of failed created user")
    private static void checkFailedUserChangedResponseFieldsAndStatus(ValidatableResponse response, int code, String message) {
        response.assertThat()
                .body("success", equalTo(false))
                .and()
                .body("message", equalTo(message))
                .and()
                .statusCode(code);
    }

    @Step("Change user data with authorization")
    private ValidatableResponse changeUserDataWithAuthorization(UserLoginResponse userLoginResponse, UserCreateRequest changeUserRequest) {
        return given()
                .header("Content-type", "application/json")
                .header("Authorization", userLoginResponse.getAccessToken())
                .and()
                .body(changeUserRequest)
                .when()
                .patch("/api/auth/user")
                .then();
    }

    @Step("Change user data without authorization")
    private ValidatableResponse changeUserDataWithoutAuthorization(UserCreateRequest changeUserRequest) {
        return given()
                .header("Content-type", "application/json")
                .and()
                .body(changeUserRequest)
                .when()
                .patch("/api/auth/user")
                .then();
    }

    @After
    public void clean() {
        UserCreateRequest userCreateRequest = new UserCreateRequest(email, password, "test");
        UserUtils.deleteUser(userCreateRequest);
    }
}
