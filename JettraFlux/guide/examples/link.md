# Link Example

The `Link` widget renders an `<a>` tag for navigation.

## Usage

```java
import io.jettra.espresso.widgets.Link;

Link homeLink = Link.of("/home", "Go to Homepage");

Link newTabLink = Link.of("https://google.com", "Search Google").target("_blank");
```
