package de.SweetCode.e.resources.dialogue.parser;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.SweetCode.e.E;
import de.SweetCode.e.resources.dialogue.Dialogue;
import de.SweetCode.e.resources.dialogue.DialogueNode;
import de.SweetCode.e.resources.dialogue.DialogueOptionPointer;
import de.SweetCode.e.resources.dialogue.DialogueParser;
import de.SweetCode.e.resources.dialogue.condition.DialogueConditionWrapper;
import de.SweetCode.e.resources.dialogue.condition.DialogueConditions;
import de.SweetCode.e.utils.Assert;
import de.SweetCode.e.utils.exceptions.ParserException;
import de.SweetCode.e.utils.log.LogEntry;
import de.SweetCode.e.utils.log.LogPrefixes;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class YarnJsonParser implements DialogueParser<String, String> {

    private final static Pattern pattern = Pattern.compile("\\[{2}(.*?)\\|(.*?)(\\|(@.*))?\\]{2}");
    private JsonParser jsonParser = new JsonParser();

    private Map<DialogueNode<String>, List<DialogueOptionPointer>> dialogueNodes = new HashMap<>();

    public YarnJsonParser() {
    }

    @Override
    public Dialogue parse(String startIdentifier, String input, DialogueConditions dialogueConditions) {

        Assert.assertNotNull("The input cannot be null.", input);
        Assert.assertNotNull("The start identifier cannot be null.", startIdentifier);

        // prepare to parse again
        this.dialogueNodes.clear();

        // All parts of the dialogue
        JsonArray array = this.jsonParser.parse(input).getAsJsonArray();

        // parsing conditions
        Map<String, DialogueConditionWrapper> conditionWrapper = DialogueConditionWrapper.getConditionWrappers(dialogueConditions);

        final DialogueNode[] startNode = {null};

        array.forEach(e -> {

            if(this.isValidNode(e)) {

                JsonObject node = e.getAsJsonObject();

                String id = node.get("title").getAsString();
                String body = node.get("body").getAsString();

                //parsing body, and adding all pointers.
                List<DialogueOptionPointer> pointers = new LinkedList<>();
                List<DialogueConditionWrapper> conditionPointers = new ArrayList<>();

                Matcher matcher = YarnJsonParser.pattern.matcher(body);
                while (matcher.find()) {

                    String optionText = matcher.group(1);
                    String optionPointer = matcher.group(2);

                    // Get all conditions
                    String conditionString = matcher.group(4);
                    if(conditionString != null) {

                        String[] conditions = conditionString.split(",");

                        for(String condition : conditions) {

                            condition = condition.trim();
                            if(conditionWrapper.containsKey(condition)) {
                                conditionPointers.add(conditionWrapper.get(condition));
                            } else {
                                //@TODO Log that the requested condition doesn't exist.
                            }

                        }

                    }

                    pointers.add(
                            new DialogueOptionPointer(
                                    optionText,
                                    DialogueNode.dummy(optionPointer),
                                    conditionPointers
                            )
                    );
                    // @TODO: Do I wanna support pointers in-text, not sure about that yet...

                }

                // find start node
                DialogueNode tmpNode = new DialogueNode<>(id, body, null);
                if(id.equals(startIdentifier)) {
                    startNode[0] = tmpNode;
                }

                this.dialogueNodes.put(tmpNode, pointers);

            } else {
                System.out.println("invalid node");
            }

        });

        this.dialogueNodes.forEach((node, fakePointers) -> {

            List<DialogueOptionPointer> pointers = new LinkedList<>();

            fakePointers.forEach(p -> {

                // Note: can be safely casted to a String, because I always set as identifier of a pointer a string in this parser.
                Optional<DialogueNode<String>> realPointer = this.getNodeByIdentifier((String) p.getPointer().getIdentifier());

                if(realPointer.isPresent()) {
                    pointers.add(new DialogueOptionPointer(p.getOptionText(), realPointer.get(), p.getDialogueConditionWrappers()));
                } else {
                    E.getE().getLog().log(
                        LogEntry.Builder.create(YarnJsonParser.class)
                            .prefix(LogPrefixes.DIALOGUE)
                            .message("Failed to find the DialogueOptionPointer with this identifier: %s.", (String) p.getPointer().getIdentifier())
                        .build()
                    );
                }

            });

            node.setPointers(pointers);

        });

        if(startNode[0] == null) {
            throw new ParserException(String.format("Failed to find start node with the identifier %s.", startIdentifier));
        }

        return new Dialogue<>(startNode[0]);

    }

    private boolean isValidNode(JsonElement node) {

        if (node == null || !(node.isJsonObject())) {
            return false;
        }

        JsonObject object = node.getAsJsonObject();

        //@TODO Validate that the node id (title) doesn't exist yet
        return (
                object.has("title") && object.get("title").isJsonPrimitive() &&
                        object.has("body") && object.get("body").isJsonPrimitive() &&
                        !(this.dialogueNodes.containsKey(DialogueNode.dummy(object.get("title").getAsString())))
        );

    }

    private Optional<DialogueNode<String>> getNodeByIdentifier(String identifier) {
        return this.dialogueNodes.keySet().stream()
                .filter(e -> e.getIdentifier().equals(identifier))
                .findAny();
    }

}
