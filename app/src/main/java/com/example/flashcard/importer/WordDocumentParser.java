package com.example.flashcard.importer;

import android.net.Uri;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class WordDocumentParser {

    private static final String TAG = "WordDocumentParser";

    public static ImportResult parse(InputStream inputStream) {
        List<ParsedFlashcard> validCards = new ArrayList<>();
        List<ImportError> errors = new ArrayList<>();

        try {
            String documentXml = extractDocumentXml(inputStream);
            if (documentXml == null) {
                errors.add(new ImportError(0, "Not a valid DOCX file. Cannot find document content."));
                return new ImportResult(validCards, errors);
            }

            String fullText = parseXmlToText(documentXml);
            parseTextToFlashcards(fullText, validCards, errors);

        } catch (Exception e) {
            Log.e(TAG, "Error parsing document", e);
            errors.add(new ImportError(0, "Error reading document: " + e.getMessage()));
        }

        return new ImportResult(validCards, errors);
    }

    private static String extractDocumentXml(InputStream inputStream) throws Exception {
        ZipInputStream zis = new ZipInputStream(inputStream);
        ZipEntry entry;
        while ((entry = zis.getNextEntry()) != null) {
            if (entry.getName().equals("word/document.xml")) {
                java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
                byte[] buffer = new byte[4096];
                int read;
                while ((read = zis.read(buffer)) != -1) {
                    baos.write(buffer, 0, read);
                }
                zis.closeEntry();
                return baos.toString("UTF-8");
            }
            zis.closeEntry();
        }
        return null;
    }

    private static String parseXmlToText(String xmlText) throws Exception {
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XmlPullParser parser = factory.newPullParser();

        java.io.StringReader reader = new java.io.StringReader(xmlText);
        parser.setInput(reader);

        StringBuilder fullText = new StringBuilder();
        int eventType = parser.getEventType();

        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG) {
                String tag = parser.getName();
                if ("br".equals(tag)) {
                    fullText.append("\n");
                } else if ("tab".equals(tag)) {
                    fullText.append("\t");
                } else if ("t".equals(tag)) {
                    String text = parser.nextText();
                    if (text != null) {
                        fullText.append(text);
                    }
                }
            } else if (eventType == XmlPullParser.END_TAG) {
                String tag = parser.getName();
                if ("p".equals(tag)) {
                    fullText.append("\n");
                }
            }
            eventType = parser.next();
        }

        return fullText.toString();
    }

    private static void parseTextToFlashcards(String text, List<ParsedFlashcard> validCards, List<ImportError> errors) {
        // Split by recognizing ques: and ans: patterns instead of relying blindly on regex which may overshoot
        // However, regex can be structured to find keywords.
        // We look for ques: optionally followed by spaces, then ", then anything, then "

        // Find all occurrences of "ques:" and "ans:" to build blocks.
        // Even better: process tokens chronologically!

        Pattern keywordPattern = Pattern.compile("(?i)(ques\\s*:|ans\\s*:)");
        Matcher matcher = keywordPattern.matcher(text);

        List<Block> blocks = new ArrayList<>();
        while (matcher.find()) {
            blocks.add(new Block(matcher.group(1).toLowerCase().contains("ques"), matcher.start(), matcher.end()));
        }

        int currentEntryIndex = 1;

        for (int i = 0; i < blocks.size(); i++) {
            Block block = blocks.get(i);

            if (!block.isQuestion) {
                // Orphan answer!
                // Only register if it's not matching a preceding question properly
                if (i == 0 || !blocks.get(i-1).isQuestion) {
                    errors.add(new ImportError(currentEntryIndex, "An answer must belong to a ques: entry."));
                    currentEntryIndex++;
                }
                continue;
            }

            // We have a question block.
            // Text to consider for this question is from block.end to the next block's start.
            int nextBlockStart = (i + 1 < blocks.size()) ? blocks.get(i+1).start : text.length();
            String qTextRaw = text.substring(block.end, nextBlockStart);

            // Extract text between quotes
            String qExtracted = extractQuotedText(qTextRaw);

            if (qExtracted == null) {
                errors.add(new ImportError(currentEntryIndex, "Question is missing quotation marks."));
                currentEntryIndex++;
                continue;
            }

            // Now check if the next block is an answer
            if (i + 1 >= blocks.size() || blocks.get(i+1).isQuestion) {
                errors.add(new ImportError(currentEntryIndex, "Missing answer for question."));
                currentEntryIndex++;
                continue;
            }

            Block nextBlock = blocks.get(i+1);
            int afterNextBlock = (i + 2 < blocks.size()) ? blocks.get(i+2).start : text.length();
            String aTextRaw = text.substring(nextBlock.end, afterNextBlock);

            String aExtracted = extractQuotedText(aTextRaw);

            if (aExtracted == null) {
                errors.add(new ImportError(currentEntryIndex, "Answer is missing quotation marks."));
                currentEntryIndex++;
                i++; // Skip the ans block
                continue;
            }

            // Both extracted successfully!
            validCards.add(new ParsedFlashcard(qExtracted, aExtracted));
            currentEntryIndex++;
            i++; // Skip the ans block
        }
    }

    private static String extractQuotedText(String raw) {
        int firstQuote = raw.indexOf('"');
        if (firstQuote == -1) {
            // Check for smart quotes as well! Word sometimes uses smart quotes.
            firstQuote = raw.indexOf('“');
            if (firstQuote == -1) firstQuote = raw.indexOf('”');
            if (firstQuote == -1) return null;
        }

        int lastQuote = raw.lastIndexOf('"');
        if (lastQuote <= firstQuote) {
            lastQuote = raw.lastIndexOf('”'); // closing smart quote
            if (lastQuote <= firstQuote) {
               lastQuote = raw.lastIndexOf('“');
            }
        }

        if (lastQuote <= firstQuote) {
            return null; // Not properly closed, or only one quote found
        }

        return raw.substring(firstQuote + 1, lastQuote).trim();
    }

    private static class Block {
        boolean isQuestion;
        int start;
        int end;

        Block(boolean isQuestion, int start, int end) {
            this.isQuestion = isQuestion;
            this.start = start;
            this.end = end;
        }
    }
}
