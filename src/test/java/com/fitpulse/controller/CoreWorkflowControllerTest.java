package com.fitpulse.controller;

import com.fitpulse.config.SecurityConfig;
import com.fitpulse.exception.FitPulseException;
import com.fitpulse.model.dto.GymClassRequest;
import com.fitpulse.model.dto.MembershipRequest;
import com.fitpulse.model.entity.GymClass;
import com.fitpulse.model.entity.Membership;
import com.fitpulse.model.entity.User;
import com.fitpulse.model.enums.ClassIntensity;
import com.fitpulse.model.enums.MembershipType;
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
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;
import java.math.BigDecimal;
import java.time.LocalDateTime;

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

    @Test
    void authenticatedUserCanListClassesAndViewClassDetails() throws Exception {
        UUID id = UUID.randomUUID();
        GymClass gymClass = gymClass(id);
        when(gymClassService.getAll()).thenReturn(List.of(gymClass));
        when(gymClassService.getById(id)).thenReturn(gymClass);
        when(bookingService.countBookingsForClass(id)).thenReturn(3L);

        mockMvc.perform(get("/classes").with(user("member").roles("MEMBER")))
                .andExpect(status().isOk()).andExpect(view().name("classes/list"))
                .andExpect(model().attribute("bookingService", bookingService));
        mockMvc.perform(get("/classes/{id}", id).with(user("member").roles("MEMBER")))
                .andExpect(status().isOk()).andExpect(view().name("classes/details"))
                .andExpect(model().attribute("gymClass", gymClass))
                .andExpect(model().attribute("bookingsCount", 3L));
    }

    @Test
    void adminCanOpenEditClassAndSubmitValidUpdate() throws Exception {
        UUID id = UUID.randomUUID();
        when(gymClassService.mapToRequest(id)).thenReturn(validClassRequest());
        mockMvc.perform(get("/classes/{id}/edit", id).with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk()).andExpect(view().name("classes/form"))
                .andExpect(model().attribute("classId", id));

        mockMvc.perform(post("/classes/{id}/edit", id).with(user("admin").roles("ADMIN")).with(csrf())
                        .param("title", "Updated yoga").param("trainerName", "Alex Smith")
                        .param("startsAt", "2035-01-02T10:00").param("capacity", "12")
                        .param("intensity", "LOW").param("description", "An updated morning workout")
                        .param("requiredMembershipId", UUID.randomUUID().toString()))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/classes/" + id));
        verify(gymClassService).update(eq(id), any(GymClassRequest.class));
    }

    @Test
    void invalidClassUpdatePreservesClassIdAndAdminCanDelete() throws Exception {
        UUID id = UUID.randomUUID();
        mockMvc.perform(post("/classes/{id}/edit", id).with(user("admin").roles("ADMIN")).with(csrf())
                        .param("title", "x"))
                .andExpect(status().isOk()).andExpect(view().name("classes/form"))
                .andExpect(model().attribute("classId", id));
        verify(gymClassService, never()).update(eq(id), any());

        mockMvc.perform(post("/classes/{id}/delete", id).with(user("admin").roles("ADMIN")).with(csrf()))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/classes"));
        verify(gymClassService).delete(id);
    }

    @Test
    void successfulBookingReturnsConfirmation() throws Exception {
        UUID id = UUID.randomUUID();
        mockMvc.perform(post("/classes/{id}/book", id).with(user("member").roles("MEMBER")).with(csrf()))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/classes/" + id))
                .andExpect(flash().attribute("success", "Class booked successfully."));
        verify(bookingService).book(id);
    }

    @Test
    void membershipListCreatePageAndEditPageExposeFormData() throws Exception {
        UUID id = UUID.randomUUID();
        MembershipRequest request = validMembershipRequest();
        when(membershipService.getAll()).thenReturn(List.of());
        when(membershipService.mapToRequest(id)).thenReturn(request);
        mockMvc.perform(get("/memberships").with(user("member").roles("MEMBER")))
                .andExpect(status().isOk()).andExpect(view().name("memberships/list"));
        mockMvc.perform(get("/memberships/create").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk()).andExpect(model().attributeExists("membershipRequest", "types"));
        mockMvc.perform(get("/memberships/{id}/edit", id).with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk()).andExpect(model().attribute("membershipId", id))
                .andExpect(model().attribute("membershipRequest", request));
    }

    @Test
    void membershipEditHandlesValidationAndSuccessfulUpdate() throws Exception {
        UUID id = UUID.randomUUID();
        mockMvc.perform(post("/memberships/{id}/edit", id).with(user("admin").roles("ADMIN")).with(csrf()))
                .andExpect(status().isOk()).andExpect(view().name("memberships/form"))
                .andExpect(model().attribute("membershipId", id));
        verify(membershipService, never()).update(eq(id), any());

        mockMvc.perform(post("/memberships/{id}/edit", id).with(user("admin").roles("ADMIN")).with(csrf())
                        .param("type", "PREMIUM").param("title", "Premium plan").param("price", "45.00")
                        .param("durationDays", "30").param("description", "Premium membership plan"))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/memberships"));
        verify(membershipService).update(eq(id), any(MembershipRequest.class));
    }

    @Test
    void authenticatedDashboardDisplaysClassesAndCurrentUser() throws Exception {
        User currentUser = new User();
        when(gymClassService.getAll()).thenReturn(List.of());
        when(userService.getCurrentUser()).thenReturn(currentUser);
        mockMvc.perform(get("/dashboard").with(user("member").roles("MEMBER")))
                .andExpect(status().isOk()).andExpect(view().name("dashboard"))
                .andExpect(model().attribute("user", currentUser));
    }

    private static GymClassRequest validClassRequest() {
        GymClassRequest request = new GymClassRequest();
        request.setTitle("Morning yoga"); request.setTrainerName("Alex Smith");
        request.setStartsAt(LocalDateTime.of(2035, 1, 2, 10, 0)); request.setCapacity(12);
        request.setIntensity(ClassIntensity.LOW); request.setDescription("A gentle morning workout");
        request.setRequiredMembershipId(UUID.randomUUID()); return request;
    }

    private static MembershipRequest validMembershipRequest() {
        MembershipRequest request = new MembershipRequest(); request.setType(MembershipType.BASIC);
        request.setTitle("Basic plan"); request.setPrice(new BigDecimal("25.00"));
        request.setDurationDays(30); request.setDescription("Basic membership plan"); return request;
    }

    private static GymClass gymClass(UUID id) {
        Membership membership = new Membership();
        membership.setType(MembershipType.BASIC);
        membership.setTitle("Basic plan");
        membership.setPrice(new BigDecimal("25.00"));
        membership.setDurationDays(30);
        membership.setDescription("Basic membership plan");

        GymClass gymClass = new GymClass();
        ReflectionTestUtils.setField(gymClass, "id", id);
        gymClass.setTitle("Morning yoga");
        gymClass.setTrainerName("Alex Smith");
        gymClass.setStartsAt(LocalDateTime.of(2035, 1, 2, 10, 0));
        gymClass.setCapacity(12);
        gymClass.setIntensity(ClassIntensity.LOW);
        gymClass.setDescription("A gentle morning workout");
        gymClass.setRequiredMembership(membership);
        return gymClass;
    }
}
