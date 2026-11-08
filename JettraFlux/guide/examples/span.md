# Span Example

The `Span` widget is used for inline containers. It generates a `<span>` HTML tag.

## Usage

```java
import io.jettra.espresso.widgets.Span;
import io.jettra.espresso.widgets.Text;
import io.jettra.espresso.core.Modifier;

Span highlightedText = Span.of("Important!").modifier(
    new Modifier().style("color: red; font-weight: bold;")
);

Span complexSpan = Span.of(
    Text.of("Start "),
    Span.of("middle"),
    Text.of(" end")
);
```
