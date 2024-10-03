package ru.yandex.practicum.util;

import io.qameta.allure.Step;
import io.restassured.http.Header;
import io.restassured.response.ValidatableResponse;
import ru.yandex.practicum.dto.request.UserCreateRequest;
import ru.yandex.practicum.dto.request.UserLoginRequest;
import ru.yandex.practicum.dto.response.UserLoginResponse;

import static io.restassured.RestAssured.given;

public class UserUtils {

    @Step("Create new user")
    public static ValidatableResponse createUser(UserCreateRequest userCreateRequest) {
        return given()
                .header("Content-type", "application/json")
                .and()
                .body(userCreateRequest)
                .when()
                .post("/api/auth/register")
                .then();
    }

    @Step("Delete created user")
    public static void deleteUser(UserCreateRequest userCreateRequest) {
        UserLoginRequest loginUserDto = new UserLoginRequest(userCreateRequest.getEmail(), userCreateRequest.getPassword());
        UserLoginResponse userLoginResponse = loginUser(loginUserDto);

        given()
                .header("Content-type", "application/json")
                .header("Authorization", userLoginResponse.getAccessToken())
                .and()
                .body(loginUserDto)
                .when()
                .delete("/api/auth/user");
    }

    @Step("Login user to receive accessToken")
    public static UserLoginResponse loginUser(UserLoginRequest loginUserDto) {
        return given()
                .header("Content-type", "application/json")
                .and()
                .body(loginUserDto)
                .when()
                .post("/api/auth/login")
                .body()
                .as(UserLoginResponse.class);
    }
}
