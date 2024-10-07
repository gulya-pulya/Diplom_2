package ru.yandex.practicum.util;

import io.qameta.allure.Step;
import ru.yandex.practicum.dto.response.GetIngredientsResponse;

import static io.restassured.RestAssured.given;

public class IngredientUtils {

    @Step("Get available ingredients")
    public static GetIngredientsResponse getAvailableIngredients() {
        return given()
                .when()
                .get("/api/ingredients")
                .body()
                .as(GetIngredientsResponse.class);
    }
}
