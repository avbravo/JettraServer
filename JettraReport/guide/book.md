# JettraReport - Comprehensive Guide & Architecture Manual

## 1. Overview & Architecture
`JettraReport` is a high-performance, template-driven document generation and reporting engine written natively in **Java 25**. It compiles datasets, records, and multi-model query results into PDF, Excel (XLSX), CSV, and HTML reports with sub-second rendering times.

---

## 2. Key Features
- **Native Java 25 Record Source Binding**: Directly accepts lists of `java.lang.Record` or `JsonObject` items as tabular data sources.
- **Multi-Format Export**: Generates PDF, XLSX, CSV, and formatted HTML with modern styling and responsive tables.
- **Streaming Document Compiler**: Uses low memory buffers suitable for generating multi-gigabyte exports without `OutOfMemoryError`.
- **Chart & Aggregation Integration**: Built-in summaries, totals, averages, and group counts.

---

## 3. Installation
```xml
<dependency>
   <groupId>com.github.jettraframework</groupId>
    <artifactId>JettraReport</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

---

## 4. Usage & Code Examples

### 4.1 Generating a Report from Java Records
```java
import io.jettra.report.ReportGenerator;
import io.jettra.report.ReportFormat;
import java.util.List;

public record InvoiceLine(String sku, String description, int quantity, double unitPrice, double subtotal) {}

public class ReportDemo {
    public static void main(String[] args) throws Exception {
        List<InvoiceLine> lines = List.of(
            new InvoiceLine("SKU-1", "Dell UltraSharp Monitor", 2, 350.0, 700.0),
            new InvoiceLine("SKU-2", "Logitech MX Master Mouse", 1, 99.0, 99.0)
        );

        ReportGenerator report = ReportGenerator.builder()
            .title("Monthly Sales Report")
            .dataSource(lines)
            .columns("sku", "description", "quantity", "unitPrice", "subtotal")
            .format(ReportFormat.PDF)
            .build();

        byte[] pdfBytes = report.render();
        System.out.println("Rendered PDF report size: " + pdfBytes.length + " bytes.");
    }
}
```
