package org.example.eventplatform.shared.context;

public final class TenantContext {

    private static final ThreadLocal<Long> currentTenantId = new ThreadLocal<>();

    private TenantContext() {
    }

    public static void setCurrentTenantId(Long tenantId) {
        currentTenantId.set(tenantId);
    }

    /**
     * Returns null for a platform-wide (super-admin) request.
     */
    public static Long getCurrentTenantId() {
        return currentTenantId.get();
    }

    public static boolean isTenantScoped() {
        return currentTenantId.get() != null;
    }

    public static void clear() {
        currentTenantId.remove();
    }
}
