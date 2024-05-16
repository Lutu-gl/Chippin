package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.recipe;


import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.item.ItemDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RecipeDetailDto {

    private long id;

    private List<ItemDto> ingredients;

    private String name;

    private String description;

    private boolean isPublic;

    private int portionSize;

    private int likes;

    private int dislikes;


    public int getScore() {
        return likes - dislikes;
    }


}
