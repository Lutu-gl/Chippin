package at.ac.tuwien.sepr.groupphase.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.OneToMany;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.CascadeType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ApplicationUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    @NotBlank
    @Email
    private String email;

    @Column
    @NotBlank
    @ToString.Exclude
    private String password;

    @Column
    private Boolean admin;

    @ManyToMany(mappedBy = "users", fetch = FetchType.EAGER)
    @Builder.Default
    @ToString.Exclude
    private Set<GroupEntity> groups = new HashSet<>();

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Builder.Default
    @JsonManagedReference
    private List<Recipe> recipes = new ArrayList<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @Builder.Default
    @JsonIgnore
    @JoinTable(
        name = "user_recipe_likes",
        joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
        inverseJoinColumns = @JoinColumn(name = "recipe_id", referencedColumnName = "id")
    )
    private Set<Recipe> likedRecipes = new HashSet<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @Builder.Default
    @JsonIgnore
    @JoinTable(
        name = "user_recipe_dislikes",
        joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
        inverseJoinColumns = @JoinColumn(name = "recipe_id", referencedColumnName = "id")
    )
    private Set<Recipe> dislikedRecipes = new HashSet<>();

    public ApplicationUser addRecipe(Recipe recipe) {
        recipes.add(recipe);
        recipe.setOwner(this);

        return this;
    }

    public void removeRecipe(Recipe recipe) {
        recipes.remove(recipe);
        recipe.setOwner(null);
    }

    public ApplicationUser addRecipeLike(Recipe recipe) {
        recipe.setLikes(recipe.getLikes() + 1);
        likedRecipes.add(recipe);
        recipe.addLiker(this);
        return this;
    }

    public void removeLike(Recipe recipe) {
        recipe.setLikes(recipe.getLikes() - 1);
        this.likedRecipes.remove(recipe);
        recipe.getLikedByUsers().remove(this);
    }

    public void removeDisLike(Recipe recipe) {
        recipe.setLikes(recipe.getDislikes() - 1);
        this.dislikedRecipes.remove(recipe);
        recipe.getDislikedByUsers().remove(this);
    }

    public ApplicationUser addRecipeDislike(Recipe recipe) {
        recipe.setLikes(recipe.getDislikes() + 1);
        dislikedRecipes.add(recipe);
        recipe.addDisliker(this);
        return this;
    }


}