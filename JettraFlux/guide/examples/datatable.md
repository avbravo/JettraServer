# Datatable Example

The `Datatable` widget renders a structured HTML table given a list of headers and rows of data.

## Usage

```java
import io.jettra.espresso.widgets.Datatable;
import java.util.Arrays;

Datatable table = Datatable.of(
    Arrays.asList("ID", "Name", "Role"), 
    Arrays.asList(
        Arrays.asList("1", "Alice", "Admin"), 
        Arrays.asList("2", "Bob", "User")
    )
);
```
