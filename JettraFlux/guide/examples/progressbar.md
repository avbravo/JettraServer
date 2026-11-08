# ProgressBar Example

The `ProgressBar` widget displays an HTML5 `<progress>` bar to indicate completion status.

## Usage

```java
import io.jettra.espresso.widgets.ProgressBar;

ProgressBar loadingBar = ProgressBar.create()
    .value(45)
    .max(100);
```
