package edu.campus.library.service.command;

/** Command pattern (L5): an operation that can be undone. */
public interface Command {
    void execute();

    void undo();

    String description();
}
