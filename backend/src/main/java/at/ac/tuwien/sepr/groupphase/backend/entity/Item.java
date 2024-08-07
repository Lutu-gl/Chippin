package at.ac.tuwien.sepr.groupphase.backend.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString
@Table(name = "item")
public class Item {
    public static final int MAX_AMOUNT = 1000000;
    private static final String MAX_AMOUNT_MSG = "The maximum amount is " + MAX_AMOUNT;


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    @Size(min = 2, max = 60, message = "The item name must be between 2 and 60 characters long")
    @NotBlank
    @NotNull
    private String description;

    @Column
    @Min(value = 0, message = "The minimum amount is 0")
    @Max(value = MAX_AMOUNT, message = MAX_AMOUNT_MSG)
    private int amount;

    @Column
    @NotNull(message = "Unit must not be empty")
    private Unit unit;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.DETACH})
    @JoinColumn(name = "recipe_id")
    @JsonIgnore
    @ToString.Exclude
    private Recipe recipe;
}



