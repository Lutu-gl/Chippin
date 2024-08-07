package at.ac.tuwien.sepr.groupphase.backend.endpoint;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.activity.ActivityDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.activity.ActivitySearchDto;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.service.ActivityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.lang.invoke.MethodHandles;
import java.util.Collection;

@RestController
@RequestMapping(value = "/api/v1/activity")
public class ActivityEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    ActivityService activityService;

    public ActivityEndpoint(ActivityService activityService) {
        this.activityService = activityService;
    }

    @Secured("ROLE_USER")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("{id}")
    public ActivityDetailDto getById(@PathVariable("id") long id) throws NotFoundException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return activityService.getById(id, authentication.getName());
    }

    @Secured("ROLE_USER")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/group-expenses/{id}")
    public Collection<ActivityDetailDto> getGroupExpenses(@PathVariable("id") long groupId, ActivitySearchDto activitySearchDto) throws NotFoundException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return activityService.getExpenseActivitiesByGroupId(groupId, authentication.getName(), activitySearchDto);
    }

    @Secured("ROLE_USER")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/user-expenses")
    public Collection<ActivityDetailDto> getUserExpenses(ActivitySearchDto activitySearchDto) throws NotFoundException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return activityService.getExpenseActivitiesByUser(authentication.getName(), activitySearchDto);
    }

    @Secured("ROLE_USER")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/group-payments/{id}")
    public Collection<ActivityDetailDto> getGroupPayments(@PathVariable("id") long groupId, ActivitySearchDto activitySearchDto) throws NotFoundException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return activityService.getPaymentActivitiesByGroupId(groupId, authentication.getName(), activitySearchDto);
    }

}
