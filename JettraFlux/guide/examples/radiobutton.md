# RadioButton Example

The `RadioButton` widget provides a selection input where only one option can be selected in a group.

## Usage

```java
import io.jettra.espresso.widgets.RadioButton;

RadioButton option1 = RadioButton.create()
    .name("gender")
    .value("M")
    .label("Male")
    .checked(true);

RadioButton option2 = RadioButton.create()
    .name("gender")
    .value("F")
    .label("Female");
```
