package de.SweetCode.e.resources.dialogue.condition;

import de.SweetCode.e.E;
import de.SweetCode.e.utils.log.LogEntry;
import de.SweetCode.e.utils.log.LogPrefixes;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;

/**
 * <p>
 * A DialogueConditionWrapper is a wrapper object containing all related information to a condition.
 * </p>
 */
public class DialogueConditionWrapper {

    private final DialogueConditions instance;

    private final String id;
    private final Method method;

    private final List<String> fieldNames;

    /**
     * <p>
     *    Creates a new DialogueConditionWrapper.
     * </p>
     *
     * @param instance The instance of the dialogue conditions class.
     * @param id The unique id of the condition.
     * @param method The method of the condition to invoke.
     * @param fields The fields the method expects.
     */
    public DialogueConditionWrapper(DialogueConditions instance, String id, Method method, List<String> fields) {
        this.instance = instance;

        this.id = id;
        this.method = method;

        this.fieldNames = fields;
    }

    /**
     * <p>
     *    Gives the unique identifier of the condition provided by {@link DialogueCondition#id()}.
     * </p>
     * @return The identifier of the condition.
     */
    public String getIdentifier() {
        return this.id;
    }

    /**
     * <p>
     *    Invokes the condition method with all required values requested by {@link DialogueConditionOption}.
     * </p>
     *
     * @param fields A map with all fields and their current values to pass them to condition method.
     * @return Returns true of all conditions are fulfilled, otherwise false.
     */
    public boolean isFulfilled(Map<String, Object> fields) {

        // build arguments
        Object[] arguments = new Object[this.fieldNames.size()];
        for(int i = 0; i < this.fieldNames.size(); i++) {
            arguments[i] = fields.get(this.fieldNames.get(i));
        }

        boolean result = false;

        try {
            result = (boolean) this.method.invoke(this.instance, arguments);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;

    }

    /**
     * <p>
     *    This method creates a new map of all {@link DialogueCondition DialogueConditions}. - The key of the map is the
     *    id with a leading at-symbol (@, e.g: <i>@myId</i>). The value is a {@link DialogueConditionWrapper which contains}
     *    all related information of the validation rule.
     * </p>
     *
     * @param dialogueConditions A wrapper class containing all DialogueConditions that can be used in the context.
     * @return Returns a map with the parsed conditions, the map is empty if there are no condtions present.
     */
    public static Map<String, DialogueConditionWrapper> getConditionWrappers(DialogueConditions dialogueConditions) {

        Map<String, DialogueConditionWrapper> conditionWrapper = new HashMap<>();

        if(dialogueConditions == null) {
            return conditionWrapper;
        }

        Method[] methods = dialogueConditions.getClass().getMethods();
        Arrays.stream(methods)
                .filter(m -> m.isAnnotationPresent(DialogueCondition.class))
                .forEach(m -> {

                    //well... the method is supposed to return a boolean
                    if(!(m.getReturnType().isAssignableFrom(boolean.class))) {
                        System.out.println(m.getReturnType().getName());
                        E.getE().getLog().log(
                            LogEntry.Builder.create(DialogueConditionWrapper.class)
                                .prefix(LogPrefixes.DIALOGUE)
                                .message("The DialogueConditions method %s does not return a boolean.", m.getName())
                            .build()
                        );
                        return;
                    }

                    DialogueCondition idAnnotation = m.getAnnotation(DialogueCondition.class);
                    List<String> fields = new LinkedList<>();

                    for(Annotation[] aArray : m.getParameterAnnotations()) {
                        for(Annotation annotation : aArray) {

                            //is DialogueConditionOption?
                            if(annotation.annotationType().equals(DialogueConditionOption.class)) {
                                fields.add(((DialogueConditionOption) annotation).fieldName());
                            }

                        }
                    }

                    // if the length is not equal, than we didn't get for all parameters a field type
                    if(!(m.getParameterTypes().length == fields.size())) {
                        E.getE().getLog().log(
                            LogEntry.Builder.create(DialogueConditionWrapper.class)
                                .prefix(LogPrefixes.DIALOGUE)
                                .message("The DialogueConditions method %s has not enough annotations.", m.getName())
                            .build()
                        );
                        return;
                    }

                    // Condition name storted with an @ - to make the lookup easier
                    conditionWrapper.put(
                            ("@" + idAnnotation.id()),
                            new DialogueConditionWrapper(dialogueConditions, idAnnotation.id(), m, fields)
                    );

                });

        return conditionWrapper;

    }

}
