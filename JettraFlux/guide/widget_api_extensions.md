# JettraFlux API Extensions

This document outlines the new widget convenience constructors and API methods added to support JettraFluxExample.

## `of(String)` Constructors
To simplify widget instantiation without manually creating `Text` or `Image` wrappers, the following widgets now support `of(String)` initializers:

- `Avatar.of(String src)`: Wraps a `Text` or `Image` depending on if the `src` string resembles a URL/URI (e.g. `http://`, `https://`, `data:`).
- `Button.of(String label)`: Creates a button containing a `Text` widget with the provided label.
- `TextBox.of(String text)`: Creates a text box containing the given text.
- `ChartsLine.of(String data)`, `CharsBar.of(String data)`, `CharsPie.of(String data)`, `CharsDoughnut.of(String data)`: Creates the respective chart wrapping the provided text/data string.

## Other Extensions
- `Column`: Modified `of(Widget... children)` to store a mutable `ArrayList` internally, enabling the new `public Column add(Widget child)` method which allows dynamically appending children after instantiation.
- `ProgressBar`: Added a parameterless `of()` static constructor.
- `RadioButton`: Added `of(String id, String label)` for quick creation with a name and label.
- `Icon.Heroicons`: Added numerous missing SVG properties (`STAR`, `LOCATION_MARKER`, `BRIEFCASE`, `CALENDAR`, `CHECK_CIRCLE`, `CHEVRON_DOWN`, `CLOUD_UPLOAD`, `DOCUMENT`, `DOTS_VERTICAL`, `FILM`, `FOLDER`, `MINUS`, `PHOTOGRAPH`).
