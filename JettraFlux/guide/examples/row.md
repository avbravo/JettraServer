# Row Example

The `Row` widget arranges its children horizontally using flexbox layout.

## Usage

```java
import io.jettra.espresso.widgets.Row;
import io.jettra.espresso.widgets.ElevatedButton;

Row buttonRow = Row.of(
    ElevatedButton.of("Save"),
    ElevatedButton.of("Cancel")
);
```
