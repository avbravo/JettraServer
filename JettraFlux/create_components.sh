#!/bin/bash
PACKAGE="io.jettra.espresso.widgets"
SRC_DIR="src/main/java/io/jettra/espresso/widgets"
DOC_DIR="guide/examples"

COMPONENTS=(
Avatar AvatarGroup Board Carrousel Footer CrudView Dashboard Left Page Panel TabView Top TrafficLigth Tree Chart Alert BarCode Button Calendar Catcha CharsBar CharsDoughnut ChartsLine CharsPie CharsRadar CheckBoxGroup Clock Console CreditCard DatePicker Divide Downloader Draw FileUpload FolderSelector FormGroup IndexUI Inputs Loading LoginAdvanced LoginUI Map Menu MenuBar MenuItem Notification Organigram OTPValidator PDFViewer QR QRReader RadioGroupButton Schedule Script SelectMany SelectOne SelectOneIcon Separator SessionTimeOutDialog Spinner Style TD TextBox Time TimeLine ToggleSwitch ViewMedia TimePicker
)

mkdir -p $SRC_DIR
mkdir -p $DOC_DIR

for COMPONENT in "${COMPONENTS[@]}"; do
  if [ ! -f "$SRC_DIR/$COMPONENT.java" ]; then
    cat <<INNER_EOF > "$SRC_DIR/$COMPONENT.java"
package $PACKAGE;

import io.jettra.espresso.core.Widget;
import io.jettra.espresso.theme.ThemeData;
import java.util.Arrays;
import java.util.List;

public class $COMPONENT extends Widget {
    private final List<Widget> children;

    private $COMPONENT(List<Widget> children) {
        this.children = children;
    }

    public static $COMPONENT of(Widget... children) {
        return new $COMPONENT(Arrays.asList(children));
    }

    @Override
    public String render(ThemeData theme) {
        StringBuilder sb = new StringBuilder();
        sb.append("<div ").append(renderCommonAttributes(theme, "espresso-$(echo $COMPONENT | tr '[:upper:]' '[:lower:]')")).append(">\n");
        for (Widget child : children) {
            sb.append(child.render(theme));
        }
        sb.append("</div>\n");
        return sb.toString();
    }
}
INNER_EOF
  fi

  LOWER_COMP=$(echo "$COMPONENT" | tr '[:upper:]' '[:lower:]')
  if [ ! -f "$DOC_DIR/$LOWER_COMP.md" ]; then
    cat <<INNER_EOF > "$DOC_DIR/$LOWER_COMP.md"
# $COMPONENT Example

The \`$COMPONENT\` widget is part of the JettraEspressoUI framework.

## Usage

\`\`\`java
import io.jettra.espresso.widgets.$COMPONENT;
import io.jettra.espresso.widgets.Text;

$COMPONENT my$COMPONENT = $COMPONENT.of(
    Text.of("This is a $COMPONENT widget.")
);
\`\`\`
INNER_EOF
  fi
done
echo "Components generated successfully."
