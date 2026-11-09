package org.example.export;

import org.example.model.Summary;
import org.example.sample.SampleData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import java.util.concurrent.atomic.AtomicBoolean;
import static org.junit.jupiter.api.Assertions.*;

class ExporterFactoryTest {
    @TempDir Path directory;
    private final ExporterFactory factory = new DefaultExporterFactory();

    @Test void eachFormatCreatesItsExporter() {
        assertInstanceOf(TxtExporter.class, factory.createExporter("txt"));
        assertInstanceOf(JsonExporter.class, factory.createExporter(" JSON "));
        assertInstanceOf(HtmlExporter.class, factory.createExporter("HTML"));
    }
    @Test void rejectsUnknownOrMissingFormats() {
        assertThrows(IllegalArgumentException.class, () -> factory.createExporter("pdf"));
        assertThrows(IllegalArgumentException.class, () -> factory.createExporter(""));
        assertThrows(IllegalArgumentException.class, () -> factory.createExporter(null));
    }
    @Test void creatorOperationUsesOverriddenFactoryMethod() throws Exception {
        AtomicBoolean called = new AtomicBoolean();
        ExporterFactory custom = new ExporterFactory() {
            @Override public ReportExporter createExporter(String format) {
                assertEquals("custom", format);
                return (summary, path) -> { called.set(true); Files.writeString(path, summary.event().name()); };
            }
        };
        var facade = SampleData.createDemo();
        Summary summary = facade.getSummary(facade.getEvents().getFirst().id());
        Path path = directory.resolve("custom.txt");
        custom.export(summary, path, "custom");
        assertTrue(called.get()); assertEquals(summary.event().name(), Files.readString(path));
    }
    @Test void escapesJsonAndHtmlSpecialCharacters() {
        assertEquals("\"a\\\"\\\\\\n\\r\\t\\u0000\\u0008\\u000c\"", JsonExporter.quote("a\"\\\n\r\t\u0000\b\f"));
        assertEquals("\"\\ud83d\\ude00\"", JsonExporter.quote("😀"));
        assertEquals("&lt;script&gt;&amp;&quot;&#39;", HtmlExporter.escape("<script>&\"'"));
    }
}
