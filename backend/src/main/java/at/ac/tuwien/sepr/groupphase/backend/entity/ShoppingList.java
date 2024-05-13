package at.ac.tuwien.sepr.groupphase.backend.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class ShoppingList {

    @Id
    @GeneratedValue
    @Column
    private Long id;

    @Column
    private String name;

    @Column
    private Float budget;

    @JoinTable(name = "shopping_list_shopping_list_item")
    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<ShoppingListItem> items = List.of();

    @JoinColumn
    @ManyToOne(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private GroupEntity group;

}