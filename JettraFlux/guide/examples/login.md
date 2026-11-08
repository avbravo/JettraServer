# Login Example

The `Login` widget is a pre-built authentication form container that simplifies rendering login screens.

## Usage

```java
import io.jettra.espresso.widgets.Login;

Login loginForm = Login.create()
    .action("/api/auth/login")
    .title("Sign In to Your Account");
```
