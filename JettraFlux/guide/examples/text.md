# Text Example

The `Text` widget is the most basic building block for rendering string literals.

## Usage

```java
import io.jettra.espresso.widgets.Text;
import io.jettra.espresso.core.Modifier;

Text greeting = Text.of("Hello World!").modifier(
    new Modifier().style("font-size: 24px;")
);
```
