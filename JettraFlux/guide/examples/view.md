# View Example

The `View` widget acts as a structural full-screen container (`width: 100%; height: 100%`) to wrap entire page layouts.

## Usage

```java
import io.jettra.espresso.widgets.View;
import io.jettra.espresso.widgets.Text;

View fullScreenView = View.of(
    Text.of("This view fills the parent size.")
);
```
