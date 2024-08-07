package at.ac.tuwien.sepr.groupphase.backend.service;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.activity.ActivityDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.activity.ActivitySearchDto;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;

import java.util.Collection;

public interface ActivityService {

    /**
     * Get a specific activity by id.
     *
     * @param id the id of the activity
     * @param requesterEmail the email of the user who requests the activity
     * @return the activity
     * @throws NotFoundException if the activity is not found
     */
    ActivityDetailDto getById(Long id, String requesterEmail) throws NotFoundException;

    /**
     * Get all expense activities of a specific user.
     *
     * @param requesterEmail the email of the user who requests the activities
     * @param activitySearchDto the search criteria
     * @return a collection of the expense activities
     * @throws NotFoundException if the user is not found
     */
    Collection<ActivityDetailDto> getExpenseActivitiesByUser(String requesterEmail, ActivitySearchDto activitySearchDto) throws NotFoundException;

    /**
     * Get all expense activities of a specific group.
     *
     * @param groupId        the id of the group
     * @param requesterEmail the email of the user who requests the activities
     * @param activitySearchDto the search criteria
     * @return a collection of the expense activities
     * @throws NotFoundException if the group is not found
     */
    Collection<ActivityDetailDto> getExpenseActivitiesByGroupId(Long groupId, String requesterEmail, ActivitySearchDto activitySearchDto) throws NotFoundException;

    /**
     * Get all payment activities of a specific group.
     *
     * @param groupId        the id of the group
     * @param requesterEmail the email of the user who requests the activities
     * @param activitySearchDto the search criteria
     * @return a collection of the payment activities
     * @throws NotFoundException if the group is not found
     */
    Collection<ActivityDetailDto> getPaymentActivitiesByGroupId(long groupId, String requesterEmail, ActivitySearchDto activitySearchDto) throws NotFoundException;
}
