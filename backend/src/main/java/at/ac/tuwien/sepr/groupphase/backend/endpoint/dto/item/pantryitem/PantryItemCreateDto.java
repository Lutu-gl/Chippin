package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.item.pantryitem;

import at.ac.tuwien.sepr.groupphase.backend.entity.Unit;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class PantryItemCreateDto {
    @NotNull
    @NotBlank
    @Size(min = 2, max = 60)
    private String description;
    @Min(0)
    @Max(1000000)
    private int amount;
    @NotNull
    private Unit unit;
    @Min(0)
    @Max(1000000)
    private Long lowerLimit;
}
