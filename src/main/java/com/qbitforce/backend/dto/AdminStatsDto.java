package com.qbitforce.backend.dto;

public record AdminStatsDto(
        long gallery,
        long videos,
        long publications,
        long press,
        long contacts,
        long unreadContacts) {}
