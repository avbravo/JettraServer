# DataView Component

The `DataView` component in JettraFlux displays data in either a list or a grid format, closely resembling the DataView component in PrimeNG Atlantis.

## Usage

Import the necessary classes:
```java
import io.jettra.flux.widgets.DataView;
import io.jettra.flux.theme.DataViewCSS;
```

### Basic List Layout

```java
List<Widget> products = Arrays.asList(
    Column.of(
        Header.of(4, "Product 1"),
        Paragraph.of("Description of product 1"),
        Text.of("$10.00")
    ),
    Column.of(
        Header.of(4, "Product 2"),
        Paragraph.of("Description of product 2"),
        Text.of("$25.00")
    )
);

Widget dataView = DataView.of(products)
    .layout(DataView.Layout.LIST)
    .header(Header.of(3, "Products List"));
```

### Including CSS
Don't forget to include the CSS required for proper layout styling somewhere in your view:
```java
return Column.of(
    Paragraph.of(DataViewCSS.get()),
    dataView
);
```

### Grid Layout
You can switch the layout to Grid format to display items as cards.

```java
Widget dataViewGrid = DataView.of(products)
    .layout(DataView.Layout.GRID)
    .header(Header.of(3, "Products Grid"));
```

## Features
- **Header Slot:** Set a custom widget as the header.
- **Two Layouts:** Seamlessly switch between `DataView.Layout.LIST` and `DataView.Layout.GRID`.
- **Responsive:** The grid auto-adapts columns, and the list switches to a vertical stack on mobile screens.
