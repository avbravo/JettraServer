# Padding Example

The `Padding` widget is a utility container that solely exists to add spacing around its child widget.

## Usage

```java
import io.jettra.espresso.widgets.Padding;
import io.jettra.espresso.widgets.Text;

Padding paddedText = Padding.of(
    Text.of("This text has padding around it")
);
// By default, padding may use Theme-defined spacing, or you can supply custom styles via Modifier.
```
