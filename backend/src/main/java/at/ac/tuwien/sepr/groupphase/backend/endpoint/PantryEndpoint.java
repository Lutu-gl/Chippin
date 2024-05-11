package at.ac.tuwien.sepr.groupphase.backend.endpoint;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ItemCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ItemDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.PantryDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.PantrySearchDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.ItemMapper;
import at.ac.tuwien.sepr.groupphase.backend.entity.Item;
import at.ac.tuwien.sepr.groupphase.backend.service.PantryService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.lang.invoke.MethodHandles;

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
    @GetMapping("/{pantryId}/pantry")
    public PantryDetailDto findAllInPantry(@PathVariable long pantryId) {
        LOGGER.info("GET /api/v1/group/{}/pantry", pantryId);
        return new PantryDetailDto(itemMapper.listOfItemsToListOfItemDto(pantryService.findAllItems(pantryId)));
    }

    @Secured("ROLE_USER")
    @GetMapping("/{pantryId}/pantry/search")
    public PantryDetailDto searchItemsInPantry(@PathVariable long pantryId, PantrySearchDto searchParams) {
        LOGGER.info("GET /api/v1/group/{}/pantry/search", pantryId);
        LOGGER.debug("request parameters: {}", searchParams);
        return new PantryDetailDto(itemMapper.listOfItemsToListOfItemDto(pantryService.findItemsByDescription(searchParams.getDetails(), pantryId)));
    }

    @Secured("ROLE_USER")
    @PostMapping("/{pantryId}/pantry")
    @ResponseStatus(HttpStatus.CREATED)
    public ItemDto addItemToPantry(@PathVariable long pantryId, @Valid @RequestBody ItemCreateDto itemCreateDto) {
        LOGGER.info("POST /api/v1/group/{}/pantry body: {}", pantryId, itemCreateDto);
        Item item = itemMapper.itemCreateDtoToItem(itemCreateDto);
        return itemMapper.itemToItemDto(pantryService.addItemToPantry(item, pantryId));
    }

    @Secured("ROLE_USER")
    @DeleteMapping("/{pantryId}/pantry/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteItem(@PathVariable long pantryId, @PathVariable long itemId) {
        LOGGER.info("DELETE /api/v1/group/{}/pantry/{}", pantryId, itemId);
        pantryService.deleteItem(pantryId, itemId);
    }

    @Secured("ROLE_USER")
    @PutMapping("/{pantryId}/pantry")
    public ItemDto updateItem(@PathVariable long pantryId, @Valid @RequestBody ItemDto itemDto) {
        LOGGER.info("PUT /api/v1/group/{}/pantry body: {}", pantryId, itemDto);
        return itemMapper.itemToItemDto(pantryService.updateItem(itemDto, pantryId));
    }
}