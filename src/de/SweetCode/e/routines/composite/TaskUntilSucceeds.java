package de.SweetCode.e.routines.composite;

import de.SweetCode.e.routines.Task;
import de.SweetCode.e.routines.TaskDecorator;
import de.SweetCode.e.routines.TaskStatus;

public class TaskUntilSucceeds<T> extends TaskDecorator<T> {

    public TaskUntilSucceeds() {
        super();
    }

    public TaskUntilSucceeds(String name) {
        this(name, null);
    }

    public TaskUntilSucceeds(String name, Task<T> task) {
        super(name, task);
    }

    @Override
    public void child(TaskStatus taskStatus, Task<T> task) {

        switch (taskStatus) {

            case FAILED:
                this.getChild(0).start();
            break;

            default:
                super.child(taskStatus, task);
            break;

        }

    }

}
