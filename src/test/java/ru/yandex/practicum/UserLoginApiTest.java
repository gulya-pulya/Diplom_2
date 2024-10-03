package ru.yandex.practicum;

import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.response.ValidatableResponse;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import ru.yandex.practicum.dto.request.UserCreateRequest;
import ru.yandex.practicum.dto.request.UserLoginRequest;
import ru.yandex.practicum.util.UserUtils;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;

public class UserLoginApiTest {

    @Before
    public void setUp() {
        RestAssured.baseURI = "https://stellarburgers.nomoreparties.site";
    }

    @Test
    @DisplayName("Login user should return ok")
    public void loginUserShouldReturnOk() {
        String email = UUID.randomUUID() + "@ya.ru";
        String password = "Qwerty231";
        UserCreateRequest userCreateRequest = new UserCreateRequest(email, password, "test");
        UserLoginRequest userLoginRequest = new UserLoginRequest(email, password);

        UserUtils.createUser(userCreateRequest);

        ValidatableResponse response = tryToLogin(userLoginRequest);
        checkSuccessfulUserLoginResponseFieldsAndStatus(response);

        UserUtils.deleteUser(userCreateRequest);
    }

    @Test
    @DisplayName("Login user should return error if user password don't match")
    public void loginUserShouldReturnErrorIfUserPasswordDontMatch() {
        String email = UUID.randomUUID() + "@ya.ru";
        String password = "Qwerty231";
        UserCreateRequest userCreateRequest = new UserCreateRequest(email, password, "test");
        UserLoginRequest userLoginRequest = new UserLoginRequest(email, "wrongPassword");

        UserUtils.createUser(userCreateRequest);

        ValidatableResponse response = tryToLogin(userLoginRequest);

        checkFailedUserLoginResponseFieldsAndStatus(response, 401, "email or password are incorrect");

        UserUtils.deleteUser(userCreateRequest);
    }

    @Test
    @DisplayName("Login user should return error if user don't exists")
    public void loginUserShouldReturnErrorIfUserDontExists() {
        String login = UUID.randomUUID().toString();
        String password = "Qwerty231";
        UserLoginRequest userLoginRequest = new UserLoginRequest(login, password);

        ValidatableResponse response = tryToLogin(userLoginRequest);

        checkFailedUserLoginResponseFieldsAndStatus(response, 401, "email or password are incorrect");
    }

    @Step("Try to login")
    private ValidatableResponse tryToLogin(UserLoginRequest userLoginRequest) {
        return given()
                .header("Content-type", "application/json")
                .and()
                .body(userLoginRequest)
                .when()
                .post("/api/auth/login")
                .then();
    }

    @Step("Check response fields and status of successfully user login")
    private static void checkSuccessfulUserLoginResponseFieldsAndStatus(ValidatableResponse response) {
        response.assertThat()
                .body("success", CoreMatchers.equalTo(true))
                .body("accessToken", CoreMatchers.notNullValue())
                .and()
                .statusCode(200);
    }

    @Step("Check response fields and status of failed user login")
    private static void checkFailedUserLoginResponseFieldsAndStatus(ValidatableResponse response, int code, String message) {
        response.assertThat()
                .body("success", equalTo(false))
                .and()
                .body("message", equalTo(message))
                .and()
                .statusCode(code);
    }
}
