package at.ac.tuwien.sepr.groupphase.backend.entity;


import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.NamedEntityGraphs;
import jakarta.persistence.Table;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.OneToMany;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.FetchType;
import jakarta.persistence.PreRemove;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.validator.constraints.Length;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@NamedEntityGraphs({
    @NamedEntityGraph(
        name = "graph.Recipe.likedByUsers",
        attributeNodes = @NamedAttributeNode("likedByUsers")),
    @NamedEntityGraph(
        name = "graph.Recipe.dislikedByUsers",
        attributeNodes = @NamedAttributeNode("dislikedByUsers")),
    @NamedEntityGraph(
        name = "graph.Recipe.likedAndDislikedByUsers",
        attributeNodes = {
            @NamedAttributeNode("likedByUsers"),
            @NamedAttributeNode("dislikedByUsers")
        })
})
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "recipe")
public class Recipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @Builder.Default
    @OneToMany(mappedBy = "recipe", cascade = {CascadeType.PERSIST, CascadeType.REMOVE}, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Item> ingredients = new ArrayList<>();

    @Column(nullable = false)
    private String name;


    @Lob
    @Column(nullable = false)
    @Length(max = 15000)
    private String description;

    @Column(nullable = false)
    private Boolean isPublic;

    @Column(nullable = false)
    private int portionSize;

    @Builder.Default
    @Column(nullable = false)
    private int likes = 0;

    @Builder.Default
    @Column(nullable = false)
    private int dislikes = 0;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference
    private ApplicationUser owner;

    @ManyToMany(mappedBy = "likedRecipes", fetch = FetchType.LAZY)
    @Builder.Default
    @JsonIgnore
    private Set<ApplicationUser> likedByUsers = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "dislikedRecipes")
    @Builder.Default
    @JsonIgnore
    private Set<ApplicationUser> dislikedByUsers = new HashSet<>();

    public void addIngredient(Item item) {
        if (this.ingredients == null) {
            this.ingredients = new ArrayList<>();
        }
        ingredients.add(item);
        item.setRecipe(this);

    }

    public void removeItem(Item item) {
        ingredients.remove(item);
        item.setRecipe(null);
    }

    public void addLiker(ApplicationUser user) {
        if (!likedByUsers.contains(user)) {
            likedByUsers.add(user);
            user.getLikedRecipes().add(this);
            likes = likedByUsers.size();
        }
    }

    public void removeLiker(ApplicationUser user) {

        ApplicationUser toRemove = likedByUsers.stream().filter(o -> o.getId().equals(user.getId())).findFirst().get();
        likedByUsers.remove(toRemove);
        likes = likedByUsers.size();

    }

    public void addDisliker(ApplicationUser user) {
        if (!dislikedByUsers.contains(user)) {
            dislikedByUsers.add(user);
            user.getDislikedRecipes().add(this);
            dislikes = dislikedByUsers.size();
        }
    }

    public void removeDisliker(ApplicationUser user) {
        ApplicationUser toRemove = dislikedByUsers.stream().filter(o -> o.getId().equals(user.getId())).findFirst().get();
        dislikedByUsers.remove(toRemove);
        dislikes = dislikedByUsers.size();

    }

    @PreRemove
    private void removeRecipe() {
        for (ApplicationUser user : this.likedByUsers) {
            user.getLikedRecipes().remove(this);
        }

        for (ApplicationUser user : this.dislikedByUsers) {
            user.getDislikedRecipes().remove(this);
        }

        owner.getRecipes().remove(this);
    }


}
