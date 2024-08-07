package at.ac.tuwien.sepr.groupphase.backend.repository;

import at.ac.tuwien.sepr.groupphase.backend.entity.Activity;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.GroupEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Set;

@Repository
public interface ActivityRepository extends JpaRepository<Activity, Long> {


    /**
     * Find all expense activities of user.
     *
     * @param user the user id
     * @param from the from
     * @param to   the to
     * @return Set of expense activities
     */
    @Query(
        "SELECT a FROM Activity a"
            + " JOIN a.expense e"
            + " JOIN e.participants ep"
            + " WHERE "
            + " KEY(ep) = :user AND"
            + " (a.category = 'EXPENSE' OR a.category = 'EXPENSE_UPDATE' OR a.category = 'EXPENSE_DELETE' OR a.category = 'EXPENSE_RECOVER') AND"
            + " (:from IS NULL OR a.timestamp >= :from) AND"
            + " (:to IS NULL OR a.timestamp <= :to)"
            + " ORDER BY a.timestamp DESC"
    )
    Set<Activity> findExpenseActivitiesByUser(ApplicationUser user, LocalDateTime from, LocalDateTime to);

    /**
     * Find all expense activities of group.
     *
     * @param group the group
     * @return Set of expense activities
     */
    @Query(
        "SELECT a FROM Activity a"
            + " WHERE "
            + " a.group = :group AND"
            + " (a.category = 'EXPENSE' OR a.category = 'EXPENSE_UPDATE' OR a.category = 'EXPENSE_DELETE' OR a.category = 'EXPENSE_RECOVER') AND"
            + " (:from IS NULL OR a.timestamp >= :from) AND"
            + " (:to IS NULL OR a.timestamp <= :to)"
            + " ORDER BY a.timestamp DESC"
    )
    Set<Activity> findExpenseActivitiesByGroup(GroupEntity group, LocalDateTime from, LocalDateTime to);

    @Query(
        "SELECT a FROM Activity a"
            + " WHERE "
            + " a.group = :group AND"
            + " (a.category = 'PAYMENT' OR a.category = 'PAYMENT_UPDATE' OR a.category = 'PAYMENT_DELETE' OR a.category = 'PAYMENT_RECOVER') AND"
            + " (:from IS NULL OR a.timestamp >= :from) AND"
            + " (:to IS NULL OR a.timestamp <= :to)"
            + " ORDER BY a.timestamp DESC"
    )
    Set<Activity> findPaymentActivitiesByGroup(GroupEntity group, LocalDateTime from, LocalDateTime to);
}
