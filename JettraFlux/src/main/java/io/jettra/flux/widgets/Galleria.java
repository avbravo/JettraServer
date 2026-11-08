package io.jettra.flux.widgets;
import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;
import java.util.Arrays;
import java.util.List;
public class Galleria extends Widget {
    private final List<String> images;
    private Galleria(List<String> images) { this.images = images; }
    public static Galleria of(String... images) { return new Galleria(Arrays.asList(images)); }
    @Override public String render(ThemeData theme) {
        String id = "galleria_" + java.util.UUID.randomUUID().toString().replace("-", "");
        StringBuilder sb = new StringBuilder();
        sb.append("<div ").append(renderCommonAttributes(theme, "galleria-container")).append(" style=\"display:flex; flex-direction:column; gap:10px; width: 100%; max-width: 600px;\">\n");
        
        if (!images.isEmpty()) {
            StringBuilder imgsArray = new StringBuilder("[");
            for(int i=0; i<images.size(); i++){
                imgsArray.append("'").append(images.get(i)).append("'");
                if(i < images.size()-1) imgsArray.append(",");
            }
            imgsArray.append("]");
            
            sb.append("<div style=\"width: 100%; height: 400px; position: relative; overflow: hidden; border-radius: 8px; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.1);\">\n");
            sb.append("  <img id=\"").append(id).append("_main\" src=\"").append(images.get(0)).append("\" data-idx=\"0\" style=\"width: 100%; height: 100%; object-fit: cover; transition: opacity 0.3s;\" />\n");
            
            sb.append("  <button onclick=\"var imgs=").append(imgsArray).append("; var el=document.getElementById('").append(id).append("_main'); var idx=parseInt(el.getAttribute('data-idx')); idx = (idx > 0) ? idx - 1 : imgs.length - 1; el.src=imgs[idx]; el.setAttribute('data-idx', idx);\" style=\"position: absolute; left: 10px; top: 50%; transform: translateY(-50%); background: rgba(0,0,0,0.5); color: white; border: none; border-radius: 50%; width: 40px; height: 40px; cursor: pointer;\"><i class=\"fas fa-chevron-left\"></i></button>\n");
            sb.append("  <button onclick=\"var imgs=").append(imgsArray).append("; var el=document.getElementById('").append(id).append("_main'); var idx=parseInt(el.getAttribute('data-idx')); idx = (idx < imgs.length - 1) ? idx + 1 : 0; el.src=imgs[idx]; el.setAttribute('data-idx', idx);\" style=\"position: absolute; right: 10px; top: 50%; transform: translateY(-50%); background: rgba(0,0,0,0.5); color: white; border: none; border-radius: 50%; width: 40px; height: 40px; cursor: pointer;\"><i class=\"fas fa-chevron-right\"></i></button>\n");
            sb.append("</div>\n");
            
            sb.append("<div style=\"display:flex; gap:10px; overflow-x: auto; padding-bottom: 5px;\">\n");
            int idx = 0;
            for (String img : images) {
                sb.append("  <img src=\"").append(img).append("\" onclick=\"var main=document.getElementById('").append(id).append("_main'); main.src=this.src; main.setAttribute('data-idx', '").append(idx++).append("');\" style=\"width:80px; height:60px; object-fit:cover; border-radius:4px; cursor:pointer; border: 2px solid transparent;\" onmouseover=\"this.style.opacity='0.8'\" onmouseout=\"this.style.opacity='1'\" />\n");
            }
            sb.append("</div>\n");
        }
        
        sb.append("</div>\n");
        return sb.toString();
    }
}
