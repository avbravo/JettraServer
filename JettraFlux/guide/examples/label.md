# Label Example

The `Label` widget is used to attach a description to a form input field using the `for` attribute.

## Usage

```java
import io.jettra.espresso.widgets.Label;
import io.jettra.espresso.widgets.TextField;

TextField usernameField = TextField.create().id("user_input");

Label usernameLabel = Label.of("Username:").forId("user_input");
```
