package ru.yandex.practicum;

import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.response.ValidatableResponse;
import org.junit.Before;
import org.junit.Test;
import ru.yandex.practicum.dto.request.UserCreateRequest;
import ru.yandex.practicum.util.UserUtils;

import java.util.UUID;

import static org.hamcrest.CoreMatchers.equalTo;

public class UserCreateApiTest {

    @Before
    public void setUp() {
        RestAssured.baseURI = "https://stellarburgers.nomoreparties.site";
    }

    @Test
    @DisplayName("Create new user should return ok")
    public void createNewUserShouldReturnOk() {
        String email = UUID.randomUUID() + "@ya.ru";
        UserCreateRequest userCreateRequest = new UserCreateRequest(email, "Qwerty231", "test");

        ValidatableResponse response = UserUtils.createUser(userCreateRequest);

        checkSuccessfulUserCreateResponseFieldsAndStatus(response);

        UserUtils.deleteUser(userCreateRequest);
    }

    @Test
    @DisplayName("Create new user should not allowed create two users with same login")
    public void createNewUserShouldNotAllowedCreateTwoUsersWithSameLogin() {
        String email = UUID.randomUUID() + "@ya.ru";
        UserCreateRequest userCreateRequest1 = new UserCreateRequest(email, "Qwerty231", "test");
        UserCreateRequest userCreateRequest2 = new UserCreateRequest(email, "TestPassword", "TestName");

        UserUtils.createUser(userCreateRequest1);

        ValidatableResponse response = UserUtils.createUser(userCreateRequest2);

        checkFailedUserCreateResponseFieldsAndStatus(response, 403, "User already exists");

        UserUtils.deleteUser(userCreateRequest1);
    }

    @Test
    @DisplayName("Create new user should return error if email field is missing")
    public void createNewUserShouldReturnErrorIfEmailFieldIsMissing()  {
        UserCreateRequest userCreateRequest = new UserCreateRequest(null, "Qwerty231", "test");

        ValidatableResponse response = UserUtils.createUser(userCreateRequest);

        checkFailedUserCreateResponseFieldsAndStatus(response, 403, "Email, password and name are required fields");
    }

    @Test
    @DisplayName("Create new user should return error if password field is missing")
    public void createNewUserShouldReturnErrorIfPasswordFieldIsMissing()  {
        UserCreateRequest userCreateRequest = new UserCreateRequest(UUID.randomUUID().toString(), null, "test");

        ValidatableResponse response = UserUtils.createUser(userCreateRequest);

        checkFailedUserCreateResponseFieldsAndStatus(response, 403, "Email, password and name are required fields");
    }

    @Test
    @DisplayName("Create new user should return error if name field is missing")
    public void createNewUserShouldReturnErrorIfNamedFieldIsMissing()  {
        UserCreateRequest userCreateRequest = new UserCreateRequest(UUID.randomUUID().toString(), "Qwerty231", null);

        ValidatableResponse response = UserUtils.createUser(userCreateRequest);

        checkFailedUserCreateResponseFieldsAndStatus(response, 403, "Email, password and name are required fields");
    }

    @Step("Check response fields and status of successfully created user")
    private static void checkSuccessfulUserCreateResponseFieldsAndStatus(ValidatableResponse response) {
        response.assertThat()
                .body("success", equalTo(true))
                .and()
                .statusCode(200);
    }

    @Step("Check response fields and status of failed created user")
    private static void checkFailedUserCreateResponseFieldsAndStatus(ValidatableResponse response, int code, String message) {
        response.assertThat()
                .body("success", equalTo(false))
                .and()
                .body("message", equalTo(message))
                .and()
                .statusCode(code);
    }
}
