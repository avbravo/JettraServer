package io.jettra.flux.widgets;
import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;
public class ImageCompare extends Widget {
    private final String img1, img2;
    private ImageCompare(String img1, String img2) { this.img1 = img1; this.img2 = img2; }
    public static ImageCompare of(String img1, String img2) { return new ImageCompare(img1, img2); }
    @Override public String render(ThemeData theme) {
        String id = "imgcomp_" + java.util.UUID.randomUUID().toString().replace("-", "");
        StringBuilder sb = new StringBuilder();
        sb.append("<div ").append(renderCommonAttributes(theme, "image-compare"))
          .append(" style=\"position: relative; width: 100%; max-width: 600px; height: 300px; overflow: hidden; border-radius: 8px;\">\n");
        
        sb.append("<img src=\"").append(img1).append("\" style=\"position: absolute; top: 0; left: 0; width: 100%; height: 100%; object-fit: cover;\" />\n");
        sb.append("<img id=\"").append(id).append("_img2\" src=\"").append(img2).append("\" style=\"position: absolute; top: 0; left: 0; width: 100%; height: 100%; object-fit: cover; clip-path: polygon(0 0, 50% 0, 50% 100%, 0 100%);\" />\n");
        
        sb.append("<input type=\"range\" min=\"0\" max=\"100\" value=\"50\" ")
          .append("oninput=\"document.getElementById('").append(id).append("_img2').style.clipPath = 'polygon(0 0, ' + this.value + '% 0, ' + this.value + '% 100%, 0 100%)'; ")
          .append("document.getElementById('").append(id).append("_divider').style.left = this.value + '%';\" ")
          .append("style=\"position: absolute; top: 0; left: 0; width: 100%; height: 100%; opacity: 0; cursor: ew-resize; z-index: 10;\" />\n");
          
        sb.append("<div style=\"position: absolute; top: 0; bottom: 0; left: 50%; width: 2px; background: white; pointer-events: none; z-index: 5; transform: translateX(-50%); box-shadow: 0 0 5px rgba(0,0,0,0.5);\" id=\"").append(id).append("_divider\"></div>\n");
        
        sb.append("</div>\n");
        return sb.toString();
    }
}
