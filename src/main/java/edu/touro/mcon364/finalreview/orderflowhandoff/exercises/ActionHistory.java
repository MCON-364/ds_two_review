package edu.touro.mcon364.finalreview.orderflowhandoff.exercises;

import edu.touro.mcon364.finalreview.model.Action;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;

/**
 * In-class Exercise 1 — Action History
 *
 * A simple editor needs to remember actions so the user can undo and redo work.
 *
 * Requirements:
 * - perform(action) records a newly completed action.
 * - undo() removes and returns the action that should be undone next.
 * - redo() removes and returns the action that should be redone next.
 * - undo() returns Optional.empty() when there is nothing available to undo.
 * - redo() returns Optional.empty() when there is nothing available to redo.
 * - performing a new action after one or more undo operations makes the old redo path invalid.
 * - getUndoCount() returns how many actions are currently available to undo.
 * - getRedoCount() returns how many actions are currently available to redo.
 *
 * You may add private fields and private helper methods.
 * Do not change the public method signatures.
 * Before coding, decide:
 * - What information does this class need to remember?
 * - What is the appropriate data structure
 * - Which operation should be fastest?
 * - When an action is undone, where should it go so it can be redone later?
 * - What should happen to redo history after a brand-new action is performed?

 */
public class ActionHistory {

    private final Deque<Action> undoStack = new ArrayDeque<>();
    private final Deque<Action> redoStack = new ArrayDeque<>();

    public void perform(Action action) {
        undoStack.push(action);
        redoStack.clear();
    }

    public Optional<Action> undo() {
        if (undoStack.isEmpty()) return Optional.empty();
        Action action = undoStack.pop();
        redoStack.push(action);
        return Optional.of(action);
    }

    public Optional<Action> redo() {
        if (redoStack.isEmpty()) return Optional.empty();
        Action action = redoStack.pop();
        undoStack.push(action);
        return Optional.of(action);
    }

    public int getUndoCount() {
        return undoStack.size();
    }

    public int getRedoCount() {
        return redoStack.size();
    }
}
