package com.example.resourcemanagement.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ActionPayloadDto {
    private String accountId;                      // "A1"
    private LocalDateTime requestedAt;             // "2025-12-31T09:00:00+09:00"
    private LocalDateTime expiresAt;               // "2026-01-31T09:00:00+09:00"
    private List<ResourceResponseDto> resources;   // [{ "type": "cpu", ... }]
}


