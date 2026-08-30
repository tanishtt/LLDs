package com.tanish.lld.command.textEditor;

import java.util.ArrayDeque;
import java.util.Deque;

interface Command{
    void execute();
    void undo();
}

//RECEIVER
/*TextEditor is the Receiver.
 It knows HOW to actually modify the document.
 Commands only coordinate with it.
 */
class TextEditor{
    private final StringBuilder content=new StringBuilder();
    public void insert(int position, String text){
        if (position < 0 || position > content.length()) {
            throw new IndexOutOfBoundsException("Invalid position: " + position);
        }
        content.insert(position, text);
    }
    public String delete(int start, int end){
        if (start < 0 || end > content.length() || start > end) {
            System.out.println(content.length() + " " + start + " " + end);
            throw new IndexOutOfBoundsException("Invalid range: [" + start + ", " + end + "]");
        }
        String deletedText = content.substring(start, end);
        content.delete(start, end);
        return deletedText;
    }

    public String getText(){
        return content.toString();
    }
    public int getLength(){
        return content.length();
    }
}

//CONCRETE COMMAND
/*
 * ============================================================
 *                 CONCRETE COMMAND
 * ============================================================
 *
 * InsertTextCommand : "Insert this text at this position."
 * DeleteTextCommand : "Delete text from start to end.", We store the deleted text so that undo() can restore it.
 * ReplaceTextCommand : "Replace old text with new text.", We store the old text so that undo() can restore it.
 */
class InsertTextCommand implements Command{
    private final TextEditor editor;
    private final int position;
    private final String text;

    InsertTextCommand(TextEditor editor, int position, String text) {
        this.editor = editor;
        this.position = position;
        this.text = text;
    }

    @Override
    public void execute() {
        editor.insert(position,text);
    }

    @Override
    public void undo() {
        editor.delete(position, position+text.length());
    }
}
class DeleteTextCommand implements Command{
    private final TextEditor editor;
    private final int start;
    private final int end;
    private String deletedText;


    DeleteTextCommand(TextEditor editor, int start, int end) {
        this.editor = editor;
        this.start = start;
        this.end = end;
    }

    @Override
    public void execute() {
        deletedText=editor.delete(start,end);
    }

    @Override
    public void undo() {
        editor.insert(start,deletedText);
    }
}
class ReplaceTextCommand implements Command{
    private final TextEditor editor;
    private final int start;
    private final int end;
    private final String newText;
    private String oldText;

    ReplaceTextCommand(TextEditor editor, int start, int end, String newText) {
        this.editor = editor;
        this.start = start;
        this.end = end;
        this.newText = newText;
    }

    @Override
    public void execute() {
        oldText = editor.delete(start,end);
        editor.insert(start,newText);
    }

    @Override
    public void undo() {
        editor.delete(start, start+newText.length());
        editor.insert(start, oldText);
    }
}

//COMMAND MANAGER // INVOKER
class CommandManager{
    private final Deque<Command> undoStack=new ArrayDeque<>();
    private final Deque<Command> redoStack=new ArrayDeque<>();

    public void execute(Command command){
        command.execute();
        undoStack.push(command);
        redoStack.clear();
    }
    public boolean undo(){
        if (undoStack.isEmpty()) {
            return false;
        }
        Command command = undoStack.pop();
        command.undo();
        redoStack.push(command);
        return true;
    }
    public boolean redo(){
        if (redoStack.isEmpty()) {
            return false;
        }
        Command command=redoStack.pop();
        command.execute();
        undoStack.push(command);
        return true;

    }
    public void clearHistory(){
        undoStack.clear();
        redoStack.clear();
    }
}
public class TextEditorSystemDriver {
    public static void main(String[] args) {
        TextEditor editor = new TextEditor();
        CommandManager commandManager = new CommandManager();

        //INSERT "Hello"
        commandManager.execute(new InsertTextCommand(editor,0,"Hello"));
        System.out.println(editor.getText());//Hello

        //INSERT "World"
        commandManager.execute(new InsertTextCommand(editor,5," World"));
        System.out.println(editor.getText());//Hello World

        //DELETE "World"
        commandManager.execute(new DeleteTextCommand(editor,6,  11));
        System.out.println(editor.getText());//Hello

        //UNDO Delete
        commandManager.undo();
        System.out.println(editor.getText());//Hello World

        //UNDO insert "world"
        commandManager.undo();
        System.out.println(editor.getText());//Hello

        //REDO insert "world"
        commandManager.redo();
        System.out.println(editor.getText());//Hello World

        //Replace "world" with "java"
        commandManager.execute(new ReplaceTextCommand(editor,6,11,"Java"));
        System.out.println(editor.getText());//Hello Java

        //UNDO replace
        commandManager.undo();
        System.out.println(editor.getText());//Hello World

        //REDO replace
        commandManager.redo();
        System.out.println(editor.getText());//Hello Java




    }
}
