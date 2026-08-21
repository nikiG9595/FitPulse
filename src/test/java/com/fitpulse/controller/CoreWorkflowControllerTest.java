package com.fitpulse.controller;

import com.fitpulse.config.SecurityConfig;
import com.fitpulse.exception.FitPulseException;
import com.fitpulse.model.dto.GymClassRequest;
import com.fitpulse.model.dto.MembershipRequest;
import com.fitpulse.service.GymClassService;
import com.fitpulse.service.MembershipService;
import com.fitpulse.service.UserService;
import com.fitpulse.service.WorkoutBookingService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({AuthController.class, GymClassController.class, MembershipController.class,
        HomeController.class, WorkoutBookingController.class})
@Import(SecurityConfig.class)
class CoreWorkflowControllerTest {
    @Autowired MockMvc mockMvc;
    @MockitoBean UserService userService;
    @MockitoBean GymClassService gymClassService;
    @MockitoBean MembershipService membershipService;
    @MockitoBean WorkoutBookingService bookingService;

    @Test
    void publicPagesRemainAccessible() throws Exception {
        when(gymClassService.getAll()).thenReturn(List.of());
        when(membershipService.getAll()).thenReturn(List.of());
        mockMvc.perform(get("/login")).andExpect(status().isOk()).andExpect(view().name("auth/login"));
        mockMvc.perform(get("/register")).andExpect(status().isOk()).andExpect(model().attributeExists("registerRequest"));
        mockMvc.perform(get("/")).andExpect(status().isOk()).andExpect(view().name("home"));
    }

    @Test
    void validRegistrationCallsServiceAndRedirects() throws Exception {
        mockMvc.perform(post("/register").with(csrf())
                        .param("username", "newmember").param("email", "new@example.com")
                        .param("password", "secret1").param("confirmPassword", "secret1"))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/login?registered"));
        verify(userService).register(any());
    }

    @Test
    void invalidAndDuplicateRegistrationReturnForm() throws Exception {
        mockMvc.perform(post("/register").with(csrf()).param("username", "x"))
                .andExpect(status().isOk()).andExpect(view().name("auth/register"));
        doThrow(new FitPulseException("Username already exists")).when(userService).register(any());
        mockMvc.perform(post("/register").with(csrf())
                        .param("username", "newmember").param("email", "new@example.com")
                        .param("password", "secret1").param("confirmPassword", "secret1"))
                .andExpect(status().isOk()).andExpect(model().attribute("globalError", "Username already exists"));
    }

    @Test
    void onlyAdminCanOpenClassCreationAndValidFormCreatesClass() throws Exception {
        mockMvc.perform(get("/classes/create").with(user("member").roles("MEMBER")))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/classes/create").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk()).andExpect(model().attributeExists("memberships", "intensities"));

        UUID membershipId = UUID.randomUUID();
        mockMvc.perform(post("/classes/create").with(user("admin").roles("ADMIN")).with(csrf())
                        .param("title", "Morning yoga").param("trainerName", "Alex Smith")
                        .param("startsAt", "2035-01-02T10:00").param("capacity", "12")
                        .param("intensity", "LOW").param("description", "A gentle morning workout")
                        .param("requiredMembershipId", membershipId.toString()))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/classes"));
        ArgumentCaptor<GymClassRequest> request = ArgumentCaptor.forClass(GymClassRequest.class);
        verify(gymClassService).create(request.capture());
        assertEquals(membershipId, request.getValue().getRequiredMembershipId());
    }

    @Test
    void invalidClassFormIsRedisplayedWithReferenceData() throws Exception {
        mockMvc.perform(post("/classes/create").with(user("admin").roles("ADMIN")).with(csrf())
                        .param("title", "x"))
                .andExpect(status().isOk()).andExpect(view().name("classes/form"))
                .andExpect(model().attributeExists("memberships", "intensities"));
        verify(gymClassService, never()).create(any());
    }

    @Test
    void bookingReportsBusinessFailureAsFlashMessage() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new FitPulseException("This class is fully booked")).when(bookingService).book(id);
        mockMvc.perform(post("/classes/{id}/book", id).with(user("member").roles("MEMBER")).with(csrf()))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/classes/" + id))
                .andExpect(flash().attribute("error", "This class is fully booked"));
    }

    @Test
    void membershipAdministrationAndSelectionFollowAuthorizationRules() throws Exception {
        UUID id = UUID.randomUUID();
        mockMvc.perform(post("/memberships/{id}/choose", id).with(user("member").roles("MEMBER")).with(csrf()))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/profile"));
        verify(membershipService).chooseMembership(id);
        mockMvc.perform(post("/memberships/{id}/delete", id).with(user("member").roles("MEMBER")).with(csrf()))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/memberships/{id}/delete", id).with(user("admin").roles("ADMIN")).with(csrf()))
                .andExpect(status().is3xxRedirection());
        verify(membershipService).delete(id);
    }

    @Test
    void invalidMembershipFormRedisplaysTypesAndValidFormCreates() throws Exception {
        mockMvc.perform(post("/memberships/create").with(user("admin").roles("ADMIN")).with(csrf()))
                .andExpect(status().isOk()).andExpect(model().attributeExists("types"));
        mockMvc.perform(post("/memberships/create").with(user("admin").roles("ADMIN")).with(csrf())
                        .param("type", "BASIC").param("title", "Basic plan").param("price", "25.00")
                        .param("durationDays", "30").param("description", "Basic membership plan"))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/memberships"));
        verify(membershipService).create(any(MembershipRequest.class));
    }

    @Test
    void userCanListAndCancelOwnBookings() throws Exception {
        UUID id = UUID.randomUUID();
        when(bookingService.getCurrentUserBookings()).thenReturn(List.of());
        mockMvc.perform(get("/bookings").with(user("member").roles("MEMBER")))
                .andExpect(status().isOk()).andExpect(view().name("bookings/list"));
        mockMvc.perform(post("/bookings/{id}/cancel", id).with(user("member").roles("MEMBER")).with(csrf()))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/bookings"));
        verify(bookingService).cancel(id);
    }
}
