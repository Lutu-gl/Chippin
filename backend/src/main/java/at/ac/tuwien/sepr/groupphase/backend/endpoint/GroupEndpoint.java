package at.ac.tuwien.sepr.groupphase.backend.endpoint;


import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.expense.ExpenseDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.group.GroupCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.payment.PaymentDto;
import at.ac.tuwien.sepr.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.service.GroupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.lang.invoke.MethodHandles;
import java.util.List;

@RestController
@RequestMapping(value = GroupEndpoint.BASE_PATH)
public class GroupEndpoint {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    static final String BASE_PATH = "/api/v1/group";

    private final GroupService groupService;

    @Autowired
    public GroupEndpoint(GroupService groupService) {
        this.groupService = groupService;
    }

    @Secured("ROLE_USER")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("@securityService.isGroupMember(#id)")
    @GetMapping("{id}")
    public GroupCreateDto getById(@PathVariable("id") long id) throws NotFoundException {
        LOGGER.trace("getById({})", id);
        return groupService.getById(id);
    }

    @Secured("ROLE_USER")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public GroupCreateDto createGroup(@RequestBody GroupCreateDto groupCreateDto) throws ValidationException, ConflictException {
        LOGGER.trace("createGroup({})", groupCreateDto);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        return groupService.create(groupCreateDto, authentication.getName());
    }

    @Secured("ROLE_USER")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("@securityService.isGroupMember(#id)")
    @PutMapping("{id}")
    public GroupCreateDto updateGroup(@PathVariable("id") long id, @RequestBody GroupCreateDto groupCreateDto) throws ValidationException, ConflictException {
        LOGGER.trace("updateGroup({}, {})", id, groupCreateDto);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        groupCreateDto.setId(id);   // set the id of the group to update

        return groupService.update(groupCreateDto, authentication.getName());
    }

    @Secured("ROLE_USER")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("@securityService.isGroupMember(#id)")
    @GetMapping("{id}/expenses")
    public List<ExpenseDetailDto> getAllExpensesById(@PathVariable("id") long id) throws NotFoundException {
        LOGGER.trace("getAllExpensesById({})", id);
        return groupService.getAllExpensesById(id);
    }

    @Secured("ROLE_USER")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("@securityService.isGroupMember(#id)")
    @GetMapping("{id}/payments")
    public List<PaymentDto> getAllPaymentsById(@PathVariable("id") long id) throws NotFoundException {
        LOGGER.trace("getAllPaymentsById({})", id);
        return groupService.getAllPaymentsById(id);
    }
}
