# OutlinedButton Example

The `OutlinedButton` widget is a Material Design style button with a transparent background and a visible border.

## Usage

```java
import io.jettra.espresso.widgets.OutlinedButton;

OutlinedButton cancelBtn = OutlinedButton.of("Cancel")
    .onClick(e -> System.out.println("Cancelled"));
```
