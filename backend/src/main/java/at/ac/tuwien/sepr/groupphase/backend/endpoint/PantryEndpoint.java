package at.ac.tuwien.sepr.groupphase.backend.endpoint;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.item.ItemDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.item.pantryitem.PantryItemMergeDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.pantry.GetRecipeDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.pantry.PantryDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.pantry.PantrySearchDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.item.pantryitem.PantryItemCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.item.pantryitem.PantryItemDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.recipe.RecipeByItemsDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.recipe.RecipeDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.recipe.RecipeListDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.ItemMapper;
import at.ac.tuwien.sepr.groupphase.backend.entity.Item;
import at.ac.tuwien.sepr.groupphase.backend.entity.PantryItem;
import at.ac.tuwien.sepr.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepr.groupphase.backend.service.PantryService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.lang.invoke.MethodHandles;
import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/group")
public class PantryEndpoint {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final PantryService pantryService;
    private final ItemMapper itemMapper;

    @Autowired
    public PantryEndpoint(PantryService pantryService, ItemMapper itemMapper) {
        this.pantryService = pantryService;
        this.itemMapper = itemMapper;
    }

    @Secured("ROLE_USER")
    @PreAuthorize("@securityService.isGroupMember(#pantryId)")
    @GetMapping("/{pantryId}/pantry")
    public PantryDetailDto findAllInPantry(@PathVariable long pantryId) {
        LOGGER.trace("GET /api/v1/group/{}/pantry", pantryId);
        return new PantryDetailDto(itemMapper.listOfPantryItemsToListOfPantryItemDto(pantryService.findAllItems(pantryId)));
    }

    @Secured("ROLE_USER")
    @PreAuthorize("@securityService.isGroupMember(#pantryId)")
    @GetMapping("/{pantryId}/pantry/search")
    public PantryDetailDto searchItemsInPantry(@PathVariable long pantryId, @Valid PantrySearchDto searchParams) {
        LOGGER.trace("GET /api/v1/group/{}/pantry/search", pantryId);
        return new PantryDetailDto(itemMapper.listOfPantryItemsToListOfPantryItemDto(pantryService.findItemsByDescription(searchParams.getDetails(), pantryId)));
    }

    @Secured("ROLE_USER")
    @PreAuthorize("@securityService.isGroupMember(#pantryId)")
    @PostMapping("/{pantryId}/pantry")
    @ResponseStatus(HttpStatus.CREATED)
    public ItemDto addItemToPantry(@PathVariable long pantryId, @Valid @RequestBody PantryItemCreateDto itemCreateDto) {
        LOGGER.trace("POST /api/v1/group/{}/pantry body: {}", pantryId, itemCreateDto);
        PantryItem item = itemMapper.pantryItemCreateDtoToPantryItem(itemCreateDto);
        return itemMapper.itemToItemDto(pantryService.addItemToPantry(item, pantryId));
    }

    @Secured("ROLE_USER")
    @PreAuthorize("@securityService.isGroupMember(#pantryId)")
    @DeleteMapping("/{pantryId}/pantry/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteItem(@PathVariable long pantryId, @PathVariable long itemId) {
        LOGGER.trace("DELETE /api/v1/group/{}/pantry/{}", pantryId, itemId);
        pantryService.deleteItem(pantryId, itemId);
    }

    @Secured("ROLE_USER")
    @PreAuthorize("@securityService.isGroupMember(#pantryId)")
    @DeleteMapping("/{pantryId}/pantry")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteItems(@PathVariable long pantryId, @RequestParam List<Long> itemIds) {
        LOGGER.trace("DELETE /api/v1/group/{}/pantry", pantryId);
        pantryService.deleteItems(pantryId, itemIds);
    }

    @Secured("ROLE_USER")
    @PreAuthorize("@securityService.isGroupMember(#pantryId)")
    @PutMapping("/{pantryId}/pantry")
    public ItemDto updateItem(@PathVariable long pantryId, @Valid @RequestBody PantryItemDto itemDto) {
        LOGGER.trace("PUT /api/v1/group/{}/pantry body: {}", pantryId, itemDto);
        PantryItem item = itemMapper.pantryItemDtoToPantryItem(itemDto);
        return itemMapper.itemToItemDto(pantryService.updateItem(item, pantryId));
    }

    @Secured("ROLE_USER")
    @PreAuthorize("@securityService.isGroupMember(#pantryId)")
    @PutMapping("/{pantryId}/pantry/multiple")
    public List<ItemDto> updateItems(@PathVariable long pantryId, @Valid @RequestBody List<PantryItemDto> itemDtos) {
        LOGGER.trace("PUT /api/v1/group/{}/pantry/multiple body: {}", pantryId, itemDtos);
        List<PantryItem> items = itemMapper.listofPantryItemDtoToListOfPantryItem(itemDtos);
        return itemMapper.listOfItemsToListOfItemDto(pantryService.updateItems(items, pantryId));
    }

    @Secured("ROLE_USER")
    @PreAuthorize("@securityService.isGroupMember(#pantryId)")
    @PutMapping("/{pantryId}/pantry/merged")
    public ItemDto mergeItems(@PathVariable long pantryId, @Valid @RequestBody PantryItemMergeDto itemMergeDto) throws ConflictException {
        LOGGER.trace("PUT /api/v1/group/{}/pantry/merged body: {}", pantryId, itemMergeDto);
        return itemMapper.itemToItemDto(pantryService.mergeItems(itemMergeDto, pantryId));
    }

    @Secured("ROLE_USER")
    @PreAuthorize("@securityService.isGroupMember(#pantryId)")
    @PostMapping("/{pantryId}/pantry/recipes/user/{userId}")
    public List<RecipeByItemsDto> getRecipes(@PathVariable long pantryId, @PathVariable long userId, @Valid @RequestBody GetRecipeDto getRecipeDto) {
        LOGGER.trace("POST /api/v1/group/{pantryId}/pantry/recipes/user/{userId}", pantryId, userId);
        return pantryService.getRecipes(getRecipeDto, pantryId, userId);
    }

    @Secured("ROLE_USER")
    @PreAuthorize("@securityService.isGroupMember(#pantryId)")
    @GetMapping("/{pantryId}/pantry/missing")
    public List<PantryItemDto> findAllMissingItems(@PathVariable long pantryId) {
        return pantryService.findAllMissingItems(pantryId);
    }

}
