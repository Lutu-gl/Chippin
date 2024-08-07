package at.ac.tuwien.sepr.groupphase.backend.unittests.endpointtests;

import at.ac.tuwien.sepr.groupphase.backend.basetest.TestData;
import at.ac.tuwien.sepr.groupphase.backend.config.properties.SecurityProperties;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.group.GroupCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.security.JwtTokenizer;
import at.ac.tuwien.sepr.groupphase.backend.service.GroupService;
import at.ac.tuwien.sepr.groupphase.backend.service.SecurityService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class GroupEndpointTest implements TestData {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GroupService groupService;

    @MockBean
    private SecurityService securityService;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    private JwtTokenizer jwtTokenizer;

    @Autowired
    private SecurityProperties securityProperties;

    List<String> ADMIN_ROLES = new ArrayList<>() {
        {
            add("ROLE_ADMIN");
            add("ROLE_USER");
        }
    };

    @Test
    @Transactional
    @Rollback
    @WithMockUser("testUser1@example.com")
    public void testCreateGroupValid() throws Exception {
        GroupCreateDto mockResponse = GroupCreateDto.builder()
            .groupName("Test Group")
            .members(Arrays.stream(new String[]{"testUser1@example.com", "testUser2@example.com"}).collect(Collectors.toSet()))
            .build();
        when(groupService.create(any(), anyString())).thenReturn(mockResponse);

        String groupJson = "{\"name\":\"Test Group\",\"members\":[\"testUser1@example.com\",\"testUser2@example.com\"]}";
        byte[] body = mockMvc.perform(MockMvcRequestBuilders
                .post("/api/v1/group")
                //.header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("testUser1@example.com", ADMIN_ROLES))
                .contentType(MediaType.APPLICATION_JSON)
                .content(groupJson))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsByteArray();

        GroupCreateDto groupResult = objectMapper.readerFor(GroupCreateDto.class)
            .readValue(body);

        assertNotNull(groupResult, "Response should not be null");
        assertEquals("Test Group", groupResult.getGroupName(), "Group name should match");
        assertTrue(groupResult.getMembers().contains("testUser1@example.com"), "Member list should contain testUser1@example.com");
        assertTrue(groupResult.getMembers().contains("testUser2@example.com"), "Member list should contain testUser2@example.com");
        assertEquals(groupResult.getMembers().size(), 2, "Members should have the size 2");
    }

    @Test
    @Transactional
    @Rollback
    @WithMockUser("testUser1@example.com")
    public void testUpdateGroupValid() throws Exception {
        GroupCreateDto mockResponse = GroupCreateDto.builder()
            .groupName("Test Group")
            .members(Arrays.stream(new String[]{"testUser1@example.com", "testUser2@example.com"}).collect(Collectors.toSet()))
            .build();
        when(groupService.update(any(), anyString())).thenReturn(mockResponse);

        String groupUpdateJson = "{\"name\":\"Test Group\",\"members\":[\"testUser1@example.com\",\"testUser2@example.com\"]}";

        when(securityService.hasCorrectId(any())).thenReturn(true);
        when(securityService.isGroupMember(any())).thenReturn(true);

        byte[] body = mockMvc.perform(MockMvcRequestBuilders
                .put("/api/v1/group/1")
                //.header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("testUser1@example.com", ADMIN_ROLES))
                .contentType(MediaType.APPLICATION_JSON)
                .content(groupUpdateJson))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsByteArray();

        GroupCreateDto groupResult = objectMapper.readerFor(GroupCreateDto.class)
            .readValue(body);

        assertNotNull(groupResult, "Response should not be null");
        assertEquals("Test Group", groupResult.getGroupName(), "Group name should match");
        assertTrue(groupResult.getMembers().contains("testUser1@example.com"), "Member list should contain testUser1@example.com");
        assertTrue(groupResult.getMembers().contains("testUser2@example.com"), "Member list should contain testUser2@example.com");
        assertEquals(groupResult.getMembers().size(), 2, "Members should have the size 2");
    }

    @Test
    @Transactional
    @Rollback
    @WithMockUser("testUser1@example.com")
    public void testCreateGroupValidationException() throws Exception {
        // Arrange: Mock the service to throw ValidationException when called
        doThrow(new ValidationException("Invalid data", new ArrayList<>())).when(groupService).create(any(), anyString());

        // Act: Perform POST request with invalid data
        String groupJson = "{\"name\":\"   \"}"; // Invalid data due to empty name
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/group")
                //.header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("testUser1@example.com", ADMIN_ROLES))
                .contentType(MediaType.APPLICATION_JSON)
                .content(groupJson))
            .andExpect(status().isUnprocessableEntity())
            .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        assertTrue(responseBody.contains("Invalid data"), "Response body should contain the error message 'Invalid data'");
    }


    @Test
    @Transactional
    @Rollback
    @WithMockUser("userNotInMember@example.com")
    public void testCreateGroupConflictException() throws Exception {
        // Arrange: Mock the service to throw ConflictException when called
        doThrow(new ConflictException("User not in members list", new ArrayList<>())).when(groupService).create(any(), anyString());

        // Act: Perform POST request with conflicting data
        String groupJson = "{\"name\":\"Test Group\",\"members\":[\"testUser1@example.com\",\"testUser2@example.com\"]}";
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/group")
                //.header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("userNotInMember@example.com", ADMIN_ROLES))
                .contentType(MediaType.APPLICATION_JSON)
                .content(groupJson))
            .andExpect(status().isConflict())
            .andReturn();

        // Assert: Optionally, check the response content or message
        String responseBody = result.getResponse().getContentAsString();
        assertTrue(responseBody.contains("User not in members list"), "Response body should contain the error message 'User not in members list'");
    }

}
