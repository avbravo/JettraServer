# Dialog Example

The `Dialog` widget generates an HTML5 `<dialog>` element which can be natively opened or closed by the browser.

## Usage

```java
import io.jettra.espresso.widgets.Dialog;
import io.jettra.espresso.widgets.Text;

Dialog myDialog = Dialog.of(
    Text.of("This is a native dialog box.")
).open(true);
```
