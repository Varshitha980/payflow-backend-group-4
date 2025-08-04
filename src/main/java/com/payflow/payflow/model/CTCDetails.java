package com.payflow.payflow.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.sql.Timestamp;

@Entity
@Table(name = "ctc_details")
public class CTCDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ctc_id")
    private Long ctcId;

    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    @Column(name = "basic_salary")
    private BigDecimal basicSalary;

    @Column(name = "hra")
    private BigDecimal hra;

    @Column(name = "allowances")
    private BigDecimal allowances;

    @Column(name = "bonuses")
    private BigDecimal bonuses;

    @Column(name = "pf_contribution")
    private BigDecimal pfContribution;

    @Column(name = "gratuity")
    private BigDecimal gratuity;

    @Column(name = "total_ctc")
    private BigDecimal totalCTC;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "da")
    private BigDecimal da;

    @Column(name = "special_allowance")
    private BigDecimal specialAllowance;

    // Constructors
    public CTCDetails() {
    }

    // Getters and Setters
    public Long getCtcId() {
        return ctcId;
    }

    public void setCtcId(Long ctcId) {
        this.ctcId = ctcId;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public LocalDate getEffectiveFrom() {
        return effectiveFrom;
    }

    public void setEffectiveFrom(LocalDate effectiveFrom) {
        this.effectiveFrom = effectiveFrom;
    }

    public BigDecimal getBasicSalary() {
        return basicSalary;
    }

    public void setBasicSalary(BigDecimal basicSalary) {
        this.basicSalary = basicSalary;
    }

    public BigDecimal getHra() {
        return hra;
    }

    public void setHra(BigDecimal hra) {
        this.hra = hra;
    }

    public BigDecimal getAllowances() {
        return allowances;
    }

    public void setAllowances(BigDecimal allowances) {
        this.allowances = allowances;
    }

    public BigDecimal getBonuses() {
        return bonuses;
    }

    public void setBonuses(BigDecimal bonuses) {
        this.bonuses = bonuses;
    }

    public BigDecimal getPfContribution() {
        return pfContribution;
    }

    public void setPfContribution(BigDecimal pfContribution) {
        this.pfContribution = pfContribution;
    }

    public BigDecimal getGratuity() {
        return gratuity;
    }

    public void setGratuity(BigDecimal gratuity) {
        this.gratuity = gratuity;
    }

    public BigDecimal getTotalCTC() {
        return totalCTC;
    }

    public void setTotalCTC(BigDecimal totalCTC) {
        this.totalCTC = totalCTC;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public BigDecimal getDa() {
        return da;
    }

    public void setDa(BigDecimal da) {
        this.da = da;
    }

    public BigDecimal getSpecialAllowance() {
        return specialAllowance;
    }

    public void setSpecialAllowance(BigDecimal specialAllowance) {
        this.specialAllowance = specialAllowance;
    }
}