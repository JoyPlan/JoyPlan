package com.joyplan;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class WebSecurityTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void anonymousUserIsRedirectedToLogin() throws Exception {
        mvc.perform(get("/plans")).andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void landingPageIsPublic() throws Exception {
        mvc.perform(get("/")).andExpect(status().isOk());
    }

    @Test
    void validLoginSucceedsAndWrongPasswordFails() throws Exception {
        mvc.perform(formLogin("/login").userParameter("email").user("parent@joyplan.ae").password("Parent@123"))
                .andExpect(authenticated());
        mvc.perform(formLogin("/login").userParameter("email").user("parent@joyplan.ae").password("wrong"))
                .andExpect(unauthenticated());
    }

    @Test
    @WithUserDetails("parent@joyplan.ae")
    void parentCanOpenPagesButNotAdmin() throws Exception {
        mvc.perform(get("/dashboard")).andExpect(status().isOk());
        mvc.perform(get("/family")).andExpect(status().isOk());
        mvc.perform(get("/plans/new")).andExpect(status().isOk());
        mvc.perform(get("/admin")).andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails("parent@joyplan.ae")
    void parentCanGenerateAPlanThroughTheForm() throws Exception {
        mvc.perform(post("/plans").with(csrf())
                        .param("area", "AL_REEM").param("mood", "HAPPY").param("energy", "MEDIUM")
                        .param("budget", "400").param("maxDistanceKm", "20")
                        .param("startTime", "10:00").param("endTime", "15:00")
                        .param("outingDate", "2026-11-28").param("setting", "ANY")
                        .param("adults", "2").param("includeMeal", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/plans/*"));
    }

    @Test
    @WithUserDetails("parent@joyplan.ae")
    void invalidTimeWindowIsRejected() throws Exception {
        mvc.perform(post("/plans").with(csrf())
                        .param("area", "AL_REEM").param("mood", "HAPPY").param("energy", "MEDIUM")
                        .param("budget", "400").param("maxDistanceKm", "20")
                        .param("startTime", "10:00").param("endTime", "10:30")
                        .param("outingDate", "2026-11-28").param("setting", "ANY").param("adults", "2"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("at least 60 minutes")));
    }

    @Test
    @WithUserDetails("admin@joyplan.ae")
    void adminCanOpenVenueManagement() throws Exception {
        mvc.perform(get("/admin")).andExpect(status().isOk());
        mvc.perform(get("/admin/venues")).andExpect(status().isOk());
        mvc.perform(get("/admin/venues/new")).andExpect(status().isOk());
    }

    @Test
    void registrationCreatesAnAccount() throws Exception {
        mvc.perform(post("/register").with(csrf())
                        .param("displayName", "Test Parent").param("email", "new.parent@example.com")
                        .param("password", "Secret123").param("confirmPassword", "Secret123")
                        .param("homeArea", "CORNICHE"))
                .andExpect(redirectedUrl("/login"));
    }
}
