package io.jettra.flux.widgets;

import java.util.Collection;

/**
 * Standard framework alias for JettraUserEditModal.
 */
public class UserEditModal extends JettraUserEditModal {

    protected UserEditModal(String modalId) {
        super(modalId);
    }

    public static JettraUserEditModal of(String modalId) {
        return JettraUserEditModal.of(modalId);
    }

    public static JettraUserEditModal builder(String modalId) {
        return JettraUserEditModal.builder(modalId);
    }
}
