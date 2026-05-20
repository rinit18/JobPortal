package com.jobportal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Certification {
	private String name;
    private String issuer;
    private String issueDate; // e.g. "Oct 2025"
    private String certificateId;
}
