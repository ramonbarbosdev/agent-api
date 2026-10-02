package com.agentapi.web;

public record AuthResponse(String accessToken, String userId, String email) {
}
