# Modal Example

The `Modal` widget is an absolute positioned overlay that covers the entire screen, often used to force user interaction or show critical information.

## Usage

```java
import io.jettra.espresso.widgets.Modal;
import io.jettra.espresso.widgets.Text;

Modal warningModal = Modal.of(
    Text.of("Are you sure you want to delete this?")
).open(true);
```
