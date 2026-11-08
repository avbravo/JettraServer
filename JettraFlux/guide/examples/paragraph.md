# Paragraph Example

The `Paragraph` widget generates a `<p>` HTML tag.

## Usage

```java
import io.jettra.espresso.widgets.Paragraph;
import io.jettra.espresso.widgets.Span;

Paragraph p1 = Paragraph.of("This is a simple paragraph of text.");

Paragraph p2 = Paragraph.of(
    Span.of("This is the first sentence. "),
    Span.of("This is the second sentence.")
);
```
