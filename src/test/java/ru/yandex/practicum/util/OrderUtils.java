package ru.yandex.practicum.util;

import io.qameta.allure.Step;
import ru.yandex.practicum.dto.request.OrderCreateRequest;
import ru.yandex.practicum.dto.response.GetIngredientsResponse;
import ru.yandex.practicum.dto.response.OrderCreateResponse;
import ru.yandex.practicum.dto.response.UserLoginResponse;

import java.util.List;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.given;

public class OrderUtils {

    @Step("Create user order")
    public static OrderCreateResponse createOrder(UserLoginResponse userLoginResponse) {
        GetIngredientsResponse availableIngredients = IngredientUtils.getAvailableIngredients();
        List<String> ingredientsHash = availableIngredients.getData().stream()
                .map(GetIngredientsResponse.Ingredient::getId)
                .collect(Collectors.toList());

        OrderCreateRequest orderCreateRequest = new OrderCreateRequest(ingredientsHash);

        return given()
                .header("Content-type", "application/json")
                .header("Authorization", userLoginResponse.getAccessToken())
                .and()
                .body(orderCreateRequest)
                .when()
                .post("/api/orders")
                .body()
                .as(OrderCreateResponse.class);
    }
}
