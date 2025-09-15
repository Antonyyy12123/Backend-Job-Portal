package com.ey.service;

import com.ey.dto.UpdateStatusRequest;
import com.ey.entity.*;
import com.ey.exception.ConflictException;
import com.ey.exception.ForbiddenException;
import com.ey.repository.ApplicationRepository;
import com.ey.repository.JobRepository;
import com.ey.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.core.context.SecurityContextHolder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class ApplicationServiceImplTest {

    @Mock private ApplicationRepository appRepo;
    @Mock private JobRepository jobRepo;
    @Mock private UserRepository userRepo;

    private ApplicationServiceImpl appService;

    @BeforeEach
    void setUp() throws Exception {
        Path tempDir = Files.createTempDirectory("test-uploads-");
        String uploadDir = tempDir.toAbsolutePath().toString();
        appService = new ApplicationServiceImpl(appRepo, jobRepo, userRepo, uploadDir);

      
        SecurityContextImpl ctx = new SecurityContextImpl();
        ctx.setAuthentication(new UsernamePasswordAuthenticationToken("seeker@example.com", null));
        SecurityContextHolder.setContext(ctx);
    }

    @Test
    void applyToJob_success() {
        User seeker = new User();
        seeker.setId(1L);
        seeker.setEmail("seeker@example.com");
        seeker.setRole(Role.SEEKER);
        seeker.setName("Seeker One");

        Job job = new Job();
        job.setId(100L);
        job.setTitle("Backend Developer");

        when(userRepo.findByEmail("seeker@example.com")).thenReturn(Optional.of(seeker));
        when(jobRepo.findById(100L)).thenReturn(Optional.of(job));
        when(appRepo.existsByJobIdAndSeekerId(100L, 1L)).thenReturn(false);
        when(appRepo.save(any(Application.class))).thenAnswer(inv -> {
            Application a = inv.getArgument(0);
            a.setId(10L);
            return a;
        });

        var resp = appService.applyToJob(100L, null);

        assertEquals(10L, resp.getId());
        assertEquals("APPLIED", resp.getStatus());
        assertEquals("Backend Developer", resp.getJobTitle());
        assertEquals("Seeker One", resp.getSeekerName());
    }

    @Test
    void applyToJob_alreadyApplied_throwsConflict() {
        User seeker = new User();
        seeker.setId(1L);
        seeker.setEmail("seeker@example.com");

        Job job = new Job();
        job.setId(100L);

        when(userRepo.findByEmail("seeker@example.com")).thenReturn(Optional.of(seeker));
        when(jobRepo.findById(100L)).thenReturn(Optional.of(job));
        when(appRepo.existsByJobIdAndSeekerId(100L, 1L)).thenReturn(true);

        assertThrows(ConflictException.class, () -> appService.applyToJob(100L, null));
    }

    @Test
    void updateStatus_asHR_success() {
        
        SecurityContextImpl hrCtx = new SecurityContextImpl();
        hrCtx.setAuthentication(new UsernamePasswordAuthenticationToken("hr@example.com", null));
        SecurityContextHolder.setContext(hrCtx);

        User hr = new User();
        hr.setId(2L);
        hr.setEmail("hr@example.com");
        hr.setRole(Role.HR);

        User seeker = new User();
        seeker.setId(3L);
        seeker.setName("Applied Seeker");
        seeker.setEmail("applied@example.com");

        Job job = new Job();
        job.setHr(hr);
        job.setTitle("Fullstack Dev");

        Application app = new Application();
        app.setId(5L);
        app.setJob(job);
        app.setSeeker(seeker);

        when(userRepo.findByEmail("hr@example.com")).thenReturn(Optional.of(hr));
        when(appRepo.findById(5L)).thenReturn(Optional.of(app));
        when(appRepo.save(any(Application.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateStatusRequest req = new UpdateStatusRequest();
        req.setStatus("ACCEPTED");

        var resp = appService.updateStatus(5L, req);

        assertEquals("ACCEPTED", resp.getStatus());
        assertEquals("Fullstack Dev", resp.getJobTitle());
        assertEquals("Applied Seeker", resp.getSeekerName());
    }

    @Test
    void updateStatus_asSeeker_throwsForbidden() {

        User seeker = new User();
        seeker.setId(1L);
        seeker.setEmail("seeker@example.com");
        seeker.setRole(Role.SEEKER);

        Application app = new Application();
        app.setId(5L);
        Job job = new Job();
        job.setHr(new User());
        app.setJob(job);

        when(userRepo.findByEmail("seeker@example.com")).thenReturn(Optional.of(seeker));
        when(appRepo.findById(5L)).thenReturn(Optional.of(app));

        UpdateStatusRequest req = new UpdateStatusRequest();
        req.setStatus("ACCEPTED");

        assertThrows(ForbiddenException.class, () -> appService.updateStatus(5L, req));
    }
}
