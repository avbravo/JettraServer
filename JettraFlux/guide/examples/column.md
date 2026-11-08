# Column Example

The `Column` widget arranges its children vertically in a flexbox column.

## Usage

```java
import io.jettra.espresso.widgets.Column;
import io.jettra.espresso.widgets.Text;

Column verticalLayout = Column.of(
    Text.of("Top"),
    Text.of("Middle"),
    Text.of("Bottom")
);
```
