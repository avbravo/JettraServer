# Form Example

The `Form` widget is used to group inputs and submit data.

## Usage

```java
import io.jettra.espresso.widgets.Form;
import io.jettra.espresso.widgets.TextField;
import io.jettra.espresso.widgets.ElevatedButton;

Form myForm = Form.of(
    TextField.create().placeholder("Username"),
    TextField.create().placeholder("Password"),
    ElevatedButton.of("Submit")
).action("/login").method("POST");
```
