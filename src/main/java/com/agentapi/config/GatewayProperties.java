package com.agentapi.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "agent.gateway")
public class GatewayProperties {

    private String iaPath = "/home/ia";

    private String auditPath;

    private int maxSessions = 10;

    private long sessionTimeoutMinutes = 120;

    private int maxMessageChars = 32_000;

    private String sidecarUrl = "http://127.0.0.1:8791";

    private String sidecarToken = "";

    private boolean sidecarAutoStart = false;

    private List<String> projectRootAllowlist = new ArrayList<>();

    /**
     * Pasta única no servidor onde o Cursor atua (cwd do sidecar). Preferencial ao catálogo {@code projects}.
     */
    private String defaultProjectPath = "";

    private List<ProjectEntry> projects = new ArrayList<>();

    public String getIaPath() {
        return iaPath;
    }

    public void setIaPath(String iaPath) {
        this.iaPath = iaPath;
    }

    public String getAuditPath() {
        return auditPath != null && !auditPath.isBlank() ? auditPath : iaPath + "/audit";
    }

    public void setAuditPath(String auditPath) {
        this.auditPath = auditPath;
    }

    public int getMaxSessions() {
        return maxSessions;
    }

    public void setMaxSessions(int maxSessions) {
        this.maxSessions = maxSessions;
    }

    public long getSessionTimeoutMinutes() {
        return sessionTimeoutMinutes;
    }

    public void setSessionTimeoutMinutes(long sessionTimeoutMinutes) {
        this.sessionTimeoutMinutes = sessionTimeoutMinutes;
    }

    public int getMaxMessageChars() {
        return maxMessageChars;
    }

    public void setMaxMessageChars(int maxMessageChars) {
        this.maxMessageChars = maxMessageChars;
    }

    public String getSidecarUrl() {
        return sidecarUrl;
    }

    public void setSidecarUrl(String sidecarUrl) {
        this.sidecarUrl = sidecarUrl;
    }

    public String getSidecarToken() {
        return sidecarToken;
    }

    public void setSidecarToken(String sidecarToken) {
        this.sidecarToken = sidecarToken;
    }

    public boolean isSidecarAutoStart() {
        return sidecarAutoStart;
    }

    public void setSidecarAutoStart(boolean sidecarAutoStart) {
        this.sidecarAutoStart = sidecarAutoStart;
    }

    public List<String> getProjectRootAllowlist() {
        return projectRootAllowlist;
    }

    public void setProjectRootAllowlist(List<String> projectRootAllowlist) {
        this.projectRootAllowlist = projectRootAllowlist;
    }

    public String getDefaultProjectPath() {
        return defaultProjectPath;
    }

    public void setDefaultProjectPath(String defaultProjectPath) {
        this.defaultProjectPath = defaultProjectPath;
    }

    public List<ProjectEntry> getProjects() {
        return projects;
    }

    public void setProjects(List<ProjectEntry> projects) {
        this.projects = projects;
    }

    public static class ProjectEntry {
        private String id;
        private String name;
        private String path;
        private boolean enabled = true;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }
}
