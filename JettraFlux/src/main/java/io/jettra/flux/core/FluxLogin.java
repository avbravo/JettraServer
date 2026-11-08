package io.jettra.flux.core;

import io.jettra.flux.model.CredentialFlux;
import io.jettra.scoped.SessionScoped;

@SessionScoped
public class FluxLogin {
    
    private CredentialFlux credential;
    private long loginTime;
    private long expirationTimeMillis;

    public FluxLogin() {
    }

    public void login(CredentialFlux credential, long expirationTimeMillis) {
        this.credential = credential;
        this.loginTime = System.currentTimeMillis();
        this.expirationTimeMillis = expirationTimeMillis;
    }

    public void logout() {
        this.credential = null;
        this.loginTime = 0;
        this.expirationTimeMillis = 0;
    }

    public boolean isLoggedIn() {
        if (credential == null) {
            return false;
        }
        if (System.currentTimeMillis() - loginTime > expirationTimeMillis) {
            // Session expired
            logout();
            return false;
        }
        return true;
    }
    
    public void extendSession() {
        if (isLoggedIn()) {
            this.loginTime = System.currentTimeMillis();
        }
    }

    public CredentialFlux getCredential() {
        return credential;
    }
}
