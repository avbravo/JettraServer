package io.jettra.studio.core;

import io.jettra.studio.model.IModel;

/**
 * Base class for leaf components that cannot contain children (e.g. Label, Image, CheckBox).
 */
public abstract class WebComponent extends Component {

    public WebComponent(String id) {
        super(id);
    }

    public WebComponent(String id, IModel<?> model) {
        super(id, model);
    }
}
