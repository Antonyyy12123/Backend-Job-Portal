package com.ey.service;
 
import com.ey.dto.HrResponse;
import java.util.List;
 
public interface AdminService {
    List<HrResponse> getPendingHrs();
    HrResponse approveHr(Long hrId);
    HrResponse rejectHr(Long hrId);
    void deleteUser(Long userId);
}