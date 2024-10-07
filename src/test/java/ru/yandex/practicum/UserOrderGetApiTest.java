package ru.yandex.practicum;

import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.response.ValidatableResponse;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ru.yandex.practicum.dto.request.UserCreateRequest;
import ru.yandex.practicum.dto.request.UserLoginRequest;
import ru.yandex.practicum.dto.response.OrderCreateResponse;
import ru.yandex.practicum.dto.response.UserLoginResponse;
import ru.yandex.practicum.util.OrderUtils;
import ru.yandex.practicum.util.UserUtils;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;

public class UserOrderGetApiTest {

    private final String email = UUID.randomUUID() + "@ya.ru";
    private final String password = "Qwerty231";

    @Before
    public void setUp() {
        RestAssured.baseURI = "https://stellarburgers.nomoreparties.site";
        UserCreateRequest userCreateRequest = new UserCreateRequest(email, password, "test");
        UserUtils.createUser(userCreateRequest);
    }

    @Test
    @DisplayName("Get authorized user orders should return ok")
    public void getAuthorizedUserOrdersShouldReturnOk() {
        UserLoginRequest userLoginRequest = new UserLoginRequest(email, password);
        UserLoginResponse userLoginResponse = UserUtils.loginUser(userLoginRequest);

        OrderCreateResponse order = OrderUtils.createOrder(userLoginResponse);

        ValidatableResponse response = getUserOrdersWithAuthorized(userLoginResponse);
        checkSuccessfulUserOrderGetResponseFieldsAndStatus(order.getOrder().getNumber(), response);
    }

    @Test
    @DisplayName("Get not authorized user orders should return error")
    public void getNotAuthorizedUserOrdersShouldReturnError() {
        UserLoginRequest userLoginRequest = new UserLoginRequest(email, password);
        UserLoginResponse userLoginResponse = UserUtils.loginUser(userLoginRequest);

        OrderUtils.createOrder(userLoginResponse);

        ValidatableResponse response = getUserOrdersWithoutAuthorized();
        checkFailedUserOrdersGetResponseFieldsAndStatus(response, 401, "You should be authorised");
    }

    @Step("Check response fields and status of successfully get user orders")
    private void checkSuccessfulUserOrderGetResponseFieldsAndStatus(int number, ValidatableResponse response) {
        response.assertThat()
                .body("success", CoreMatchers.equalTo(true))
                .body("orders[0].number", CoreMatchers.equalTo(number))
                .and()
                .statusCode(200);
    }

    @Step("Get user orders with authorization")
    private ValidatableResponse getUserOrdersWithAuthorized(UserLoginResponse userLoginResponse) {
        return given()
                .header("Content-type", "application/json")
                .header("Authorization", userLoginResponse.getAccessToken())
                .when()
                .get("/api/orders")
                .then();
    }

    @Step("Get user orders without authorization")
    private ValidatableResponse getUserOrdersWithoutAuthorized() {
        return given()
                .header("Content-type", "application/json")
                .when()
                .get("/api/orders")
                .then();
    }

    @Step("Check response fields and status of failed user orders get")
    private static void checkFailedUserOrdersGetResponseFieldsAndStatus(ValidatableResponse response, int code, String message) {
        response.assertThat()
                .body("success", equalTo(false))
                .and()
                .body("message", equalTo(message))
                .and()
                .statusCode(code);
    }

    @After
    public void clean() {
        UserCreateRequest userCreateRequest = new UserCreateRequest(email, password, "test");
        UserUtils.deleteUser(userCreateRequest);
    }
}
