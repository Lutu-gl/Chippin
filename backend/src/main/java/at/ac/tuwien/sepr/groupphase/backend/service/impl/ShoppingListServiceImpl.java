package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.AddRecipeItemToShoppingListDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.item.ItemCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.shoppinglist.ShoppingListCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.shoppinglist.ShoppingListItemUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.shoppinglist.ShoppingListUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.ItemMapper;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.ShoppingListMapper;
import at.ac.tuwien.sepr.groupphase.backend.entity.Item;
import at.ac.tuwien.sepr.groupphase.backend.entity.PantryItem;
import at.ac.tuwien.sepr.groupphase.backend.entity.ShoppingList;
import at.ac.tuwien.sepr.groupphase.backend.entity.ShoppingListItem;
import at.ac.tuwien.sepr.groupphase.backend.entity.Unit;
import at.ac.tuwien.sepr.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.repository.GroupRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.PantryItemRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.RecipeRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ShoppingListItemRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ShoppingListRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.UserRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.PantryService;
import at.ac.tuwien.sepr.groupphase.backend.service.ShoppingListService;
import at.ac.tuwien.sepr.groupphase.backend.service.validator.ShoppingListValidator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ShoppingListServiceImpl implements ShoppingListService {


    private final ShoppingListMapper shoppingListMapper;
    private final ShoppingListRepository shoppingListRepository;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final RecipeRepository recipeRepository;
    private final PantryItemRepository pantryItemRepository;
    private final ShoppingListItemRepository shoppingListItemRepository;
    private final PantryService pantryService;
    private final ItemMapper itemMapper;
    private final ShoppingListValidator shoppingListValidator;


    @Override
    @Transactional
    public ShoppingList createShoppingList(ShoppingListCreateDto shoppingListCreateDto, Long ownerId) {
        log.trace("createShoppingList({}, {})", shoppingListCreateDto, ownerId);
        log.trace("Creating shopping list {} for user {}", shoppingListCreateDto, ownerId);
        ShoppingList shoppingList = shoppingListMapper.shoppingListCreateDtoToShoppingList(shoppingListCreateDto);
        // Add owner to shopping list
        shoppingList.setOwner(userRepository.findById(ownerId).orElseThrow(() -> new NotFoundException("User with id " + ownerId + " not found")));

        // Add group to shopping list
        if (shoppingListCreateDto.getGroup() != null) {
            var group = groupRepository.findById(shoppingListCreateDto.getGroup().getId())
                .orElseThrow(() -> new NotFoundException("Group with id " + shoppingListCreateDto.getGroup().getId() + " not found"));
            log.trace("Setting group of shopping list to: {}", group);
            shoppingList.setGroup(group);
        }

        ShoppingList savedList = shoppingListRepository.save(shoppingList);
        log.trace("Shopping list created with id {}", savedList.getId());
        return savedList;
    }

    @Override
    public ShoppingList getShoppingList(Long id) {
        log.trace("getShoppingList({})", id);
        log.trace("Getting shopping list with id {}", id);
        var shoppingList = shoppingListRepository.findById(id).orElseThrow(() -> new NotFoundException("Shopping list with id " + id + " not found"));
        log.trace("Found shopping list: {}", shoppingList);
        return shoppingList;
    }

    @Override
    public void deleteShoppingList(Long id) throws ConflictException {
        log.trace("Deleting shopping list with id {}", id);
        shoppingListValidator.validateForDelete(id);
        shoppingListRepository.deleteById(id);
        log.trace("Shopping list deleted");
    }

    /**
     * Merge item into shopping list.
     * If an item with the same description, unit and checked-state exists in the shopping list, the quantity of the existing item is increased and the checked-state is updated.
     *
     * @param item         the item to merge
     * @param shoppingList the shopping list to merge the item into
     */
    private ShoppingListItem mergeNewItem(ShoppingListItem item, ShoppingList shoppingList) throws ConflictException {
        log.trace("mergeNewItem({}, {})", item, shoppingList);
        log.trace("Merging item {} into shopping list {}", item, shoppingList);
        var updatedItem = item;
        var existingItem = shoppingList.getItems().stream()
            .filter(i -> i.getItem().getDescription().equals(item.getItem().getDescription())
                && i.getItem().getUnit().equals(item.getItem().getUnit())
                && !i.getId().equals(item.getId())
                && ((i.getCheckedBy() == null) == (item.getCheckedBy() == null)))
            .findFirst();
        if (existingItem.isPresent()) {
            log.trace("Item already exists in shopping list. Merging quantities");
            // Check if mergin the item would exceed the maximum amount of an item
            if (existingItem.get() != item && existingItem.get().getItem().getAmount() + item.getItem().getAmount() > Item.MAX_AMOUNT) {
                log.warn("The maximum amount of the item {} was reached", existingItem.get().getItem().getDescription());
                throw new ConflictException("Conflict", List.of("The maximum amount of the item " + existingItem.get().getItem().getDescription() + " was reached"));
            }
            existingItem.get().getItem().setAmount(existingItem.get().getItem().getAmount() + item.getItem().getAmount());
            existingItem.get().setCheckedBy(item.getCheckedBy());
            updatedItem = existingItem.get();
        } else {
            log.trace("Item does not exist in shopping list. Adding item");
            shoppingList.getItems().add(item);
        }
        return updatedItem;
    }


    @Override
    @Transactional
    public ShoppingListItem addItemForUser(Long shoppingListId, ItemCreateDto itemCreateDto, Long userId) throws ConflictException {
        log.trace("addItemForUser({}, {}, {})", shoppingListId, itemCreateDto, userId);
        log.trace("Adding item {} to shopping list with id {}", itemCreateDto, shoppingListId);
        var shoppingList = shoppingListRepository.findById(shoppingListId).orElseThrow(
            () -> new NotFoundException("Shopping list with id " + shoppingListId + " not found")
        );
        // Get current user
        var user = userRepository.findById(userId).orElseThrow(
            () -> new NotFoundException("User with id " + userId + " not found")
        );
        var item = shoppingListMapper.itemCreateDtoAndUserToShoppingListItem(itemCreateDto, user);
        mergeNewItem(item, shoppingList);
        var savedShoppingList = shoppingListRepository.save(shoppingList);
        log.trace("Item added to shopping list: {}", savedShoppingList);
        return shoppingList.getItems().getLast();
    }

    @Override
    @Transactional
    public Long deleteItem(Long shoppingListId, Long itemId) {
        log.trace("deleteItem({}, {})", shoppingListId, itemId);
        var shoppingList = shoppingListRepository.findById(shoppingListId).orElseThrow(
            () -> new NotFoundException("Shopping list with id " + shoppingListId + " not found")
        );
        var item = shoppingList.getItems().stream()
            .filter(i -> i.getId().equals(itemId))
            .findFirst()
            .orElseThrow(() -> new NotFoundException("Item with id " + itemId + " not found in shopping list with id " + shoppingListId));
        shoppingList.getItems().remove(item);
        shoppingListRepository.save(shoppingList);
        shoppingListItemRepository.delete(item);
        log.trace("Item deleted");
        return itemId;
    }

    @Override
    public List<ShoppingList> getShoppingListsForGroup(Long groupId) {
        log.trace("getShoppingListsForGroup({})", groupId);
        log.trace("Getting shopping lists for group {}", groupId);
        List<ShoppingList> shoppingLists = shoppingListRepository.findAllByGroupId(groupId);
        log.trace("Found {} shopping lists for group {}", shoppingLists.size(), groupId);
        return shoppingLists;
    }

    @Override
    @Transactional
    public ShoppingList updateShoppingList(Long shoppingListId, ShoppingListUpdateDto shoppingList) throws ConflictException {
        log.trace("updateShoppingList({}, {})", shoppingListId, shoppingList);
        log.trace("Updating shopping list with id {}", shoppingListId);
        var shoppingListEntity =
            shoppingListRepository.findById(shoppingListId).orElseThrow(() -> new NotFoundException("Shopping list with id " + shoppingListId + " not found"));
        shoppingListValidator.validateForUpdateGroup(shoppingList, shoppingListEntity);
        shoppingListMapper.updateShoppingList(shoppingListEntity, shoppingList);
        if (shoppingList.getGroup() != null) {
            var group = groupRepository.findById(shoppingList.getGroup().getId())
                .orElseThrow(() -> new NotFoundException("Group with id " + shoppingList.getGroup().getId() + " not found"));
            log.trace("Setting group of shopping list to: {}", group);
            shoppingListEntity.setGroup(group);
        } else {
            shoppingListEntity.setGroup(null);
        }
        var savedShoppingList = shoppingListRepository.save(shoppingListEntity);
        log.trace("Shopping list updated: {}", savedShoppingList);
        return savedShoppingList;
    }

    @Override
    public List<ShoppingList> getShoppingListsForUser(Long userId) {
        log.trace("getShoppingListsForUser({})", userId);
        log.trace("Getting shopping lists for user {}", userId);
        var ownedShoppingLists = shoppingListRepository.findAllByOwnerId(userId);
        log.trace("Found {} shopping lists where user is owner", ownedShoppingLists.size());
        var groupShoppingLists = shoppingListRepository.findByGroup_Users_Id(userId);
        log.trace("Found {} shopping lists where user is in group", groupShoppingLists.size());

        // Add group shopping lists to owned shopping lists if not already present
        groupShoppingLists.stream()
            .filter(sl -> ownedShoppingLists.stream().noneMatch(osl -> osl.getId().equals(sl.getId())))
            .forEach(ownedShoppingLists::add);

        return ownedShoppingLists;
    }

    /**
     * Merge an item that already exists in a shopping list into that shopping list.
     *
     * @param item         The item to merge
     * @param shoppingList The shopping list to merge the item into
     * @return The updated item
     */
    private ShoppingListItem mergeExistingItem(ShoppingListItem item, ShoppingList shoppingList) throws ConflictException {
        log.trace("mergeExistingItem({}, {})", item, shoppingList);
        var updatedItem = item;
        // Check if item already exists in shopping list
        var existingItem = shoppingList.getItems().stream()
            .filter(i -> i.getItem().getDescription().equals(item.getItem().getDescription())
                && i.getItem().getUnit().equals(item.getItem().getUnit())
                && ((i.getCheckedBy() == null) == (item.getCheckedBy() == null))
                && (i.getId() != item.getId()))
            .findFirst();
        if (existingItem.isPresent()) {
            // Merge quantities
            // Check if mergin the item would exceed the maximum amount of an item
            if (existingItem.get() != item && existingItem.get().getItem().getAmount() + item.getItem().getAmount() > Item.MAX_AMOUNT) {
                log.warn("The maximum amount of the item {} was reached", existingItem.get().getItem().getDescription());
                throw new ConflictException("Conflict", List.of("The maximum amount of the item " + existingItem.get().getItem().getDescription() + " was reached"));
            }
            existingItem.get().setCheckedBy(item.getCheckedBy());
            // Remove the old item from the shopping list
            shoppingList.getItems().remove(item);
            updatedItem = existingItem.get();
        }
        return updatedItem;
    }

    @Override
    @Transactional(rollbackOn = ConflictException.class)
    public ShoppingListItem updateItemForUser(Long shoppingListId, Long itemId, ShoppingListItemUpdateDto shoppingListItemUpdateDto, Long userId)
        throws ConflictException {
        log.trace("updateItemForUser({}, {}, {}, {})", shoppingListId, itemId, shoppingListItemUpdateDto, userId);
        log.trace("Updating item with id {} in shopping list with id {} for user with id {}", itemId, shoppingListId, userId);
        var shoppingList = shoppingListRepository.findById(shoppingListId).orElseThrow(
            () -> new NotFoundException("Shopping list with id " + shoppingListId + " not found")
        );
        var shoppingListItem = shoppingList.getItems().stream()
            .filter(i -> i.getId().equals(itemId))
            .findFirst()
            .orElseThrow(() -> new NotFoundException("Item with id " + itemId + " not found in shopping list with id " + shoppingListId));
        // Update the item inside the shopping-list-item
        shoppingListMapper.updateShoppingListItem(shoppingListItem, shoppingListItemUpdateDto);
        // Add checkedBy if item is checked
        if (shoppingListItemUpdateDto.isChecked()) {
            shoppingListItem.setCheckedBy(userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException("User with id " + userId + " not found")
            ));
        } else {
            shoppingListItem.setCheckedBy(null);
        }
        // Merge item into shopping list
        shoppingListItem = mergeExistingItem(shoppingListItem, shoppingList);
        shoppingListRepository.save(shoppingList);
        log.trace("Item updated: {}", shoppingListItem);
        return shoppingListItem;
    }

    @Override
    @Transactional(rollbackOn = ConflictException.class)
    public void moveItemToPantry(Long shoppingListId, Long itemId) {
        log.trace("moveItemToPantry({}, {})", shoppingListId, itemId);
        log.trace("Moving item with id {} to pantry from group in shopping list with id {}", itemId, shoppingListId);
        var shoppingList = shoppingListRepository.findById(shoppingListId).orElseThrow(
            () -> new NotFoundException(String.format("Shopping list with id %d not found", shoppingListId))
        );
        var group = shoppingList.getGroup();
        if (group == null) {
            throw new NotFoundException(String.format("Shopping list with id %d does not belong to a group", shoppingListId));
        }
        var shoppingListItem = shoppingList.getItems().stream()
            .filter(i -> i.getId().equals(itemId))
            .findFirst()
            .orElseThrow(
                () -> new NotFoundException(String.format("Item with id %d not found in shopping list with id %d", itemId, shoppingListId))
            );
        var pantryItem = itemMapper.itemToPantryItem(shoppingListItem.getItem(), null);
        log.trace("Adding item to pantry: {}", pantryItem);
        pantryService.addItemToPantry(pantryItem, group.getPantry().getId());
        shoppingList.getItems().remove(shoppingListItem);
        shoppingListRepository.save(shoppingList);
        log.trace("Item moved to pantry");
    }

    @Override
    @Transactional(rollbackOn = ConflictException.class)
    public void moveItemsToPantry(Long shoppingListId) {
        log.trace("moveItemsToPantry({})", shoppingListId);
        log.trace("Moving all checked items to pantry from shopping list with id {}", shoppingListId);
        var shoppingList = shoppingListRepository.findById(shoppingListId).orElseThrow(
            () -> new NotFoundException(String.format("Shopping list with id %d not found", shoppingListId))
        );
        var group = shoppingList.getGroup();
        if (group == null) {
            throw new NotFoundException(String.format("Shopping list with id %d does not belong to a group", shoppingListId));
        }
        var checkedItems = shoppingList.getItems().stream().filter(item -> item.getCheckedBy() != null);

        // Add checked items to pantry
        checkedItems.forEach(item -> {
            var pantryItem = itemMapper.itemToPantryItem(item.getItem(), null);
            log.trace("Adding item to pantry: {}", pantryItem);
            pantryService.addItemToPantry(pantryItem, group.getPantry().getId());
        });
        // Remove checked items from shopping list
        shoppingList.getItems().removeIf(item -> item.getCheckedBy() != null);
        shoppingListRepository.save(shoppingList);

        log.trace("Items moved to pantry");
    }

    @Override
    @Transactional
    public void deleteCheckedItems(Long shoppingListId) {
        log.trace("deleteCheckedItems({})", shoppingListId);
        var shoppingList = shoppingListRepository.findById(shoppingListId).orElseThrow(
            () -> new NotFoundException(String.format("Shopping list with id %d not found", shoppingListId))
        );
        shoppingList.getItems().removeIf(item -> item.getCheckedBy() != null);
        shoppingListRepository.save(shoppingList);
    }

    @Override
    @Transactional(rollbackOn = ConflictException.class)
    public List<ShoppingListItem> addItemsForUser(Long shoppingListId, List<ItemCreateDto> items, Long userId) throws ConflictException {
        log.trace("addItemsForUser({}, {}, {})", shoppingListId, items, userId);
        log.trace("Adding items to shopping list with id {} for user with id {}", shoppingListId, userId);
        var shoppingList = shoppingListRepository.findById(shoppingListId).orElseThrow(
            () -> new NotFoundException("Shopping list with id " + shoppingListId + " not found"));
        var user = userRepository.findById(userId).orElseThrow(
            () -> new NotFoundException("User with id " + userId + " not found"));
        List<ShoppingListItem> addedItems = new ArrayList<>();
        for (var itemCreateDto : items) {
            var item = shoppingListMapper.itemCreateDtoAndUserToShoppingListItem(itemCreateDto, user);
            addedItems.add(mergeNewItem(item, shoppingList));
        }
        shoppingListRepository.save(shoppingList);
        log.trace("Items added to shopping list");
        return addedItems;

    }

    @Override
    public Long getAmountOfItemInGroupShoppingLists(Long groupId, String description, Unit unit) {
        log.trace("getAmountOfItemInGroupShoppingLists({}, {}, {})", groupId, description, unit);
        log.trace("Getting amount of item with description {} and unit {} in shopping lists of group with id {}", description, unit, groupId);
        return shoppingListItemRepository.getAmountOfItemInGroupShoppingLists(groupId, description, unit);
    }


    @Override
    public AddRecipeItemToShoppingListDto selectIngredientsForShoppingList(long recipeId, long shoppingListId, Long pantryId) {
        log.trace("selectIngredientsForShoppingList({}, {}, {})", recipeId, shoppingListId, pantryId);
        //Get Recipe
        List<Item> recipe = recipeRepository.findAllIngredientsByRecipeId(recipeId);

        //Get Pantry
        List<PantryItem> pantry = new ArrayList<>();
        if (pantryId == null) {
            pantry = null;
        } else {
            pantry = pantryItemRepository.findMatchingRecipeItemsInPantry(pantryId, recipeId);
        }
        //Get Shopping List
        Optional<ShoppingList> optional = shoppingListRepository.findById(shoppingListId);
        if (optional.isEmpty()) {
            throw new NotFoundException("Could not find shoppinglist");
        }
        //If shoppingListItem is in recipe, return it to the result dto
        List<ShoppingListItem> shoppingList = new ArrayList<>();
        for (ShoppingListItem item : optional.get().getItems()) {
            if (recipe.stream().anyMatch(o -> item.getItem().getDescription().equals(o.getDescription())
                && item.getItem().getUnit().equals(o.getUnit()))) {

                shoppingList.add(item);
            }
        }


        return AddRecipeItemToShoppingListDto.builder().recipeItems(recipe).pantryItems(pantry).shoppingListItems(shoppingList).build();
    }


}
