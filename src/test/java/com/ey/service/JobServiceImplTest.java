package com.ey.service;

import com.ey.dto.JobCreateRequest;
import com.ey.entity.Company;
import com.ey.entity.HrStatus;
import com.ey.entity.Job;
import com.ey.entity.Role;
import com.ey.entity.User;
import com.ey.exception.ForbiddenException;
import com.ey.repository.CompanyRepository;
import com.ey.repository.JobRepository;
import com.ey.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Clean JobServiceImpl tests: use SecurityContextImpl to avoid Mockito stubbing of SecurityContext.
 */
@ExtendWith(MockitoExtension.class)
class JobServiceImplTest {

    @Mock private JobRepository jobRepo;
    @Mock private UserRepository userRepo;
    @Mock private CompanyRepository companyRepo;

    @InjectMocks
    private JobServiceImpl jobService;

    @BeforeEach
    void setUp() {
        // default: put a SecurityContextImpl with HR email (tests can override)
        SecurityContextImpl ctx = new SecurityContextImpl();
        ctx.setAuthentication(new UsernamePasswordAuthenticationToken("hr@example.com", null));
        SecurityContextHolder.setContext(ctx);
    }

    @Test
    void createJob_asHR_success() {
        // prepare HR user
        User hr = new User();
        hr.setId(1L);
        hr.setEmail("hr@example.com");
        hr.setRole(Role.HR);
        hr.setHrStatus(HrStatus.APPROVED);
        Company company = new Company();
        company.setId(10L);
        hr.setCompany(company);

        when(userRepo.findByEmail("hr@example.com")).thenReturn(Optional.of(hr));
        when(jobRepo.save(any(Job.class))).thenAnswer(inv -> {
            Job j = inv.getArgument(0);
            j.setId(100L);
            return j;
        });

        JobCreateRequest req = new JobCreateRequest();
        req.setTitle("Java Developer");
        req.setDescription("Backend dev role");
        req.setLocation("Remote");
        req.setSalary(new BigDecimal("60000"));

        var resp = jobService.createJob(req);

        assertEquals("Java Developer", resp.getTitle());
        assertEquals("APPROVED", resp.getStatus());
        verify(jobRepo, times(1)).save(any(Job.class));
    }

    @Test
    void createJob_asNonHR_throwsForbidden() {
        // place seeker in security context
        SecurityContextImpl seekerCtx = new SecurityContextImpl();
        seekerCtx.setAuthentication(new UsernamePasswordAuthenticationToken("seeker@example.com", null));
        SecurityContextHolder.setContext(seekerCtx);

        User seeker = new User();
        seeker.setEmail("seeker@example.com");
        seeker.setRole(Role.SEEKER);

        when(userRepo.findByEmail("seeker@example.com")).thenReturn(Optional.of(seeker));

        JobCreateRequest req = new JobCreateRequest();
        req.setTitle("Python Developer");
        req.setDescription("Backend role");
        req.setLocation("Remote");
        req.setSalary(new BigDecimal("70000"));

        assertThrows(ForbiddenException.class, () -> jobService.createJob(req));
    }
}
