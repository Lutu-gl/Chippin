package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import at.ac.tuwien.sepr.groupphase.backend.entity.Item;
import at.ac.tuwien.sepr.groupphase.backend.entity.PantryItem;
import at.ac.tuwien.sepr.groupphase.backend.entity.ShoppingListItem;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;


import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@EqualsAndHashCode
@AllArgsConstructor
public class AddRecipeItemToShoppingListDto {

    @NotNull
    @Builder.Default
    List<Item> recipeItems = new ArrayList<>();

    @NotNull
    @Builder.Default
    List<ShoppingListItem> shoppingListItems = new ArrayList<>();

    @Builder.Default
    List<PantryItem> pantryItems = new ArrayList<>();
}
