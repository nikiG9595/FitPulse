package com.fitpulse.controller;

import com.fitpulse.config.SecurityConfig;
import com.fitpulse.service.ProgressService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProgressController.class)
@Import(SecurityConfig.class)
class ProgressControllerSecurityTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProgressService progressService;

    @Test
    void unauthenticatedUserIsRedirectedToLogin() throws Exception {
        mockMvc.perform(get("/progress"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void memberCanViewProgress() throws Exception {
        when(progressService.getCurrentUserProgress()).thenReturn(List.of());

        mockMvc.perform(get("/progress").with(user("member").roles("MEMBER")))
                .andExpect(status().isOk());
    }

    @Test
    void adminCannotAccessMemberProgress() throws Exception {
        mockMvc.perform(get("/progress").with(user("admin").roles("ADMIN")))
                .andExpect(status().isForbidden());
    }

    @Test
    void createRequiresCsrfToken() throws Exception {
        mockMvc.perform(post("/progress/create")
                        .with(user("member").roles("MEMBER"))
                        .param("weight", "75.5")
                        .param("recordedAt", "2025-01-01"))
                .andExpect(status().isForbidden());
    }

    @Test
    void memberCanDeleteOwnRecordWithCsrfToken() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(post("/progress/{id}/delete", id)
                        .with(user("member").roles("MEMBER"))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection());

        verify(progressService).delete(id);
    }
}
