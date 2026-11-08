package io.jettra.studio.components;

import io.jettra.studio.core.Component;
import io.jettra.studio.core.MarkupContainer;
import io.jettra.studio.markup.MarkupTag;
import io.jettra.studio.model.IModel;
import io.jettra.studio.model.Model;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * Repeater component repeater component for collections in JettraStudio.
 * Clones the template markup tag and body for each element in the collection,
 * invoking populateItem(ListItem&lt;T&gt; item) for child component binding.
 *
 * Example:
 * Java:
 *   add(new ListView&lt;User&gt;("userList", users) {
 *       protected void populateItem(ListItem&lt;User&gt; item) {
 *           User u = item.getModelObject();
 *           item.add(new Label("name", u.getName()));
 *           item.add(new Label("role", u.getRole()));
 *       }
 *   });
 *
 * HTML:
 *   &lt;tr jettrat:id="userList"&gt;
 *       &lt;td jettrat:id="name"&gt;Placeholder Name&lt;/td&gt;
 *       &lt;td jettrat:id="role"&gt;Placeholder Role&lt;/td&gt;
 *   &lt;/tr&gt;
 *
 * @param <T> Element type
 */
public class ListView<T> extends Component {

    private final IModel<? extends List<T>> listModel;
    private BiConsumer<ListItem<T>, T> itemConsumer;

    public ListView(String id, List<T> list) {
        this(id, new Model<>(new ArrayList<>(list != null ? list : List.of())));
    }

    public ListView(String id, IModel<? extends List<T>> listModel) {
        super(id, listModel);
        this.listModel = listModel;
    }

    public ListView(String id, List<T> list, BiConsumer<ListItem<T>, T> itemConsumer) {
        this(id, list);
        this.itemConsumer = itemConsumer;
    }

    public ListView(String id, IModel<? extends List<T>> listModel, BiConsumer<ListItem<T>, T> itemConsumer) {
        this(id, listModel);
        this.itemConsumer = itemConsumer;
    }

    public static <T> ListView<T> of(String id, List<T> list, BiConsumer<ListItem<T>, T> itemConsumer) {
        return new ListView<>(id, list, itemConsumer);
    }

    /**
     * Hook to bind components to the individual list item.
     * Override or pass a lambda in the constructor.
     */
    protected void populateItem(ListItem<T> item) {
        if (itemConsumer != null) {
            itemConsumer.accept(item, item.getModelObject());
        }
    }

    @Override
    public void render(MarkupTag tag, StringBuilder buffer) {
        if (!isVisible()) return;

        List<T> list = (listModel != null) ? listModel.getObject() : null;
        if (list == null || list.isEmpty()) {
            return; // Empty list renders no rows
        }

        String templateBody = tag.getBodyContent();

        for (int i = 0; i < list.size(); i++) {
            T element = list.get(i);
            ListItem<T> item = new ListItem<>(i, new Model<>(element));
            item.setParent(getParent());

            // User populates item with child components
            populateItem(item);

            // Render opening tag for this repeated item
            MarkupTag itemTag = new MarkupTag(tag.getTagName(), tag.getId());
            itemTag.getAttributes().putAll(tag.getAttributes());
            itemTag.setBodyContent(templateBody);
            itemTag.setSelfClosing(tag.isSelfClosing());

            item.onComponentTag(itemTag);
            buffer.append(itemTag.renderOpeningTag());

            if (!itemTag.isSelfClosing()) {
                // Render children inside the item's body
                item.renderMarkupContent(templateBody, buffer);
                buffer.append(itemTag.renderClosingTag());
            }

            item.detach();
        }
    }
}
