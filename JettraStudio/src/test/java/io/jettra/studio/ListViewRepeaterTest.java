package io.jettra.studio;

import io.jettra.studio.components.Label;
import io.jettra.studio.components.ListItem;
import io.jettra.studio.components.ListView;
import io.jettra.studio.core.WebPage;
import io.jettra.test.annotation.NotRequiresRunningServer;
import io.jettra.test.annotation.Test;

import java.util.List;

import static io.jettra.test.core.JettraAssert.*;

@NotRequiresRunningServer
public class ListViewRepeaterTest {

    public record ServerNode(String name, String ip, String status) {}

    public static class ClusterMonitorPage extends WebPage {
        public ClusterMonitorPage(List<ServerNode> nodes) {
            add(new ListView<ServerNode>("nodeList", nodes) {
                @Override
                protected void populateItem(ListItem<ServerNode> item) {
                    ServerNode node = item.getModelObject();
                    item.add(new Label("nodeName", node.name()));
                    item.add(new Label("nodeIp", node.ip()));
                    item.add(new Label("nodeStatus", node.status()));
                }
            });
        }
    }

    @Test
    public void testListViewClonesMarkupForEveryElement() {
        List<ServerNode> nodes = List.of(
            new ServerNode("node-01-master", "127.0.0.1:8765", "ONLINE"),
            new ServerNode("node-02-secondary", "127.0.0.1:8766", "ONLINE"),
            new ServerNode("node-03-replica", "127.0.0.1:8767", "OFFLINE")
        );

        ClusterMonitorPage page = new ClusterMonitorPage(nodes);
        page.setCustomMarkup("<table class=\"cluster-table\">\n"
            + "  <thead><tr><th>Nombre</th><th>IP</th><th>Estado</th></tr></thead>\n"
            + "  <tbody>\n"
            + "    <tr jettrat:id=\"nodeList\">\n"
            + "      <td jettrat:id=\"nodeName\">node-sample</td>\n"
            + "      <td jettrat:id=\"nodeIp\">0.0.0.0</td>\n"
            + "      <td jettrat:id=\"nodeStatus\">UNKNOWN</td>\n"
            + "    </tr>\n"
            + "  </tbody>\n"
            + "</table>");

        String html = page.renderPage();
        assertNotNull(html);

        // Verify all 3 rows were rendered with data
        assertTrue(html.contains("node-01-master"));
        assertTrue(html.contains("127.0.0.1:8765"));

        assertTrue(html.contains("node-02-secondary"));
        assertTrue(html.contains("127.0.0.1:8766"));

        assertTrue(html.contains("node-03-replica"));
        assertTrue(html.contains("127.0.0.1:8767"));
        assertTrue(html.contains("OFFLINE"));

        // Placeholder must not be present
        assertFalse(html.contains("node-sample"));
        assertFalse(html.contains("0.0.0.0"));
    }
}
