package com.agentapi.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "agent.deployment")
public class AgentDeploymentProperties {

    /**
     * {@code single} = uma organizacao por instalacao (padrao). {@code multi} reservado para evolucao SaaS.
     */
    private TenantMode tenantMode = TenantMode.SINGLE;

    public TenantMode getTenantMode() {
        return tenantMode;
    }

    public void setTenantMode(TenantMode tenantMode) {
        this.tenantMode = tenantMode;
    }

    public boolean isSingleTenant() {
        return tenantMode == TenantMode.SINGLE;
    }

    public enum TenantMode {
        SINGLE,
        MULTI
    }
}
