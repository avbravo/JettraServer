# Scaffold Example

The `Scaffold` widget provides a high-level layout structure for a page, typically including a top app bar, body, and optionally a bottom navigation bar.

## Usage

```java
import io.jettra.espresso.widgets.Scaffold;
import io.jettra.espresso.widgets.Text;
import io.jettra.espresso.widgets.Container;

Scaffold myApp = Scaffold.of()
    .topBar(Container.of(Text.of("App Title")))
    .body(Text.of("Main content goes here"));
```
