package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.budget;

import at.ac.tuwien.sepr.groupphase.backend.entity.Category;
import at.ac.tuwien.sepr.groupphase.backend.entity.ResetFrequency;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class BudgetCreateDto {
    @NotNull(message = "Budget name must be given")
    @NotBlank(message = "Budget name must not be empty")
    @Size(min = 1, max = 50, message = "Budget name must be between 2 and 60 characters")
    private String name;
    @NotNull(message = "Amount must be given")
    @PositiveOrZero(message = "Amount must be positive or zero")
    @Max(value = 99999, message = "Amount must be less than or equal to 99999")
    private double amount;

    private Category category = Category.Other;
    @NotNull(message = "Reset frequency must be given")
    private ResetFrequency resetFrequency;
}
