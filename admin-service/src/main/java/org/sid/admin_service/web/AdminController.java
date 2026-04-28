package org.sid.admin_service.web;

import org.sid.admin_service.entities.Admin;
import org.sid.admin_service.services.AdminService;
import org.sid.admin_service.dto.CompanyDto;
import org.sid.admin_service.dto.FreelancerDto;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping
    public Admin getAdmin() {
        return adminService.getAdmin();
    }

    @PutMapping
    public Admin updateAdmin(@RequestBody Admin admin) {
        return adminService.updateAdmin(admin);
    }

    @PostMapping("/companies/{id}/approve")
    public void approveCompany(@PathVariable Long id) {
        adminService.approveCompany(id);
    }

    @GetMapping("/companies/pending")
    public List<CompanyDto> getPendingCompanies() {
        return adminService.getPendingCompanies();
    }

    @GetMapping("/freelancers")
    public List<FreelancerDto> getAllFreelancers() {
        return adminService.getAllFreelancers();
    }

    @PostMapping("/companies/{id}/reject")
    public CompanyDto rejectCompany(@PathVariable Long id, @RequestParam String reason) {
        return adminService.rejectCompany(id, reason);
    }

//    @PostMapping("/companies/{id}/suspend")
//    public void suspendCompany(@PathVariable Long id, @RequestParam String reason) {
//        adminService.suspendCompany(id, reason);
//    }

    @PostMapping("/freelancers/{id}/suspend")
    public void suspendFreelancer(@PathVariable Long id, @RequestParam String reason) {
        adminService.suspendFreelancer(id, reason);
    }

    @DeleteMapping("/companies/{id}")
    public void deleteCompany(@PathVariable Long id, @RequestParam String reason) {
        adminService.deleteCompany(id, reason);
    }

    @DeleteMapping("/freelancers/{id}")
    public void deleteFreelancer(@PathVariable Long id, @RequestParam String reason) {
        adminService.deleteFreelancer(id, reason);
    }
}