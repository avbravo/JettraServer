# TextArea Example

The `TextArea` widget renders a `<textarea>` tag for multi-line text input.

## Usage

```java
import io.jettra.espresso.widgets.TextArea;

TextArea commentBox = TextArea.create()
    .name("comments")
    .rows(5)
    .cols(80)
    .placeholder("Enter your comments here...")
    .value("Initial text");
```
