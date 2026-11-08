# Header Example

The `Header` widget is used to render `<h1` to `<h6>` tags. 
You must specify the level (1-6).

## Usage

```java
import io.jettra.espresso.widgets.Header;

// Renders an <h1> tag
Header title = Header.of(1, "Main Dashboard Title");

// Renders an <h2> tag with custom classes
Header subtitle = Header.of(2, "Section Overview").modifier(
    new Modifier().cssClass("text-muted")
);
```
