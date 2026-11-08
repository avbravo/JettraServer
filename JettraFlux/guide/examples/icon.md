# Icon Example

The `Icon` widget renders an `<i>` tag, typically used with icon fonts like FontAwesome.

## Usage

```java
import io.jettra.espresso.widgets.Icon;
import io.jettra.espresso.core.Modifier;

Icon userIcon = Icon.of("fa fa-user");

Icon redHeart = Icon.of("fa fa-heart").modifier(
    new Modifier().style("color: red;")
);
```
