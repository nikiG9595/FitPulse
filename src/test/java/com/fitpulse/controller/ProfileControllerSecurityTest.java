package com.fitpulse.controller;

import com.fitpulse.config.SecurityConfig;
import com.fitpulse.model.dto.ProfileUpdateRequest;
import com.fitpulse.service.UserService;
import com.fitpulse.service.WorkoutBookingService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(ProfileController.class)
@Import(SecurityConfig.class)
class ProfileControllerSecurityTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private WorkoutBookingService bookingService;

    @Test
    void authenticatedUserCanOpenEditProfilePage() throws Exception {
        when(userService.getCurrentUserProfileUpdateRequest()).thenReturn(profileRequest("member", "member@example.com"));

        mockMvc.perform(get("/profile/edit").with(user("member").roles("MEMBER")))
                .andExpect(status().isOk())
                .andExpect(view().name("profile-edit"));
    }

    @Test
    void authenticatedUserCanUpdateOwnProfile() throws Exception {
        mockMvc.perform(post("/profile/edit")
                        .with(user("member").roles("MEMBER"))
                        .with(csrf())
                        .param("username", "updatedMember")
                        .param("email", "updated@example.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile?updated"));

        ArgumentCaptor<ProfileUpdateRequest> captor = ArgumentCaptor.forClass(ProfileUpdateRequest.class);
        verify(userService).updateCurrentUserProfile(captor.capture());
        assertEquals("updatedMember", captor.getValue().getUsername());
        assertEquals("updated@example.com", captor.getValue().getEmail());
    }

    @Test
    void unauthenticatedAccessIsRedirectedToLogin() throws Exception {
        mockMvc.perform(get("/profile/edit"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void roleCannotBeSubmittedThroughProfileEditing() throws Exception {
        mockMvc.perform(post("/profile/edit")
                        .with(user("member").roles("MEMBER"))
                        .with(csrf())
                        .param("username", "member")
                        .param("email", "member@example.com")
                        .param("role", "ADMIN"))
                .andExpect(status().is3xxRedirection());

        ArgumentCaptor<ProfileUpdateRequest> captor = ArgumentCaptor.forClass(ProfileUpdateRequest.class);
        verify(userService).updateCurrentUserProfile(captor.capture());
        assertEquals(ProfileUpdateRequest.class, captor.getValue().getClass());
    }

    private ProfileUpdateRequest profileRequest(String username, String email) {
        ProfileUpdateRequest request = new ProfileUpdateRequest();
        request.setUsername(username);
        request.setEmail(email);
        return request;
    }
}
