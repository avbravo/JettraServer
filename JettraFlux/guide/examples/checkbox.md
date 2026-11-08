# Checkbox Example

The `Checkbox` widget allows the user to select one or multiple options from a set.

## Usage

```java
import io.jettra.espresso.widgets.Checkbox;

Checkbox agreeBox = Checkbox.of("agree", "yes")
    .checked(true)
    .id("chk_agree");
```
