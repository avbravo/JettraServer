package io.jettra.studio.core;

import io.jettra.studio.markup.MarkupTag;
import io.jettra.studio.model.IModel;

import java.util.Map;
import java.util.function.Consumer;

/**
 * Form component (<form>) in JettraStudio.
 * Coordinates nested form fields, binds submitted request values, and executes onSubmit handlers.
 *
 * @param <T> Form bean / model type
 */
public class Form<T> extends MarkupContainer {

    private String action = "";
    private String method = "POST";
    private Consumer<Form<T>> submitAction;

    public Form(String id) {
        super(id);
    }

    public Form(String id, IModel<T> model) {
        super(id, model);
    }

    public static <T> Form<T> of(String id) {
        return new Form<>(id);
    }

    public static <T> Form<T> of(String id, IModel<T> model) {
        return new Form<>(id, model);
    }

    public Form<T> action(String action) {
        this.action = action;
        return this;
    }

    public Form<T> method(String method) {
        this.method = method;
        return this;
    }

    public Form<T> onSubmit(Consumer<Form<T>> submitAction) {
        this.submitAction = submitAction;
        return this;
    }

    /**
     * Submit action hook. Override or configure via onSubmit(...).
     */
    public void onSubmit() {
        if (submitAction != null) {
            submitAction.accept(this);
        }
    }

    /**
     * Processes form submission data by binding parameters to child input components
     * and triggering the submit action.
     */
    public void processSubmit(Map<String, String> formData) {
        if (formData == null) return;

        // 1. Traverse child components and update models
        io.jettra.studio.components.Button clickedButton = null;
        io.jettra.studio.components.Button defaultButton = null;

        for (Component child : this) {
            String childId = child.getId();
            if (formData.containsKey(childId)) {
                String submittedVal = formData.get(childId);
                child.setModelObject(submittedVal);
                if (child instanceof io.jettra.studio.components.Button btn) {
                    clickedButton = btn;
                }
            }
            if (child instanceof io.jettra.studio.components.Button btn && defaultButton == null) {
                defaultButton = btn;
            }
        }

        // 2. Trigger button onClick action hook
        if (clickedButton != null) {
            clickedButton.onClick();
        } else if (defaultButton != null) {
            defaultButton.onClick();
        }

        // 3. Trigger form-level onSubmit hook
        onSubmit();
    }

    @Override
    public void onComponentTag(MarkupTag tag) {
        super.onComponentTag(tag);
        if (tag.getAttribute("id") == null) {
            tag.setAttribute("id", getId());
        }
        if (tag.getAttribute("name") == null) {
            tag.setAttribute("name", getId());
        }
        tag.setAttribute("method", method != null ? method : "POST");
        if (action != null && !action.isBlank()) {
            tag.setAttribute("action", action);
        } else {
            tag.setAttribute("action", ""); // Submit to current URL
        }
        addCssClass("espresso-form");
    }
}
