# Scroll Example

The `Scroll` widget enables scrolling for its children when the content exceeds the widget's bounds.

## Usage

```java
import io.jettra.espresso.widgets.Scroll;
import io.jettra.espresso.widgets.Text;

Scroll scrollableArea = Scroll.of(
    Text.of("Very long text..."),
    Text.of("More content...")
).direction("y"); // Enables vertical scrolling
```
