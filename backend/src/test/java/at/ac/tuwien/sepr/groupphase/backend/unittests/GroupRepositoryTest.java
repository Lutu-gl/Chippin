package at.ac.tuwien.sepr.groupphase.backend.unittests;

import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.GroupEntity;
import at.ac.tuwien.sepr.groupphase.backend.repository.GroupRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@ActiveProfiles("test")
public class GroupRepositoryTest {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupRepository groupRepository;

    @BeforeEach
    public void beforeEach() {
        userRepository.deleteAll();
        groupRepository.deleteAll();
    }

    @Test
    @Transactional
    @Rollback
    public void testCreateGroupWith3MembersAndGroupName() {
        ApplicationUser user = createUserWithGroups();

        Set<GroupEntity> foundGroups = userRepository.findGroupsByUserEmail(user.getEmail());


        assertAll(
            () -> assertNotNull(foundGroups, "Groups should not be null"),
            () -> assertFalse(foundGroups.isEmpty(), "Groups should not be empty"),
            () -> assertTrue(foundGroups.containsAll(user.getGroups()), "Groups should contain all assigned groups")
        );
    }

    @Test
    @Transactional
    @Rollback
    public void testUpdateGroupWith3MembersAndGroupName() {
        ApplicationUser user = createUserWithGroups();

        Set<GroupEntity> foundGroups = userRepository.findGroupsByUserEmail(user.getEmail());

        assertAll(
            () -> assertNotNull(foundGroups, "Groups should not be null"),
            () -> assertFalse(foundGroups.isEmpty(), "Groups should not be empty"),
            () -> assertTrue(foundGroups.containsAll(user.getGroups()), "Groups should contain all assigned groups")
        );

        GroupEntity group1 = foundGroups.stream().filter(group -> group.getGroupName().equals("Developers")).findFirst().get();
        group1.setGroupName("NewDevelopers");

        groupRepository.save(group1);

        Set<GroupEntity> updatedGroups = userRepository.findGroupsByUserEmail(user.getEmail());

        assertAll(
            () -> assertNotNull(updatedGroups, "Groups should not be null"),
            () -> assertFalse(updatedGroups.isEmpty(), "Groups should not be empty"),
            () -> assertTrue(updatedGroups.contains(group1), "Groups should contain updated group")
        );
    }

    private ApplicationUser createUserWithGroups() {
        GroupEntity group1 = GroupEntity.builder()
            .groupName("Developers")
            .users(new HashSet<>())
            .build();

        GroupEntity group2 = GroupEntity.builder()
            .groupName("Managers")
            .users(new HashSet<>())
            .build();

        ApplicationUser user = ApplicationUser.builder()
            .email("alice@test.com")
            .password("$2a$10$CMt4NPOyYWlEUP6zg6yNxewo24xZqQnmOPwNGycH0OW4O7bidQ5CG")
            .admin(true)
            .groups(new HashSet<>())
            .build();

        user.getGroups().add(group1);
        user.getGroups().add(group2);
        group1.getUsers().add(user);
        group2.getUsers().add(user);

        groupRepository.save(group1);
        groupRepository.save(group2);
        userRepository.save(user);

        return user;
    }
}
