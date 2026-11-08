# Div Example

The `Div` widget is the primary container for other widgets. It generates a standard `<div>` HTML tag.

## Usage

```java
import io.jettra.espresso.widgets.Div;
import io.jettra.espresso.widgets.Text;
import io.jettra.espresso.core.Modifier;

Div myDiv = Div.of(
    Text.of("Hello inside a div"),
    Text.of("Another text element")
).modifier(new Modifier().cssClass("my-custom-class").style("padding: 10px;"));
```
