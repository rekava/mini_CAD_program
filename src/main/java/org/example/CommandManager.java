package org.example;

import java.util.Stack;

public class CommandManager {
    private Stack<Command> undoStack = new Stack<>();
    private Stack<Command> redoStack = new Stack<>();

    public void execute(Command command) {
        command.execute();
        undoStack.push(command);
        redoStack.clear();
        System.out.println("=== EXECUTE: " + command.getClass().getSimpleName() + " | Undo size: " + undoStack.size());
    }

    public void undo() {
        if (!undoStack.isEmpty()) {
            Command cmd = undoStack.pop();
            cmd.undo();
            redoStack.push(cmd);
            System.out.println("=== UNDO: " + cmd.getClass().getSimpleName() + " | Undo size: " + undoStack.size());
        }
    }

    public void redo() {
        if (!redoStack.isEmpty()) {
            Command cmd = redoStack.pop();
            cmd.execute();
            undoStack.push(cmd);
            System.out.println("=== REDO: " + cmd.getClass().getSimpleName() + " | Undo size: " + undoStack.size());
        }
    }
}
