package io.github.ragportfolio.evaluation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.*;

class SyntheticDatasetContractTest {
    @Test
    void loadsSyntheticCasesWithConsistentEvidenceLabels() throws IOException {
        var cases = loadJsonLines("cases.jsonl");
        var documents = loadJsonLines("documents.jsonl");
        assertEquals(6, cases.size(), "synthetic-v1 case count");
        assertEquals(5, documents.size(), "synthetic-v1 document count");

        var byDocumentKey = new HashMap<String, JsonNode>();
        for (var document : documents) {
            String key = text(document, "documentKey");
            assertEquals(text(document, "tenantId") + "/" + text(document, "documentId"), key);
            assertNull(byDocumentKey.put(key, document), "duplicate document key: " + key);
        }

        var caseIds = new HashSet<String>();
        var slices = new HashSet<String>();
        for (var evaluationCase : cases) {
            String caseId = text(evaluationCase, "id");
            assertTrue(caseIds.add(caseId), "duplicate case id: " + caseId);
            slices.add(text(evaluationCase, "slice"));
            assertTrue(text(evaluationCase, "question").length() <= 4000, caseId);
            long topK = positiveInteger(evaluationCase, "topK");
            assertTrue(topK <= 20, caseId);
            String tenantId = text(evaluationCase.get("actor"), "tenantId");
            String subjectId = text(evaluationCase.get("actor"), "subjectId");
            String outcome = text(evaluationCase, "expectedOutcome");
            assertTrue(Set.of("ANSWER_WITH_EVIDENCE", "NO_CONTEXT").contains(outcome), caseId);

            var forbidden = textSet(evaluationCase, "forbiddenDocumentKeys");
            for (String key : forbidden) {
                assertTrue(byDocumentKey.containsKey(key), caseId + " unknown forbidden document: " + key);
            }
            var citations = array(evaluationCase, "requiredCitations");
            assertEquals("NO_CONTEXT".equals(outcome), citations.isEmpty(), caseId + " outcome/citation mismatch");
            for (var citation : citations) {
                String key = text(citation, "documentKey");
                var document = byDocumentKey.get(key);
                assertNotNull(document, caseId + " unknown cited document: " + key);
                assertFalse(forbidden.contains(key), caseId + " forbidden citation: " + key);
                assertEquals("ACTIVE", text(document, "status"), caseId);
                assertEquals(tenantId, text(document, "tenantId"), caseId);
                assertTrue(textSet(document, "readableBy").contains(subjectId), caseId + " unreadable citation");
                assertEquals(positiveInteger(document, "version"), positiveInteger(citation, "version"), caseId);
                assertEquals(text(document, "locator"), text(citation, "locator"), caseId);
            }
        }
        assertEquals(Set.of("answerable", "no-context", "cross-tenant", "denied", "deleted", "version-change"), slices);
    }

    private static List<JsonNode> loadJsonLines(String fileName) throws IOException {
        String resource = "/eval/synthetic-v1/" + fileName;
        // Read the canonical fixture through test resources; do not depend on the launch directory.
        try (var input = SyntheticDatasetContractTest.class.getResourceAsStream(resource)) {
            assertNotNull(input, "missing evaluation resource: " + resource);
            try (var reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
                var mapper = JsonMapper.builder().build();
                var rows = new ArrayList<JsonNode>();
                for (String line; (line = reader.readLine()) != null;) {
                    var row = mapper.readTree(line);
                    assertNotNull(row, "empty JSONL record in " + fileName);
                    assertTrue(row.isObject(), "JSONL record must be an object in " + fileName);
                    rows.add(row);
                }
                return List.copyOf(rows);
            }
        }
    }

    private static String text(JsonNode object, String field) {
        assertNotNull(object, "missing object for " + field);
        var value = object.get(field);
        assertNotNull(value, "missing field: " + field);
        assertTrue(value.isString(), "expected string: " + field);
        String result = value.stringValue();
        assertFalse(result.isBlank(), "blank field: " + field);
        return result;
    }

    private static long positiveInteger(JsonNode object, String field) {
        var value = object.get(field);
        assertNotNull(value, "missing field: " + field);
        assertTrue(value.isIntegralNumber(), "expected integer: " + field);
        long result = value.longValue();
        assertTrue(result >= 1, "expected positive integer: " + field);
        return result;
    }

    private static JsonNode array(JsonNode object, String field) {
        var value = object.get(field);
        assertNotNull(value, "missing field: " + field);
        assertTrue(value.isArray(), "expected array: " + field);
        return value;
    }

    private static Set<String> textSet(JsonNode object, String field) {
        var values = new HashSet<String>();
        for (var value : array(object, field)) {
            assertTrue(value.isString(), "expected string item: " + field);
            String item = value.stringValue();
            assertFalse(item.isBlank(), "blank item: " + field);
            assertTrue(values.add(item), "duplicate item in " + field + ": " + item);
        }
        return Set.copyOf(values);
    }
}
