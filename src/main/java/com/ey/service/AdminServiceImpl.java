package com.ey.service;

import com.ey.dto.HrResponse;
import com.ey.entity.User;
import com.ey.exception.NotFoundException;
import com.ey.repository.UserRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminServiceImpl implements AdminService {

	private final UserRepository userRepo;

	public AdminServiceImpl(UserRepository userRepo) {
		this.userRepo = userRepo;
	}

	@Override
	public List<HrResponse> getPendingHrs() {
		return userRepo.findByRoleAndHrStatus(com.ey.entity.Role.HR, com.ey.entity.HrStatus.PENDING).stream()
				.map(this::toDto).collect(Collectors.toList());
	}

	@Override
	@Transactional
	public HrResponse approveHr(Long hrId) {
		User hr = userRepo.findById(hrId).orElseThrow(() -> new NotFoundException("HR not found"));
		hr.setHrStatus(com.ey.entity.HrStatus.APPROVED);
		userRepo.save(hr);
		return toDto(hr);
	}

	@Override
	@Transactional
	public HrResponse rejectHr(Long hrId) {
		User hr = userRepo.findById(hrId).orElseThrow(() -> new NotFoundException("HR not found"));
		hr.setHrStatus(com.ey.entity.HrStatus.REJECTED);
		userRepo.save(hr);
		return toDto(hr);
	}

	@Override
	@Transactional
	public void deleteUser(Long userId) {
		if (!userRepo.existsById(userId))
			throw new NotFoundException("User not found");
		userRepo.deleteById(userId);
	}

	private HrResponse toDto(User u) {
		HrResponse r = new HrResponse();
		r.setId(u.getId());
		r.setName(u.getName());
		r.setEmail(u.getEmail());
		r.setCompany(u.getCompany() != null ? u.getCompany().getName() : null);
		r.setHrStatus(u.getHrStatus() != null ? u.getHrStatus().name() : null);
		return r;
	}
}