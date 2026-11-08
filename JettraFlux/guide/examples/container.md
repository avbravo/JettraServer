# Container Example

The `Container` widget wraps a single child and applies padding, margin, borders, and background color seamlessly via the `Modifier`.

## Usage

```java
import io.jettra.espresso.widgets.Container;
import io.jettra.espresso.widgets.Text;
import io.jettra.espresso.core.Modifier;

Container box = Container.of(Text.of("Wrapped Content"))
    .modifier(new Modifier().padding(16).background("#f0f0f0"));
```
