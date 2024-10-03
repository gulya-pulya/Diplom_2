package ru.yandex.practicum;

import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.response.ValidatableResponse;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import ru.yandex.practicum.dto.request.OrderCreateRequest;
import ru.yandex.practicum.dto.request.UserCreateRequest;
import ru.yandex.practicum.dto.request.UserLoginRequest;
import ru.yandex.practicum.dto.response.GetIngredientsResponse;
import ru.yandex.practicum.dto.response.OrderCreateResponse;
import ru.yandex.practicum.dto.response.UserLoginResponse;
import ru.yandex.practicum.util.IngredientUtils;
import ru.yandex.practicum.util.OrderUtils;
import ru.yandex.practicum.util.UserUtils;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;

public class UserOrderGetApiTest {

    @Before
    public void setUp() {
        RestAssured.baseURI = "https://stellarburgers.nomoreparties.site";
    }

    @Test
    @DisplayName("Get authorized user orders should return ok")
    public void getAuthorizedUserOrdersShouldReturnOk() {
        String email = UUID.randomUUID() + "@ya.ru";
        String password = "Qwerty231";
        UserCreateRequest userCreateRequest = new UserCreateRequest(email, password, "test");
        UserLoginRequest userLoginRequest = new UserLoginRequest(email, password);
        UserUtils.createUser(userCreateRequest);
        UserLoginResponse userLoginResponse = UserUtils.loginUser(userLoginRequest);

        OrderCreateResponse order = OrderUtils.createOrder(userLoginResponse);

        ValidatableResponse response = getUserOrdersWithAuthorized(userLoginResponse);
        checkSuccessfulUserOrderGetResponseFieldsAndStatus(order.getOrder().getNumber(), response);

        UserUtils.deleteUser(userCreateRequest);
    }

    @Test
    @DisplayName("Get not authorized user orders should return error")
    public void getNotAuthorizedUserOrdersShouldReturnError() {
        String email = UUID.randomUUID() + "@ya.ru";
        String password = "Qwerty231";
        UserCreateRequest userCreateRequest = new UserCreateRequest(email, password, "test");
        UserLoginRequest userLoginRequest = new UserLoginRequest(email, password);
        UserUtils.createUser(userCreateRequest);
        UserLoginResponse userLoginResponse = UserUtils.loginUser(userLoginRequest);

        OrderCreateResponse order = OrderUtils.createOrder(userLoginResponse);

        ValidatableResponse response = getUserOrdersWithoutAuthorized();
        checkFailedUserOrdersGetResponseFieldsAndStatus(response, 401, "You should be authorised");

        UserUtils.deleteUser(userCreateRequest);
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
}
