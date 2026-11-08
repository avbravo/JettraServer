# Carousel Example

The `Carousel` widget displays items in a horizontal scrollable row with snap points.

## Usage

```java
import io.jettra.espresso.widgets.Carousel;
import io.jettra.espresso.widgets.Image;

Carousel myCarousel = Carousel.of(
    Image.of("/assets/slide1.png"),
    Image.of("/assets/slide2.png"),
    Image.of("/assets/slide3.png")
);
```
