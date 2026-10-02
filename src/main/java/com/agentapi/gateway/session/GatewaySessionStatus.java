package com.agentapi.gateway.session;

public final class GatewaySessionStatus {

    public static final String CREATED = "CREATED";
    public static final String RUNNING = "RUNNING";
    public static final String WAITING = "WAITING";
    public static final String COMPLETED = "COMPLETED";
    public static final String FAILED = "FAILED";
    public static final String STOPPED = "STOPPED";

    private GatewaySessionStatus() {
    }
}
