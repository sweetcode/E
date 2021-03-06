package de.SweetCode.e.resources.routines;

import de.SweetCode.e.utils.ToString.ToStringBuilder;

import java.util.*;
import java.util.function.Predicate;

public abstract class Task {

    /**
     * The name of the task.
     */
    private final String name;

    /**
     * It's the parent parent of the task.
     */
    private Task parent;

    /**
     * It's current taskStatus.
     */
    private TaskStatus taskStatus = TaskStatus.FRESH;

    /**
     * This list keeps track of all children.
     */
    private List<Task> children = new LinkedList<>();

    /**
     * This map keeps track of all predicates and their last results.
     */
    private Map<Predicate<Task>, Boolean> predicates = new LinkedHashMap<>();

    public Task() {
        this(null);
    }

    public Task(String name) {
        this.name = name;
    }

    /**
     * Returns the name of the name, null when no name provided.
     * @return Returns a string.
     */
    public String getName() {
        return this.name;
    }

    /**
     * <p>
     *     Returns the structure of the task and its children as a printable string.
     * </p>
     *
     * @return The structure as string.
     */
    public String getStructure() {

        StringBuilder builder = new StringBuilder();
        this.print(builder, "", true);
        return builder.toString();

    }

    /**
     * @return Returns the current {@link TaskStatus status} of the task.
     */
    public TaskStatus getTaskStatus() {
        return this.taskStatus;
    }

    /**
     * This method returns a stream of all children.
     *
     * @return Returns a {@link LinkedList} with all children {@link Task tasks} of this task.
     */
    public List<Task> getChildren() {
        return this.children;
    }

    /**
     * Returns a child by its index.
     * @param index The index of the child.
     * @return Returns the {@link Task} defined by the provided index.
     */
    public Task getChild(int index) {

        if(this.children.isEmpty() || index < 0 || index >= this.children.size()) {
            throw new IndexOutOfBoundsException("A child with this index does not exist.");
        }

        return this.children.get(index);
    }

    /**
     * Returns the number of children of the task.
     * @return The amount of children.
     */
    public int getChildAmount() {
        return this.children.size();
    }

    /**
     * This method will add a child to the list of this task's children.
     *
     * @param child The child {@link Task task}.
     */
    public void addChild(Task child) {
        //@TODO Check if one of the parents has this child's instance already as child/parent, if this is the case
        // the child cannot be added again, because e.g. this would cause an endless loop in the cancel method.
        // Update 12-2-2017: Maybe you wanna allow endless loops... not sure about that yet, if there should maybe be an option
        // to detect or ignore in the Settings.

        child.setParent(this);
        this.children.add(child);
    }

    /**
     * Adds a predicate.
     *
     * @param predicate The {@link Predicate}.
     */
    public void addFilter(Predicate<Task> predicate) {
        this.predicates.put(predicate, null);
    }

    /**
     * Returns its parent.
     * @return Returns the parent of the task, it is null if the task has no parent.
     */
    public Task getParent() {
        return this.parent;
    }

    /**
     * Sets the parent of the task.
     * @param parent The parent that should be assigned to this task.
     */
    public void setParent(Task parent) {
        this.parent = parent;
    }

    /**
     * Checks if the task is the given taskStatus.
     * @param taskStatus The taskStatus to check against.
     * @return true if the task is in the given taskStatus, otherwise false.
     */
    public boolean is(TaskStatus taskStatus) {
        return this.taskStatus == taskStatus;
    }

    /**
     * This method will be called to request a rerun and wants to keep running.
     */
    public final void running() {

        this.taskStatus = TaskStatus.RUNNING;

        if(!(this.parent == null)) {
            this.parent.child(this.taskStatus, this);
        }

    }

    /**
     * This method will be called to inform its parent that it succeeded.
     */
    public final void success() {

        this.taskStatus = TaskStatus.SUCCEEDED;

        if(!(this.parent == null)) {
            this.parent.child(this.taskStatus, this);
        }

    }

    /**
     * This method will be called to inform its parent that it failed.
     */
    public final void fail() {

        this.taskStatus = TaskStatus.FAILED;

        if(!(this.parent == null)) {
            this.parent.child(this.taskStatus, this);
        }

    }

    /**
     * This task and all it's children will be cancelled if the current task is running.
     */
    public final void cancel() {

        if(this.is(TaskStatus.RUNNING)) {
            this.children.forEach(Task::cancel);
        }

        this.taskStatus = TaskStatus.CANCELLED;

    }

    /**
     * Starts the task and calls the run method, if the current status is running you won't be able to call this method.
     * If it is any other mode than FRESH the reset method will be called.
     */
    public void start() {

        // We don't wanna interrupt running tasks.
        if(this.is(TaskStatus.RUNNING)) {
            return;
        }

        // Reset
        if(!(this.is(TaskStatus.FRESH))) {
            this.reset();
        }

        //--- If all predicates succeed, we can run it
        //      - We also store the last value that the predicate returned to improve the ability of debugging trees.
        if(
            this.predicates.entrySet().stream().allMatch(e -> {
                boolean value = e.getKey().test(this);
                this.predicates.put(e.getKey(), value);
                return value;
            })
        ) {
            this.run();
        } else {
            this.fail();
        }

    }

    /**
     * Resets this task to make it restart from scratch on next run.
     */
    public void reset() {

        if(this.is(TaskStatus.RUNNING)) {
            this.cancel();
        }

        this.children.forEach(Task::reset);
        this.taskStatus = TaskStatus.FRESH;

    }

    /**
     * <p>
     *    This method will be called if the task:
     * </p>
     * <ul>
     *     <li><i>RUNNING</i> when one of the children of this task needs to run again.</li>
     *     <li><i>SUCCEEDED</i> when one of the children of this task succeeds.</li>
     *     <li><i>FAILED</i> when one of the children of this task fails.</li>
     * </ul>
     *
     * @param taskStatus The taskStatus of the task.
     * @param task The task that called this method.
     */
    public void child(TaskStatus taskStatus, Task task) {}

    /**
     * This method contains the logic/job that is the task supposed to do if called.
     */
    public abstract void run();

    @Override
    public String toString() {

        return ToStringBuilder.create(this)
                .append("name", (this.name == null ? this.getClass().getSimpleName() : this.name))
                .append("taskStatus", this.taskStatus.name())
                .append("parent", (this.parent == null ? null : this.parent.getName()))
                .append("filters", this.predicates)
            .build();
    }

    private void print(StringBuilder stringBuilder, String prefix, boolean isTail) {

        //--- Append current child
        stringBuilder.append(String.format(
            "%s%s%s\n",
                prefix,
                (isTail ? "└── " : "├── "),
                (this.name == null ? this.getClass().getSimpleName() : this.name)
        ));

        //--- Append filters
        this.predicates.entrySet().forEach(e ->
            stringBuilder.append(String.format(
                "%s? FILTER -> %s\n",
                prefix,
                (e.getValue() == null ? "[Never Run]" : String.valueOf(e.getValue()))
            ))
        );

        //--- Add all of its children minus the last one
        for (int i = 0; i < this.children.size() - 1; i++) {
            this.children.get(i).print(stringBuilder, String.format("%s%s", prefix, (isTail ? "    " : "│   ")), false);
        }

        //--- If it this task has children then append the last one as tail.
        if (this.children.size() > 0) {
            this.children.get(this.children.size() - 1).print(stringBuilder,  String.format("%s%s", prefix, (isTail ? "    " : "│   ")), true);
        }
    }

}
