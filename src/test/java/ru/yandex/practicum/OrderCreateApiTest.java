package ru.yandex.practicum;

import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.response.ValidatableResponse;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ru.yandex.practicum.dto.request.OrderCreateRequest;
import ru.yandex.practicum.dto.request.UserCreateRequest;
import ru.yandex.practicum.dto.request.UserLoginRequest;
import ru.yandex.practicum.dto.response.GetIngredientsResponse;
import ru.yandex.practicum.dto.response.UserLoginResponse;
import ru.yandex.practicum.util.IngredientUtils;
import ru.yandex.practicum.util.UserUtils;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;

public class OrderCreateApiTest {

    private final String email = UUID.randomUUID() + "@ya.ru";
    private final String password = "Qwerty231";

    @Before
    public void setUp() {
        RestAssured.baseURI = "https://stellarburgers.nomoreparties.site";
        UserCreateRequest userCreateRequest = new UserCreateRequest(email, password, "test");
        UserUtils.createUser(userCreateRequest);
    }

    @Test
    @DisplayName("Create order with authorized user should return ok")
    public void createOrderWithAuthorizedUserShouldReturnOk() {
        UserLoginRequest userLoginRequest = new UserLoginRequest(email, password);
        UserLoginResponse userLoginResponse = UserUtils.loginUser(userLoginRequest);

        GetIngredientsResponse availableIngredients = IngredientUtils.getAvailableIngredients();
        List<String> ingredientsHash = availableIngredients.getData().stream()
                .map(GetIngredientsResponse.Ingredient::getId)
                .collect(Collectors.toList());

        OrderCreateRequest orderCreateRequest = new OrderCreateRequest(ingredientsHash);

        ValidatableResponse response = createOrderWithAuthorized(userLoginResponse, orderCreateRequest);
        checkSuccessfulOrderCreateResponseFieldsAndStatus(response);
    }

    @Test
    @DisplayName("Create order with not authorized user should return ok")
    public void createOrderWithNotAuthorizedUserShouldReturnOk() {
        GetIngredientsResponse availableIngredients = IngredientUtils.getAvailableIngredients();
        List<String> ingredientsHash = availableIngredients.getData().stream()
                .map(GetIngredientsResponse.Ingredient::getId)
                .collect(Collectors.toList());

        OrderCreateRequest orderCreateRequest = new OrderCreateRequest(ingredientsHash);

        ValidatableResponse response = createOrderWithoutAuthorized(orderCreateRequest);
        checkSuccessfulOrderCreateResponseFieldsAndStatus(response);
    }

    @Test
    @DisplayName("Create order should return error if ingredients list is empty")
    public void createOrderShouldReturnErrorIfIngredientsListIsEmpty() {
        UserLoginRequest userLoginRequest = new UserLoginRequest(email, password);
        UserLoginResponse userLoginResponse = UserUtils.loginUser(userLoginRequest);

        OrderCreateRequest orderCreateRequest = new OrderCreateRequest(Collections.emptyList());

        ValidatableResponse response = createOrderWithAuthorized(userLoginResponse, orderCreateRequest);
        checkFailedOrderCreateResponseFieldsAndStatus(response, 400, "Ingredient ids must be provided");
    }

    @Test
    @DisplayName("Create order should return error if ingredient not found")
    public void createOrderShouldReturnErrorIfIngredientNotFound() {
        UserLoginRequest userLoginRequest = new UserLoginRequest(email, password);
        UserLoginResponse userLoginResponse = UserUtils.loginUser(userLoginRequest);

        OrderCreateRequest orderCreateRequest = new OrderCreateRequest(List.of(UUID.randomUUID().toString()));

        ValidatableResponse response = createOrderWithAuthorized(userLoginResponse, orderCreateRequest);
        checkFailedOrderCreateResponseStatus(response, 500);
    }

    @Step("Create order with authorized")
    private ValidatableResponse createOrderWithAuthorized(UserLoginResponse userLoginResponse, OrderCreateRequest orderCreateRequest) {
        return given()
                .header("Content-type", "application/json")
                .header("Authorization", userLoginResponse.getAccessToken())
                .and()
                .body(orderCreateRequest)
                .when()
                .post("/api/orders")
                .then();
    }

    @Step("Create order without authorized")
    private ValidatableResponse createOrderWithoutAuthorized(OrderCreateRequest orderCreateRequest) {
        return given()
                .header("Content-type", "application/json")
                .and()
                .body(orderCreateRequest)
                .when()
                .post("/api/orders")
                .then();
    }

    @Step("Check response fields and status of successfully order create")
    private static void checkSuccessfulOrderCreateResponseFieldsAndStatus(ValidatableResponse response) {
        response.assertThat()
                .body("success", CoreMatchers.equalTo(true))
                .body("name", CoreMatchers.notNullValue())
                .body("order.number", CoreMatchers.notNullValue())
                .and()
                .statusCode(200);
    }

    @Step("Check response status of failed order create")
    private static void checkFailedOrderCreateResponseStatus(ValidatableResponse response, int code) {
        response.assertThat()
                .statusCode(code);
    }

    @Step("Check response fields and status of failed order create")
    private static void checkFailedOrderCreateResponseFieldsAndStatus(ValidatableResponse response, int code, String message) {
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
