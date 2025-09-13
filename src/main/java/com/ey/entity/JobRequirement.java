package com.ey.entity;
 
import jakarta.persistence.*;
 
@Entity
@Table(name = "job_requirements")
public class JobRequirement {
 
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
 
    private String requirement;
 
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;
 
    // getters & setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getRequirement() { return requirement; }
    public void setRequirement(String requirement) { this.requirement = requirement; }
    public Job getJob() { return job; }
    public void setJob(Job job) { this.job = job; }
}