package de.SweetCode.e.input;

import de.SweetCode.e.E;
import de.SweetCode.e.event.EventListener;
import de.SweetCode.e.event.Subscribe;
import de.SweetCode.e.input.combinations.InputCombination;
import de.SweetCode.e.input.combinations.InputCombinationEvent;
import de.SweetCode.e.input.entries.*;
import de.SweetCode.e.rendering.Priority;
import de.SweetCode.e.utils.ToString.ToStringBuilder;

import java.awt.*;
import java.awt.event.*;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.LinkedTransferQueue;

/**
 * <p>
 *    The Input is basically just a {@link KeyAdapter} waiting for all user input to put it into {@link LinkedTransferQueue}
 *    to make it easier accessible for the user.
 * </p>
 */
public final class Input extends KeyAdapter implements EventListener {

    private final Queue<KeyEntry> keyQueue = new LinkedTransferQueue<>();
    private final Queue<MouseEntry> mouseQueue = new LinkedTransferQueue<>();
    private final Queue<MouseReleaseEntry> mouseReleasedQueue = new LinkedTransferQueue<>();
    private final Queue<MouseWheelEntry> mouseScrollQueue = new LinkedTransferQueue<>();
    private final Queue<MouseDraggedEntry> mouseDraggedEntries = new LinkedTransferQueue<>();
    private final Queue<MouseEntry> mouseMovedEntries = new LinkedTransferQueue<>();
    private final Queue<InputCombinationEntry> inputCombinationsQueue = new LinkedTransferQueue<>();

    /**
     * <p>
     *    Creates a new instance of Input and calls the {@link Input#register()} method to set up all listeners.
     * </p>
     */
    public Input() {
        this.register();
    }

    /**
     * <p>
     *    Sets up all listeners.
     * </p>
     */
    private void register() {

        E.getE().getEventHandler().registerListener(this);

        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(e -> {

            if(e.getID() == KeyEvent.KEY_PRESSED) {
                this.keyQueue.add(
                    KeyEntry.Builder.create()
                        .keyCode(e.getKeyCode())
                        .extendedKeyCode(e.getExtendedKeyCode())
                        .character(e.getKeyChar())
                        .keyLocation(e.getKeyLocation())
                        .isActionKey(e.isActionKey())
                        .isAltDown(e.isAltDown())
                        .isAltGraphDown(e.isAltGraphDown())
                        .isMetaDown(e.isMetaDown())
                        .isShiftDown(e.isShiftDown())
                    .build()
                );
            }

            if(e.getID() == KeyEvent.KEY_RELEASED) {
                this.keyQueue.removeIf(entry -> entry.getKeyCode() == e.getKeyCode());
            }

            return true;

        });

        E.getE().getScreen().addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                Input.this.mouseQueue.add(
                    MouseEntry.Builder.create()
                        .button(e.getButton())
                        .clickCount(e.getClickCount())
                        .isPopupTrigger(e.isPopupTrigger())
                        .locationOnScreen(e.getLocationOnScreen())
                        .point(e.getPoint())
                        .isAltDown(e.isAltDown())
                        .isAltGraphDown(e.isAltGraphDown())
                        .isControlDown(e.isControlDown())
                        .isMetaDown(e.isMetaDown())
                        .isShiftDown(e.isShiftDown())
                    .build()
                );

            }

            @Override
            public void mouseReleased(MouseEvent e) {
                Input.this.mouseReleasedQueue.add(
                    MouseEntry.Builder.create()
                        .button(e.getButton())
                        .clickCount(e.getClickCount())
                        .isPopupTrigger(e.isPopupTrigger())
                        .locationOnScreen(e.getLocationOnScreen())
                        .point(e.getPoint())
                        .isAltDown(e.isAltDown())
                        .isAltGraphDown(e.isAltGraphDown())
                        .isControlDown(e.isControlDown())
                        .isMetaDown(e.isMetaDown())
                        .isShiftDown(e.isShiftDown())
                    .buildReleaseEntry()
                );
            }
        });

        E.getE().getScreen().addMouseWheelListener(e -> Input.this.mouseScrollQueue.add(
            MouseWheelEntry.Builder.create()
                .point(e.getPoint())
                .preciseWheelRotation(e.getPreciseWheelRotation())
                .scrollAmount(e.getScrollAmount())
                .unitsToScroll(e.getUnitsToScroll())
                .wheelRotation(e.getWheelRotation())
                .isAltDown(e.isAltDown())
                .isAltGraphDown(e.isAltGraphDown())
                .isControlDown(e.isControlDown())
                .isMetaDown(e.isMetaDown())
                .isShiftDown(e.isShiftDown())
            .build()
        ));

        E.getE().getScreen().addMouseMotionListener(new MouseMotionAdapter() {

            @Override
            public void mouseDragged(MouseEvent e) {

                Input.this.mouseDraggedEntries.add(
                    MouseEntry.Builder.create()
                        .button(e.getButton())
                        .clickCount(e.getClickCount())
                        .isPopupTrigger(e.isPopupTrigger())
                        .locationOnScreen(e.getLocationOnScreen())
                        .point(e.getPoint())
                        .isAltDown(e.isAltDown())
                        .isAltGraphDown(e.isAltGraphDown())
                        .isControlDown(e.isControlDown())
                        .isMetaDown(e.isMetaDown())
                        .isShiftDown(e.isShiftDown())
                    .buildDraggedEntry()
                );

            }

            @Override
            public void mouseMoved(MouseEvent e) {
                Input.this.mouseMovedEntries.add(
                    MouseEntry.Builder.create()
                        .button(e.getButton())
                        .clickCount(e.getClickCount())
                        .isPopupTrigger(e.isPopupTrigger())
                        .locationOnScreen(e.getLocationOnScreen())
                        .point(e.getPoint())
                        .isAltDown(e.isAltDown())
                        .isAltGraphDown(e.isAltGraphDown())
                        .isControlDown(e.isControlDown())
                        .isMetaDown(e.isMetaDown())
                        .isShiftDown(e.isShiftDown())
                    .build()
                );
            }
        });

    }

    @Subscribe
    public void onInputCombinationEvent(InputCombinationEvent event) {
        this.inputCombinationsQueue.add(new InputCombinationEntry(event.getInputCombination()));
    }

    /**s
     * <p>
     *    This method returns a {@link LinkedList} of all keyboard entries since the last update cycle.
     *    <b>Event:</b> {@link KeyEvent} and {@link KeyEvent#getID()} equals to <i>{@link KeyEvent#KEY_PRESSED}</i>
     * </p>
     *
     * @return A list of all registered keyboard entries.
     */
    public LinkedList<KeyEntry> getKeyboardEntries() {
        return new LinkedList<>(this.keyQueue);
    }

    /**
     * <p>
     *    This method returns a {@link LinkedList} of all mouse entries since the last update cycle.
     *    <b>Event:</b> {@link MouseAdapter#mousePressed(MouseEvent)}
     * </p>
     *
     * @return A list of all registered mouse entries.
     */
    public LinkedList<MouseEntry> getMouseEntries() {
        return new LinkedList<>(this.mouseQueue);
    }

    /**
     * <p>
     *    This method returns a {@link LinkedList} of all mouse wheel entries since the last update cycle.
     *    <b>Event:</b> {@link MouseWheelEvent}
     * </p>
     *
     * @return A list of all registered mouse wheel entries.
     */
    public LinkedList<MouseWheelEntry> getMouseWheelEntries() {
        return new LinkedList<>(this.mouseScrollQueue);
    }

    /**
     * <p>
     *    This method returns a {@link LinkedList} of all mouse dragged entries since the last update cycle.
     *    <b>Event:</b> {@link MouseMotionAdapter#mouseDragged(MouseEvent)}
     * </p>
     *
     * @return A list of all registered mouse dragged entries.
     */
    public LinkedList<MouseDraggedEntry> getMouseDraggedEntries() {
        return new LinkedList<>(this.mouseDraggedEntries);
    }

    /**
     * <p>
     *    This method returns a {@link LinkedList} of all mouse moved entries since the last update cycle.
     *    <b>Event:</b> {@link MouseMotionAdapter#mouseMoved(MouseEvent)}
     * </p>
     *
     * @return A list of all registered mouse moved entries.
     */
    public LinkedList<MouseMoveEntry> getMouseMovedEntries() {
        return new LinkedList(this.mouseMovedEntries);
    }

    /**
     * <p>
     *    This method returns a {@link LinkedList} of all mouse moving entries since the last update cycle.
     *    <b>Event:</b> Since Java doesn't support listing to mouse movement we are using an internal loop to keep track
     *    of the movement. The {@link de.SweetCode.e.loop.MouseMovingLoop} is responsible for this task.
     * </p>
     *
     * @return A list of all required mouse moving entries.
     */
    public LinkedList<MouseMovingEntry> getMouseMovingEntries() {
        return new LinkedList<>(E.getE().getMouseMovingLoop().getMouseMovingEntries());
    }

    /**
     * <p>
     *    This method returns a {@link LinkedList} of all mouse moved entries since the last update cycle
     *    <b>Event:</b> {@link MouseAdapter#mouseReleased(MouseEvent)}
     * </p>
     *
     * @return A list of all registered mouse release entries.
     */
    public LinkedList<MouseReleaseEntry> getMouseReleasedQueue() {
        return new LinkedList<>(this.mouseReleasedQueue);
    }

    /**
     * <p>
     *    This method returns a {@link LinkedList} of all {@link InputCombination}s since the last update cycle.
     * </p>
     *
     * @return
     */
    public LinkedList<InputCombinationEntry> getInputCombinationsQueue() {
        return new LinkedList<>(this.inputCombinationsQueue);
    }

    /**
     * <p>
     *    This method clears all queues.
     * </p>
     */
    public void clear() {

        this.keyQueue.clear();
        this.mouseQueue.clear();
        this.mouseScrollQueue.clear();
        this.mouseDraggedEntries.clear();
        this.mouseMovedEntries.clear();
        this.mouseReleasedQueue.clear();
        this.inputCombinationsQueue.clear();

        E.getE().getMouseMovingLoop().getMouseMovingEntries().clear();

    }

    /**
     * <p>
     *    Builds a new {@link InputEntry} by calling:
     * </p>
     * <ul>
     *     <li>{@link Input#getKeyboardEntries()}</li>
     *     <li>{@link Input#getMouseEntries()}</li>
     *     <li>{@link Input#getMouseWheelEntries()}</li>
     *     <li>{@link Input#getMouseDraggedEntries()}</li>
     *     <li>{@link Input#getMouseMovedEntries()}</li>
     *     <li>{@link Input#getMouseMovingEntries()}</li>
     *     <li>{@link Input#getMouseReleasedQueue()}</li>
     * </ul>
     * <p>
     *    and passing them to its constructor.
     * </p>
     *
     * @return Builds a new {@link InputEntry} with the current values and removes all from the queue.
     */
    public InputEntry build() {

        InputEntry inputEntry = new InputEntry(
            this.getKeyboardEntries(),
            this.getMouseEntries(),
            this.getMouseWheelEntries(),
            this.getMouseDraggedEntries(),
            this.getMouseMovedEntries(),
            this.getMouseMovingEntries(),
            this.getMouseReleasedQueue(),
            this.getInputCombinationsQueue()
        );

        this.clear();
        return inputEntry;
    }

    @Override
    public String toString() {
        return ToStringBuilder.create(this)
            .append("keyQueue", this.keyQueue)
            .append("mouseQueue", this.mouseQueue)
            .append("mouseScrollQueue", this.mouseScrollQueue)
            .append("mouseDraggedEntries", this.mouseDraggedEntries)
            .append("mouseMovedEntries", this.mouseMovedEntries)
            .append("mouseMovingEntries", E.getE().getMouseMovingLoop().getMouseMovingEntries())
            .append("mouseReleasedQueue", this.mouseReleasedQueue)
            .append("inputCombinationsQueue", this.inputCombinationsQueue)
        .build();
    }

}
