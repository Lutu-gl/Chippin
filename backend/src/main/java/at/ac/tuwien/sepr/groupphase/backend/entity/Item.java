package at.ac.tuwien.sepr.groupphase.backend.entity;


import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@Table(name = "item")
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    @Size(min = 2, max = 60)
    @NotBlank
    @NotNull
    private String description;

    @Column
    @PositiveOrZero
    private double amount;

    @Column
    @NotNull
    private Unit unit;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "pantry_id")
    private Pantry pantry;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "item_list_id")
    private ItemList itemList;

}
