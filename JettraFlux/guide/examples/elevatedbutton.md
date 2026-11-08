# ElevatedButton Example

The `ElevatedButton` widget is a Material Design style button with a solid background and elevation shadow.

## Usage

```java
import io.jettra.espresso.widgets.ElevatedButton;

ElevatedButton submitBtn = ElevatedButton.of("Submit")
    .onClick(e -> System.out.println("Clicked!"));
```
