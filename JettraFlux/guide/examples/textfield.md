# TextField Example

The `TextField` widget renders a standard `<input type="text">` element for capturing user input.

## Usage

```java
import io.jettra.espresso.widgets.TextField;

TextField usernameInput = TextField.create()
    .id("username")
    .name("username")
    .placeholder("Enter username");
```
