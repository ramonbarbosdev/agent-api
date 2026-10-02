package com.agentapi.web;

public record CursorStatusDto(boolean connected, boolean testOk, boolean sidecarReachable) {
}
