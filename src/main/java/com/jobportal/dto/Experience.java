package com.jobportal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Experience {
    private String title;
    private String company;
    private String location;
    private String startDate;   // e.g. "Apr 2022"
    private String endDate;     // e.g. "Present" or "Mar 2024"
    private Boolean working;
    private String description;
}
